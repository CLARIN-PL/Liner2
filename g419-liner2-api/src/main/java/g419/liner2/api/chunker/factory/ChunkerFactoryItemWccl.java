package g419.liner2.api.chunker.factory;


import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.WcclChunker;
import g419.corpus.Logger;
import org.ini4j.Ini;


/*
 * @author Maciej Janicki
 */

public class ChunkerFactoryItemWccl extends ChunkerFactoryItem {

	public ChunkerFactoryItemWccl() {
		super("wccl");
	}

	@Override
	public Chunker getChunker(Ini.Section description, ChunkerManager cm) throws Exception {
        String wcclFile = description.get("rules");
        Logger.log("--> WCCL Chunker with rules file " + wcclFile);
        WcclChunker chunker = new WcclChunker();
        chunker.setWcclFile(wcclFile);
        return chunker;
	}

}
