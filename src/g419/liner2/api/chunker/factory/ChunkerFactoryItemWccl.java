package g419.liner2.api.chunker.factory;


import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.WcclChunker;
import g419.liner2.api.tools.Logger;

import java.util.regex.Matcher;


/*
 * @author Maciej Janicki
 */

public class ChunkerFactoryItemWccl extends ChunkerFactoryItem {

	public ChunkerFactoryItemWccl() {
		super("wccl:(.*)");
	}

	@Override
	public Chunker getChunker(String description, ChunkerManager cm) throws Exception {
		Matcher matcherWccl = this.pattern.matcher(description);
		if (matcherWccl.find()) {
			String wcclFile = matcherWccl.group(1);
			Logger.log("--> WCCL Chunker with rules file " + wcclFile);
			WcclChunker chunker = new WcclChunker();
			chunker.setWcclFile(wcclFile);
            return chunker;		
		}
		return null;
	}

}
