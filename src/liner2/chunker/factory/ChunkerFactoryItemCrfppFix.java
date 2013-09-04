package liner2.chunker.factory;

import java.util.regex.Matcher;

import liner2.Main;
import liner2.chunker.CrfppChunker;
import liner2.chunker.Chunker;
import liner2.chunker.CrfppFix;
import liner2.chunker.PropagateChunker;
import liner2.tools.ParameterException;


public class ChunkerFactoryItemCrfppFix extends ChunkerFactoryItem {

	public ChunkerFactoryItemCrfppFix() {
		super("crfpp-fix:(.*)");
	}

	@Override
	public Chunker getChunker(String description, ChunkerManager cm) throws Exception {
       	Matcher matcher = this.pattern.matcher(description);
		if (matcher.find()){
			String chunkername = matcher.group(1); 
            Main.log("--> CRFPP Fix Chunker on  " + chunkername);
            
			Chunker baseChunker = cm.getChunkerByName(chunkername);
			if (baseChunker == null)
				throw new ParameterException("Crfpp Fix: undefined base chunker: " + chunkername);
			return new CrfppFix(baseChunker);
		}

		return null;
	}

}
