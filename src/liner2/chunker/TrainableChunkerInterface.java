package liner2.chunker;

import liner2.structure.ParagraphSet;

public interface TrainableChunkerInterface {

	public void train(ParagraphSet paragraphSet) throws Exception;
	
}
