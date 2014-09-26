package g419.liner2.api.chunker.factory;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.CrfTemplate;
import g419.corpus.structure.Document;
import g419.liner2.api.Liner2;
import g419.liner2.api.LinerOptions;
import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.CrfppChunker;
import g419.liner2.api.converter.Converter;
import g419.liner2.api.converter.factory.ConverterFactory;
import g419.liner2.api.features.TokenFeatureGenerator;
import g419.liner2.api.tools.Logger;
import g419.liner2.api.tools.TemplateFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ini4j.Ini;
import org.ini4j.Profile;

public class ChunkerFactoryItemCrfpp extends ChunkerFactoryItem {

	public ChunkerFactoryItemCrfpp() {
		super("crfpp");
	}

	@Override
	public Chunker getChunker(Ini.Section description, ChunkerManager cm) throws Exception {
        if (!cm.opts.libCRFPPLoaded){
            try {
                String linerJarPath = Liner2.class.getProtectionDomain().getCodeSource().getLocation().getPath();
                System.load(linerJarPath.replace("g419-liner2-api.jar","") + "libCRFPP.so");
            } catch (UnsatisfiedLinkError e) {
                System.err.println("Cannot load the libCRFPP.so native code.\nIf you are using liner as an imported jar specify correct path as CRFlib parameter in config.\n" + e);
                System.exit(1);
            }
        }
        String mode = description.get("mode");
        if(mode.equals("train")){
            return train(description, cm);
        }
        else if(mode.equals("load")){
            return load(description);
        }
        else{
            throw new Exception("Unrecognized mode for CRFPP chunker: " + mode + "(Valid: train/load)");
        }
	}

	/**
	 * Load the model from a file.
	 * @param description
	 * @return
	 * @throws IOException
	 */
    private Chunker load(Profile.Section description) throws IOException {
        String store = description.get("store");

        Logger.log("--> CRFPP Chunker deserialize from " + store);

        CrfppChunker chunker = new CrfppChunker();
        chunker.deserialize(store);

        return chunker;
    }

    /**
     * Train the chunker and serialize the model to a file.
     * @param description
     * @param cm
     * @return
     * @throws Exception
     */
    private Chunker train(Profile.Section description, ChunkerManager cm) throws Exception {
        Logger.log("--> CRFPP Chunker train");

        Converter trainingDataConverter = null;
        if ( description.containsKey("training-data-converter") ){
        	ArrayList<String> converters = new ArrayList<String>();
        	for ( String in : description.get("training-data-converter").split(","))
        		converters.add(in);
        	trainingDataConverter = ConverterFactory.createPipe(converters);
        }
        
        int threads = Integer.parseInt(description.get("threads"));
        String inputFile = description.get("training-data");
        String inputFormat;
        String modelFilename = description.get("store");

        TokenFeatureGenerator gen = new TokenFeatureGenerator(cm.opts.features);
        ArrayList<Document> trainData = new ArrayList<Document>();

        // Setup training data 
        if(inputFile.equals("{CV_TRAIN}")){
        	if ( trainingDataConverter != null ){
        		for ( Document doc : cm.trainingData ){
        			Document docClone = doc.clone();
        			trainingDataConverter.apply(docClone);
        			trainData.add(docClone);
        		}        			
        	}
        	else{
        		trainData = cm.trainingData;
        	}
        }
        else{
            inputFormat = description.get("format");
            AbstractDocumentReader reader = 
            		ReaderFactory.get().getStreamReader(inputFile, inputFormat);
            Document document = reader.nextDocument();
            while ( document != null ){
                gen.generateFeatures(document);
                if ( trainingDataConverter != null )
                	trainingDataConverter.apply(document);
                trainData.add(document);
                document = reader.nextDocument();
            }
        }
        
        List<Pattern> types = new ArrayList<Pattern>();
        if ( description.containsKey("types")) {
            types = LinerOptions.getGlobal().parseTypes(description.get("types"));
        }

        Logger.log("--> Training on file=" + inputFile);

        String templateData = description.get("template");
        CrfppChunker chunker = new CrfppChunker(threads, types);
        if(!templateData.equals("null")){
            CrfTemplate template = TemplateFactory.parseTemplate(templateData);
            template.setAttributeIndex(gen.getAttributeIndex());
            chunker.setTemplate(template);
        }
        chunker.setModelFilename(modelFilename);

        for(Document document: trainData)
             chunker.addTrainingData(document);
        chunker.train();

        return chunker;
    }

}
