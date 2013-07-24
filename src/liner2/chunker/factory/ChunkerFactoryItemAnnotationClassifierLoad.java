package liner2.chunker.factory;

import java.util.regex.Matcher;

import liner2.Main;
import liner2.chunker.AnnotationClassifierChunker;
import liner2.chunker.Chunker;
import liner2.tools.ParameterException;

public class ChunkerFactoryItemAnnotationClassifierLoad extends ChunkerFactoryItem {

	public ChunkerFactoryItemAnnotationClassifierLoad() {
		super("classifier-load:model=(.*):base=(.*)");
	}

	@Override
	public Chunker getChunker(String description) throws Exception {
		Matcher m = this.pattern.matcher(description);
		
		if ( !m.find() )
			return null;
		
        String modelFilename = m.group(1);
        String inputClassifier = m.group(2);

		Chunker baseChunker = ChunkerFactory.getChunkerByName(inputClassifier);
		if (baseChunker == null)
			throw new ParameterException("Annotation Classifier: undefined base chunker: " + inputClassifier);

		AnnotationClassifierChunker chunker = new AnnotationClassifierChunker(baseChunker);
        
        chunker.deserialize(modelFilename);
        Main.log(chunker.toString());
                        		
		return chunker;
	}

}
