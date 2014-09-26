package g419.liner2.api.chunker.factory;


import g419.liner2.api.chunker.Chunker;
import org.ini4j.Ini;

import java.util.regex.Matcher;


public class ChunkerFactoryItemEnsamble extends ChunkerFactoryItem {

	public ChunkerFactoryItemEnsamble() {
		super("ensamble");
	}

	@Override
	public Chunker getChunker(Ini.Section description, ChunkerManager cm) throws Exception {
	    return ChunkerFactory.getChunkerPipe(description.get("chunkers"), cm);

	}
}
