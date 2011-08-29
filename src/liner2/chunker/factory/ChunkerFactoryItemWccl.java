package liner2.chunker.factory;

import java.util.regex.Matcher;

import liner2.Main;
import liner2.chunker.WcclChunker;
import liner2.chunker.Chunker;

/*
 * @author Maciej Janicki
 */

public class ChunkerFactoryItemWccl extends ChunkerFactoryItem {

	public ChunkerFactoryItemWccl() {
		super("wccl:(.*)");
	}

	@Override
	public Chunker getChunker(String description) throws Exception {
		Matcher matcherWccl = this.pattern.matcher(description);
		if (matcherWccl.find()) {
			String wcclFile = matcherWccl.group(1);
			Main.log("--> WCCL Chunker with rules file " + wcclFile);
			WcclChunker chunker = new WcclChunker();
			chunker.setWcclFile(wcclFile);
            return chunker;		
		}
		return null;
	}

}
