package g419.liner2.api.chunker.factory;


import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.PropagateChunker;
import g419.liner2.api.tools.Logger;
import g419.liner2.api.tools.ParameterException;

import java.util.regex.Matcher;


public class ChunkerFactoryItemPropagate extends ChunkerFactoryItem {

	public ChunkerFactoryItemPropagate() {
		super("propagate:(.*?)");
	}
	
	@Override
	public Chunker getChunker(String description, ChunkerManager cm) throws Exception {
		Matcher matcher = this.pattern.matcher(description);
		if (matcher.find()) {
			Logger.log("--> Chunk propagation");

			String baseChunkerName = matcher.group(1);
			Chunker baseChunker = cm.getChunkerByName(baseChunkerName);
			if (baseChunker == null)
				throw new ParameterException("Propagate Chunker: undefined base chunker: " + baseChunkerName);
			return new PropagateChunker(baseChunker);
		}
		else
			return null;
	}

}
