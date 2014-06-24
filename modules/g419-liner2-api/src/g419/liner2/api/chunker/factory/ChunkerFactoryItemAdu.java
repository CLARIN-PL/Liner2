package g419.liner2.api.chunker.factory;


import g419.liner2.api.chunker.AduChunker;
import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.tools.Logger;
import g419.liner2.api.tools.ParameterException;

import java.util.regex.Matcher;


public class ChunkerFactoryItemAdu extends ChunkerFactoryItem {

	public ChunkerFactoryItemAdu() {
		super("adu:(.*?)(:one)?");
	}

	@Override
	public Chunker getChunker(String description, ChunkerManager cm) throws Exception {
		Matcher matcher = this.pattern.matcher(description);
		if (matcher.find()) {
			Logger.log("--> Automatic Dictionary Update chunker");

			String baseChunkerName = matcher.group(1);
			Chunker baseChunker = cm.getChunkerByName(baseChunkerName);
			if (baseChunker == null)
				throw new ParameterException("ADU Chunker: undefined base chunker: " + baseChunkerName);
			boolean one = (matcher.group(2) != null);
			AduChunker chunker = new AduChunker();
			chunker.setSettings(baseChunker, one);
			return chunker;
		}
		else
			return null;
	}
}
