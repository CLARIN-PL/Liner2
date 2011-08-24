package liner2.chunker;

import java.util.ArrayList;

import liner2.structure.Sentence;

public interface TrainableChunkerInterface {

	public void train(ArrayList<Sentence> sentences);
	
}
