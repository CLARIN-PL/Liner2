package g419.liner2.api.chunker.factory;


import g419.lib.cli.ParameterException;
import g419.liner2.api.chunker.AnnotationRenameChunker;
import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.CrfppFix;
import g419.corpus.Logger;

import org.ini4j.Ini;


public class ChunkerFactoryItemAnnotationRename extends ChunkerFactoryItem {

	public ChunkerFactoryItemAnnotationRename() {
		super("annotation-rename");
	}

	@Override
	public Chunker getChunker(Ini.Section description, ChunkerManager cm) throws Exception {
        String chunkername = description.get("base-chunker");
        String file = description.get("file");
        Logger.log("--> AnnotationRenameChunker on  " + chunkername);

        Chunker baseChunker = cm.getChunkerByName(chunkername);
        if (baseChunker == null){
            throw new ParameterException("Undefined base chunker: " + chunkername);
        }
        return new AnnotationRenameChunker(baseChunker);

	}

}
