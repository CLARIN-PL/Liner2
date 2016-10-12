package g419.liner2.api.chunker.factory;


import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.ensemble.CascadeChunker;
import g419.liner2.api.chunker.ensemble.MajorityVotingChunker;
import g419.liner2.api.chunker.ensemble.UnionChunker;
import g419.corpus.Logger;
import org.ini4j.Ini;
import g419.liner2.api.normalizer.factory.GlobalRuleTimexNormalizerFactoryItem;
import g419.liner2.api.normalizer.factory.GlobalTimexNormalizerFactoryItem;
import g419.liner2.api.normalizer.factory.RBNormalizerFactoryItem;

import java.util.ArrayList;


public class ChunkerFactory {

    private static final String CHUNKER_TYPE = "type";
    private static ChunkerFactory factory = null;
	
	private ArrayList<ChunkerFactoryItem> items = new ArrayList<ChunkerFactoryItem>();
	
	private ChunkerFactory(){
		// TODO automatycznie dodać obiekty dziedziczące po ChunkerFactoryItem z pakietu factory
		this.items.add(new ChunkerFactoryItemAdu());
        this.items.add(new ChunkerFactoryItemAnnotationRename());
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
		this.items.add(new ChunkerFactoryItemMinos());
		this.items.add(new ChunkerFactoryItemAnnotationAdder());
		this.items.add(new ChunkerFactoryItemAnnotationAdderWithZero());
		this.items.add(new ChunkerFactoryItemAnnotationAdderOnlyZero());
		this.items.add(new ChunkerFactoryItemNullChunker());
        this.items.add(new ChunkerFactoryItemAnnotationCRFClassifier());
        this.items.add(new ChunkerFactoryItemMapping());
        this.items.add(new ChunkerFactoryItemIobber());
        this.items.add(new ChunkerFactoryItemChunkRel());
        this.items.add(new ChunkerFactoryItemRulesChunker());
        this.items.add(new ChunkerFactoryItemRuleTitle());
        this.items.add(new RBNormalizerFactoryItem());
        this.items.add(new ChunkerFactoryItemRuleRoad());
        this.items.add(new GlobalTimexNormalizerFactoryItem());
        this.items.add(new GlobalRuleTimexNormalizerFactoryItem());
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
			sb.append("  " + item.getType() + "\n");
		return sb.toString();
	}
	
	/**
	 * Creates a chunker according to the description
	 * @param description
	 * @return
	 * @throws Exception 
	 */
	public static Chunker createChunker(Ini.Section description, ChunkerManager cm) throws Exception{
		Logger.log("-> Setting up chunker: " + description.getName());
		for (ChunkerFactoryItem item : ChunkerFactory.get().items) {
            if ( item.getType().equals(description.get(CHUNKER_TYPE)) ) {
				Chunker chunker =  item.getChunker(description, cm);
                chunker.setDescription(description);
                return chunker;
            }
        }
        throw new Error(String.format("Chunker description '%s' not recognized", description.get(CHUNKER_TYPE)));
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
            return getChunkerCascadePipe(chunkerNames[0].split(">"), cm);
        }
        else {
            ArrayList<Chunker> chunkers = new ArrayList<Chunker>();
            for (String name: chunkerNames){
                chunkers.add(getChunkerCascadePipe(name.split(">"), cm));
            }
            return new MajorityVotingChunker(chunkers);
        }
    }

    private static Chunker getChunkerCascadePipe(String[] chunkerNames, ChunkerManager cm) {
        if (chunkerNames.length == 1){
            return cm.getChunkerByName(chunkerNames[0]);
        }
        else {
            ArrayList<Chunker> chunkers = new ArrayList<Chunker>();
            for (String name: chunkerNames){
                chunkers.add(cm.getChunkerByName(name));
            }
            return new CascadeChunker(chunkers);
        }
    }
}
