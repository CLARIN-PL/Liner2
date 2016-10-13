package g419.liner2.api.chunker.factory;


import org.ini4j.Ini;

import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.CrfppFix;


public class ChunkerFactoryItemCrfppFix extends ChunkerFactoryItem {

	public ChunkerFactoryItemCrfppFix() {
		super("crfpp-fix");
	}

	@Override
	public Chunker getChunker(Ini.Section description, ChunkerManager cm) throws Exception {
        return new CrfppFix();

	}

}
