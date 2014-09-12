package g419.liner2.api.chunker.factory;

import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.MappingChunker;

import java.util.regex.Matcher;

/**
 * Created by michal on 9/12/14.
 */
public class ChunkerFactoryItemMapping extends ChunkerFactoryItem {

    public ChunkerFactoryItemMapping() {
        super("mapping:([^:]*)");
    }
    @Override
    public Chunker getChunker(String description, ChunkerManager cm) throws Exception {
        Matcher matcher = this.pattern.matcher(description);
        if (matcher.find()){
            return new MappingChunker(matcher.group(1), cm.testData);
        }
        else{
            return null;
        }
    }
}
