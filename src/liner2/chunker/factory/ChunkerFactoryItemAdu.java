package liner2.chunker.factory;

import java.util.regex.Matcher;
import liner2.chunker.AduChunker;
import liner2.chunker.Chunker;
import liner2.chunker.factory.ChunkerFactory;
import liner2.tools.ParameterException;
import liner2.Main;

public class ChunkerFactoryItemAdu extends ChunkerFactoryItem {

	public ChunkerFactoryItemAdu() {
		super("adu:(.*?)(:one)?");
	}

	@Override
	public Chunker getChunker(String description) throws Exception {
		Matcher matcher = this.pattern.matcher(description);
		if (matcher.find()) {
			Main.log("--> Automatic Dictionary Update chunker");

			String baseChunkerName = matcher.group(1);
			Chunker baseChunker = ChunkerFactory.getChunkerByName(baseChunkerName);
			if (baseChunker == null)
				throw new ParameterException("ADU Chunker: undefined base chunker: " + baseChunkerName);
			boolean one = (matcher.group(2) != null);
			AduChunker chunker = new AduChunker();
			chunker.setSettings(baseChunker, one);
			return chunker;
		}
		else
			return null;
	}
}
