package liner2.chunker.factory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import liner2.Main;
import liner2.chunker.AnnotationClassifierChunker;
import liner2.chunker.Chunker;
import liner2.chunker.TrainableChunkerInterface;
import liner2.reader.ReaderFactory;
import liner2.reader.StreamReader;
import liner2.tools.CorpusFactory;
import liner2.tools.ParameterException;

public class ChunkerFactoryItemAnnotationClassifierTrain extends ChunkerFactoryItem {

	public ChunkerFactoryItemAnnotationClassifierTrain() {
		super("classifier-train:(ccl|iob|data)=([^:]*?)(:model=([^:]*))?(:features=([^:]*))?(:base=(.*))?");
	}

	@Override
	public Chunker getChunker(String description) throws Exception {
		Main.log("Training annotation classifier");

		Matcher m = this.pattern.matcher(description);
		
		if ( !m.find() )
			return null;
        String inputFormat = m.group(1);
        String inputFile = m.group(2);
        String outputFile = m.group(4);
        String featuresFile = m.group(6);
        String inputClassifier = m.group(8);

		Chunker baseChunker = null;
		
		if ( inputClassifier != null ){
			baseChunker = ChunkerFactory.getChunkerByName(inputClassifier);
			if (baseChunker == null)
				throw new ParameterException("Annotation Classifier: undefined base chunker: " + inputClassifier);
		}

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

		AnnotationClassifierChunker chunker = new AnnotationClassifierChunker(baseChunker, features);

        if ((inputFormat.equals("iob")) || (inputFormat.equals("ccl"))) {
        	Main.log("--> Training on file=" + inputFile);            
        	StreamReader reader = ReaderFactory.get().getStreamReader(inputFile, inputFormat);
        	((TrainableChunkerInterface)chunker).train(reader.readParagraphSet());
        }
        else {
        	Main.log("--> Training on corpus=" + inputFile);
        	((TrainableChunkerInterface)chunker).train(CorpusFactory.get().query(inputFile));
        }
        
        if ( outputFile != null ){
        	chunker.serialize(outputFile);
        }
		
		return chunker;
	}

}
