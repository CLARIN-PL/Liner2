package g419.liner2.api.chunker.factory;


import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.Document;
import g419.lib.cli.ParameterException;
import g419.liner2.api.LinerOptions;
import g419.liner2.api.chunker.AnnotationWekaClassifierChunker;
import g419.liner2.api.chunker.Chunker;
import g419.corpus.ConsolePrinter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.ini4j.Ini;
import org.ini4j.Profile;

public class ChunkerFactoryItemAnnotationWekaClassifier extends ChunkerFactoryItem {

	public ChunkerFactoryItemAnnotationWekaClassifier() {
		super("classifier");
	}

	@Override
	public Chunker getChunker(Ini.Section description, ChunkerManager cm) throws Exception {
		ConsolePrinter.log("Training annotation classifier");

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
            if(!feature.isEmpty() && !feature.startsWith("#")) {
                feature = feature.trim().replace("{INI_PATH}", iniPath);
                features.add(feature);
            }
            feature = br.readLine();
        }

		AnnotationWekaClassifierChunker chunker = new AnnotationWekaClassifierChunker(baseChunker, features);

        String mode = description.get("mode");
        String modelPath = description.get("store");
        File modelFile = new File(modelPath);
        if ( mode.equals("load") && modelFile.exists()){
            chunker.deserialize(modelPath);
        }
        else if(mode.equals("train")){
            train(description, chunker, cm);
            modelFile.createNewFile();
            chunker.serialize(modelPath);
        }
        else{
            throw new Exception("Unrecognized mode for annotation classifier chunker: " + mode + "(Valid: train/load)");
        }
		return chunker;
	}

    private void train(Profile.Section description, AnnotationWekaClassifierChunker chunker, ChunkerManager cm) throws Exception {
        String[] parameters;
        if(description.containsKey("parameters")){
            parameters = description.get("parameters").split(",");
        }
        else{
            parameters = new String[0];
        }
        List<Pattern> types = new ArrayList<Pattern>();
        if ( description.containsKey("types")) {
            types = LinerOptions.getGlobal().parseTypes(description.get("types"));
        }

        String inputFile = description.get("training-data");

        // Setup training data
        ArrayList<Document> trainData = new ArrayList<Document>();
        if(inputFile.equals("{CV_TRAIN}")){
            trainData = cm.trainingData;
        }
        else{
            String inputFormat = description.get("format");
            AbstractDocumentReader reader =
                    ReaderFactory.get().getStreamReader(inputFile, inputFormat);
            Document document = reader.nextDocument();
            while ( document != null ){
                trainData.add(document);
                document = reader.nextDocument();
            }
        }


        ConsolePrinter.log("--> Training on file=" + inputFile);
        chunker.setTypes(types);
        for(Document document: trainData) {
            chunker.updateClassDomain(document);
        }
        chunker.initializeTraining(description.get("classifier"), parameters, description.get("strategy"));
        for(Document document: trainData) {
            chunker.addTrainingData(document);
        }
        chunker.train();
    }

}
