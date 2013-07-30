package liner2.chunker.factory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import liner2.Main;
import liner2.chunker.AnnotationClassifierChunker;
import liner2.chunker.Chunker;
import liner2.tools.ParameterException;

public class ChunkerFactoryItemAnnotationClassifierLoad extends ChunkerFactoryItem {

	public ChunkerFactoryItemAnnotationClassifierLoad() {
		super("classifier-load:model=(.*):features=([^:]*):base=(.*)");
	}

	@Override
	public Chunker getChunker(String description) throws Exception {
		Matcher m = this.pattern.matcher(description);
		
		if ( !m.find() )
			return null;
		
        String modelFilename = m.group(1);
        String featuresFile = m.group(2);
        String inputClassifier = m.group(3);

        System.out.println(m.group(1));
        System.out.println(m.group(2));
        System.out.println(m.group(3));

        BufferedReader br = new BufferedReader(new FileReader(featuresFile));
        List<String> features = new ArrayList<String>();
        try {
            String line = br.readLine();
            while (line != null) {
                features.add(line);
                line = br.readLine();
            }
        } finally {
            br.close();
        }

		Chunker baseChunker = ChunkerFactory.getChunkerByName(inputClassifier);
		if (baseChunker == null)
			throw new ParameterException("Annotation Classifier: undefined base chunker: " + inputClassifier);

		AnnotationClassifierChunker chunker = new AnnotationClassifierChunker(baseChunker, features);
        
        chunker.deserialize(modelFilename);
        Main.log(chunker.toString());
                        		
		return chunker;
	}

}
