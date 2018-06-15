package g419.liner2.core.chunker.factory;


import g419.liner2.core.chunker.Chunker;
import g419.liner2.core.chunker.PolemChunker;
import org.ini4j.Ini;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;


public class ChunkerFactoryItemPolem extends ChunkerFactoryItem {

    public ChunkerFactoryItemPolem() {
        super("polem");
    }

    @Override
    public Chunker getChunker(Ini.Section description, ChunkerManager cm) throws Exception {
        final List<Pattern> patterns = Arrays.asList(Pattern.compile(getParameterString(description, "annotations")));
        return new PolemChunker(cm.getChunkerByName(getParameterString(description, "base-chunker")), patterns);

    }
}
