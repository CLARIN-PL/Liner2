package liner2.chunker.factory;

import java.util.regex.Matcher;

import liner2.Main;
import liner2.chunker.AduChunker;
import liner2.chunker.Chunker;
import liner2.chunker.PropagateChunker;
import liner2.tools.ParameterException;

public class ChunkerFactoryItemPropagate extends ChunkerFactoryItem {

	public ChunkerFactoryItemPropagate() {
		super("propagate:(.*?)");
	}
	
	@Override
	public Chunker getChunker(String description) throws Exception {
		Matcher matcher = this.pattern.matcher(description);
		if (matcher.find()) {
			Main.log("--> Chunk propagation");

			String baseChunkerName = matcher.group(1);
			Chunker baseChunker = ChunkerFactory.getChunkerByName(baseChunkerName);
			if (baseChunker == null)
				throw new ParameterException("Propagate Chunker: undefined base chunker: " + baseChunkerName);
			return new PropagateChunker(baseChunker);
		}
		else
			return null;
	}

}
