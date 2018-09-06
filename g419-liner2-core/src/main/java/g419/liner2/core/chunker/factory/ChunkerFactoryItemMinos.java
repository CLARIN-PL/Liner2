package g419.liner2.core.chunker.factory;

import g419.liner2.core.chunker.Chunker;
import g419.liner2.core.chunker.MinosChunker;
import g419.liner2.core.chunker.MinosChunker.MinosVerb;
import org.ini4j.Ini;

/*
 * @author Michał Marcińczuk
 */

public class ChunkerFactoryItemMinos extends ChunkerFactoryItem {

	public ChunkerFactoryItemMinos() {
		super("minos");
	}

    @Override
    public Chunker getChunker(Ini.Section description, ChunkerManager cm) throws Exception {
        return new MinosChunker(description.get(MinosChunker.OPTION_MALT_MODEL_PATH), description.get(MinosVerb.OPTION_NON_SUBJECT_VERBS), description.get(MinosVerb.OPTION_NON_SUBJECT_VERBS_REFL));
    }
}
