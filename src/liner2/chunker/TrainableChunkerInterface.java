package liner2.chunker;

import liner2.structure.Document;

public interface TrainableChunkerInterface {

	public void addTrainingData(Document document) throws Exception;
	
	public void train() throws Exception;
	
}
