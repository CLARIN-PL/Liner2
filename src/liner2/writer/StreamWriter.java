package liner2.writer;

import liner2.structure.Sentence;

public abstract class StreamWriter {

	public abstract void writeSentence(Sentence sentence);
	
	public void close(){
		
	}
}
