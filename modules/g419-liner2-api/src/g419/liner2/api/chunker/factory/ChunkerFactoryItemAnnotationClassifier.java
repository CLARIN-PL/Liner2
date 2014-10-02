package g419.liner2.api.chunker.factory;


import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.liner2.api.chunker.AnnotationClassifierChunker;
import g419.liner2.api.chunker.Chunker;
import g419.corpus.Logger;
import g419.liner2.api.tools.ParameterException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.ini4j.Ini;

public class ChunkerFactoryItemAnnotationClassifier extends ChunkerFactoryItem {

	public ChunkerFactoryItemAnnotationClassifier() {
		super("classifier:([^:]*)(:base=(.*))?");
	}

	@Override
	public Chunker getChunker(Ini.Section description, ChunkerManager cm) throws Exception {
		Logger.log("Training annotation classifier");

        String inputClassifier = description.get("base-chunker");
        Chunker baseChunker = null;

		if ( inputClassifier != null ){
			baseChunker = cm.getChunkerByName(inputClassifier);
			if (baseChunker == null)
				throw new ParameterException("Annotation Classifier: undefined base chunker: " + inputClassifier);
		}

        List<String> features = new ArrayList<String>();

        File featuresFile = new File(description.get("features"));
        if(!featuresFile.exists())     {
            throw new FileNotFoundException("Error while parsing features:" + description.get("features") + " is not an existing file!");
        }
        String iniPath = featuresFile.getAbsoluteFile().getParentFile().getAbsolutePath();
        BufferedReader br = new BufferedReader(new FileReader(featuresFile));
        StringBuffer sb = new StringBuffer();
        String feature = br.readLine();
        while(feature != null) {
            feature = feature.trim().replace("{INI_PATH}", iniPath);
            features.add(feature);
            feature = br.readLine();
        }

        String[] parameters;
        if(description.containsKey("parameters")){
            parameters = description.get("parameters").split(",");
        }
        else{
            parameters = new String[0];
        }
        for(String p: parameters)
        System.out.println(p);
		AnnotationClassifierChunker chunker = new AnnotationClassifierChunker(baseChunker, features, description.get("classifier"), parameters, description.get("strategy"));

        String mode = description.get("mode");
        String modelPath = description.get("store");
        File modelFile = new File(modelPath);
        if ( mode.equals("load") && modelFile.exists()){
            chunker.deserialize(modelPath);
        }
        else {
            String inputFormat = description.get("format");
            String inputFile = description.get("training-data");

            Logger.log("--> Training on file=" + inputFile);
            AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(inputFile, inputFormat);
            // TODO
            chunker.addTrainingData(reader.nextDocument());
            chunker.train();

            modelFile.createNewFile();
            chunker.serialize(modelPath);
        }
		return chunker;
	}

}
