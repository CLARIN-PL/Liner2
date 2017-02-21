package g419.liner2.api.chunker.factory;


import java.io.IOException;

import org.apache.log4j.Logger;
import org.ini4j.Ini;

import g419.liner2.api.chunker.BsnlpFixChunker;
import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.tools.NeLemmatizer;


public class ChunkerFactoryItemBsnlpFixChunker extends ChunkerFactoryItem {

	private static final String PARAM_CLEANUP = "cleanup";
	private static final String PARAM_NELEXICON = "nelexicon";
	private static final String PARAM_MORFEUSZ = "morfeusz";
	
	public ChunkerFactoryItemBsnlpFixChunker() {
		super("bsnlp-fix");
	}

	@Override
	public Chunker getChunker(Ini.Section description, ChunkerManager cm) throws Exception {
		boolean cleanup = description.containsKey(PARAM_CLEANUP) && description.get(PARAM_CLEANUP).equalsIgnoreCase("true");
		String nelexicon = description.get(PARAM_NELEXICON);
		String morfeusz = description.get(PARAM_MORFEUSZ);
		NeLemmatizer lemmatizer = null;
		if ( nelexicon != null ){
			try{
				lemmatizer = new NeLemmatizer(nelexicon, morfeusz);
			} catch ( IOException ex){
				Logger.getLogger(this.getClass()).error("Failed to load lemmatizer", ex);
			}
		}
        return new BsnlpFixChunker(lemmatizer, cleanup);

	}

}
