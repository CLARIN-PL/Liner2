package g419.liner2.api.chunker.factory;


import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.CrfppFix;
import g419.liner2.api.tools.Logger;
import g419.liner2.api.tools.ParameterException;
import org.ini4j.Ini;

import java.util.regex.Matcher;



public class ChunkerFactoryItemCrfppFix extends ChunkerFactoryItem {

	public ChunkerFactoryItemCrfppFix() {
		super("crfpp-fix:(.*)");
	}

	@Override
	public Chunker getChunker(Ini.Section description, ChunkerManager cm) throws Exception {
        String chunkername = description.get("base-chunker");
        Logger.log("--> CRFPP Fix Chunker on  " + chunkername);

        Chunker baseChunker = cm.getChunkerByName(chunkername);
        if (baseChunker == null)
            throw new ParameterException("Crfpp Fix: undefined base chunker: " + chunkername);
        return new CrfppFix(baseChunker);

	}

}
