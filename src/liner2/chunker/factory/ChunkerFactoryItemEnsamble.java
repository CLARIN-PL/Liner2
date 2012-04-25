package liner2.chunker.factory;

import java.util.regex.Matcher;
import liner2.chunker.AduChunker;
import liner2.chunker.Chunker;
import liner2.chunker.factory.ChunkerFactory;
import liner2.tools.ParameterException;
import liner2.Main;

public class ChunkerFactoryItemEnsamble extends ChunkerFactoryItem {

	public ChunkerFactoryItemEnsamble() {
		super("ensamble:(.*?)");
	}

	@Override
	public Chunker getChunker(String description) throws Exception {
		Matcher matcher = this.pattern.matcher(description);
		if (matcher.find()) {
			String chunkerDescription = matcher.group(1);
			return ChunkerFactory.getChunkerPipe(chunkerDescription);
		}
		else
			return null;
	}
}
