package g419.liner2.api.chunker.factory;


import g419.liner2.api.chunker.Chunker;

import java.util.regex.Matcher;


public class ChunkerFactoryItemEnsamble extends ChunkerFactoryItem {

	public ChunkerFactoryItemEnsamble() {
		super("ensamble:(.*?)");
	}

	@Override
	public Chunker getChunker(String description, ChunkerManager cm) throws Exception {
		Matcher matcher = this.pattern.matcher(description);
		if (matcher.find()) {
			String chunkerDescription = matcher.group(1);
			return ChunkerFactory.getChunkerPipe(chunkerDescription, cm);
		}
		else
			return null;
	}
}
