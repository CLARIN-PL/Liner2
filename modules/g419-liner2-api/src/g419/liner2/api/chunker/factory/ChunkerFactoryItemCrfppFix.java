package g419.liner2.api.chunker.factory;


import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.CrfppFix;
import g419.liner2.api.tools.Logger;
import g419.liner2.api.tools.ParameterException;

import java.util.regex.Matcher;



public class ChunkerFactoryItemCrfppFix extends ChunkerFactoryItem {

	public ChunkerFactoryItemCrfppFix() {
		super("crfpp-fix:(.*)");
	}

	@Override
	public Chunker getChunker(String description, ChunkerManager cm) throws Exception {
       	Matcher matcher = this.pattern.matcher(description);
		if (matcher.find()){
			String chunkername = matcher.group(1); 
            Logger.log("--> CRFPP Fix Chunker on  " + chunkername);
            
			Chunker baseChunker = cm.getChunkerByName(chunkername);
			if (baseChunker == null)
				throw new ParameterException("Crfpp Fix: undefined base chunker: " + chunkername);
			return new CrfppFix(baseChunker);
		}

		return null;
	}

}
