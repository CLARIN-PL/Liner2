package liner2.chunker.factory;

import java.util.regex.Pattern;

import liner2.chunker.Chunker;


public abstract class ChunkerFactoryItem {

	protected Pattern pattern = null;
	
	public ChunkerFactoryItem(String stringPattern){
		pattern = Pattern.compile("^"+stringPattern+"$");
	}
	
	public Pattern getPattern(){
		return this.pattern;
	}
	
	abstract public Chunker getChunker(String description, ChunkerManager cm) throws Exception ;

    public void addChunker(Chunker chunker, String chunkerName, String chunkerDesc, ChunkerManager cm){
        if (chunker != null){
            chunker.setDescription(chunkerDesc);
            cm.addChunker(chunkerName, chunker);
        }
        else
            throw new Error(String.format("Chunker description '%s' not recognized", chunkerDesc));
    }
	
}
