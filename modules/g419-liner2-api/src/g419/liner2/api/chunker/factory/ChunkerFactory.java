package g419.liner2.api.chunker.factory;


import g419.liner2.api.LinerOptions;
import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.ensemble.MajorityVotingChunker;
import g419.liner2.api.chunker.ensemble.UnionChunker;
import g419.liner2.api.tools.Logger;

import java.util.ArrayList;


public class ChunkerFactory {

	private static ChunkerFactory factory = null;
	
	private ArrayList<ChunkerFactoryItem> items = new ArrayList<ChunkerFactoryItem>();
	
	private ChunkerFactory(){
		this.items.add(new ChunkerFactoryItemAdu());
		this.items.add(new ChunkerFactoryItemAnnotationClassifier());
		this.items.add(new ChunkerFactoryItemEnsamble());
		this.items.add(new ChunkerFactoryItemHeuristic());
		this.items.add(new ChunkerFactoryItemCrfppFix());
		this.items.add(new ChunkerFactoryItemCrfpp());
		this.items.add(new ChunkerFactoryItemDictCompile());
		this.items.add(new ChunkerFactoryItemDictLoad());
		this.items.add(new ChunkerFactoryItemDictFullCompile());
		this.items.add(new ChunkerFactoryItemDictFullLoad());
		this.items.add(new ChunkerFactoryItemPropagate());
		this.items.add(new ChunkerFactoryItemWccl());
        this.items.add(new ChunkerFactoryItemAnnotationCRFClassifier());
	}
	
	/**
	 * Get current ChunkerFactory. If the factory does not exist then create it.
	 * @return
	 */
	private static ChunkerFactory get(){
		if ( ChunkerFactory.factory == null )
			ChunkerFactory.factory = new ChunkerFactory();
		return ChunkerFactory.factory;
	}
	
	/**
	 * Get human-readable description of chunker commands.
	 * @return
	 */
	public static String getDescription(){
		StringBuilder sb = new StringBuilder();
		for (ChunkerFactoryItem item : ChunkerFactory.get().items)
			sb.append("  " + item.getPattern() + "\n");
		return sb.toString();
	}
	
	/**
	 * Creates a chunker according to the description
	 * @param description
	 * @return
	 * @throws Exception 
	 */
	public static Chunker createChunker(String description, ChunkerManager cm) throws Exception{
		Logger.log("-> Setting up chunker: " + description);
		for (ChunkerFactoryItem item : ChunkerFactory.get().items) {
			if ( item.getPattern().matcher(description).find() ) {
				Chunker chunker =  item.getChunker(description, cm);
                chunker.setDescription(description);
                return chunker;
            }
        }
        throw new Error(String.format("Chunker description '%s' not recognized", description));
	}
	
	/**
	 * Creates a hash of chunkers according to the description
	 * @param opts
	 * @return
	 * @throws Exception
	 */
	public static ChunkerManager loadChunkers(LinerOptions opts) throws Exception {
        ChunkerManager cm = new ChunkerManager(opts);
		for (String chunkerName : opts.chunkersDescriptions.keySet()) {
			String chunkerDesc = opts.chunkersDescriptions.get(chunkerName);
			Chunker chunker = ChunkerFactory.createChunker(chunkerDesc, cm);
            cm.addChunker(chunkerName, chunker);
		}
        return cm;
	}
	
	
	/**
	 * Validate a chunker description.
	 * @param description
	 * @return
	 */
	public boolean parse(String description){
		for (ChunkerFactoryItem item : this.items)
			if (item.getPattern().matcher(description).find())
				return true;
		return false;
	}
	
	/**
	 * Create chunker pipe according to given description. The chunker names
	 * must be provided in the list of chunker description passed to the
	 * constructor.
	 *
	 * Example: c1 --- getGlobal single chunker named `c1`
	 *
	 * @param description
	 * @return
	 */
    public static Chunker getChunkerPipe(String description, ChunkerManager cm) {
        return getChunkerUnionPipe(description.split("\\+"), cm);
    }

    private static Chunker getChunkerUnionPipe(String[] chunkerNames, ChunkerManager cm) {
        if (chunkerNames.length == 1) {
            return getChunkerVotingPipe(chunkerNames[0].split("\\*"), cm);
        }
        else {
            ArrayList<Chunker> chunkers = new ArrayList<Chunker>();
            for (String name: chunkerNames) {
                chunkers.add(getChunkerVotingPipe(name.split("\\*"), cm));
            }
            return new UnionChunker(chunkers);
        }
    }

    private static Chunker getChunkerVotingPipe(String[] chunkerNames, ChunkerManager cm) {
        if (chunkerNames.length == 1){
            return cm.getChunkerByName(chunkerNames[0]);
        }
        else {
            ArrayList<Chunker> chunkers = new ArrayList<Chunker>();
            for (String name: chunkerNames){
                chunkers.add(cm.getChunkerByName(name));
            }
            return new MajorityVotingChunker(chunkers);
        }
    }
}
