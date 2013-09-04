package liner2.chunker.factory;

import liner2.chunker.Chunker;
import liner2.chunker.ensemble.MajorityVotingChunker;
import liner2.chunker.ensemble.UnionChunker;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: michal
 * Date: 9/3/13
 * Time: 9:40 AM
 * To change this template use File | Settings | File Templates.
 */
public class ChunkerManager {

    private HashMap<String, Chunker> chunkers = new HashMap<String, Chunker>();

    public void addChunker(String name, Chunker chunker) {
        if(chunkers.containsKey(name)){
            throw new Error(String.format("Chunker name '%s' duplicated", name));
        }
        chunkers.put(name, chunker);
    }

    public Chunker getChunkerByName(String name) {
        return chunkers.get(name);
    }

}
