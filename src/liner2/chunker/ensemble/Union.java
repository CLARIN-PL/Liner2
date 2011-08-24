package liner2.chunker.ensemble;

import java.util.ArrayList;

import liner2.structure.Chunk;
import liner2.structure.Chunking;

public class Union {

	public static Chunking run(ArrayList<Chunking> chunkings){
		
		if ( chunkings.size() == 0 )
			return null;
		
		Chunking chunking = new Chunking(chunkings.get(0).getSentence());
		for (Chunking c : chunkings)
			for (Chunk chunk : c.chunkSet())
				if (!chunking.contains(chunk))
					chunking.addChunk(chunk);
		
		return chunking;
	}
	
}
