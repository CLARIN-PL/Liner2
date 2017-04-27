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
import g419.corpus.ConsolePrinter;
import g419.liner2.api.tools.TemplateFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import org.ini4j.Ini;
import org.ini4j.Profile;

public class ChunkerFactoryItemCrfpp extends ChunkerFactoryItem {

	public static final String PARAM_WRAP = "wrap";
	
	public ChunkerFactoryItemCrfpp() {
		super("crfpp");
	}

	@Override
	public Chunker getChunker(Ini.Section description, ChunkerManager cm) throws Exception {
        if (!cm.opts.libCRFPPLoaded){
            try {
                //fixme: I replaced this with exploded JAR-contained library to allow CRFPP building automatization. See ShareLibUtils and g419-external-dependencies module ~Filip
//                String linerJarPath = Liner2.class.getProtectionDomain().getCodeSource().getLocation().getPath();
//                System.load(linerJarPath.replace("g419-liner2-api.jar","") + "libCRFPP.so");
                System.load(SharedLibUtils.getCrfppLibPath());
            } catch (UnsatisfiedLinkError e) {
                System.err.println("Cannot load the libCRFPP.so native code.\nIf you are using liner as an imported jar specify correct path as CRFlib parameter in config.\n" + e);
                System.exit(1);
            }
        }
        String mode = description.get("mode");
        TokenFeatureGenerator gen = new TokenFeatureGenerator(cm.opts.features);
        if(mode.equals("train")){
            return train(description, cm, gen);
        }
        else if(mode.equals("load")){
            return load(description, cm, gen);
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
    private Chunker load(Profile.Section description, ChunkerManager cm, TokenFeatureGenerator gen) throws Exception {
        String store = description.get("store");
        String wrap = description.get(PARAM_WRAP);

        ConsolePrinter.log("--> CRFPP Chunker deserialize from " + store);

        CrfTemplate template = getTemplate(description, cm, gen);
        List<String> features = description.containsKey("features") ? loadUsedFeatures(description.get("features")) : template.getUsedFeatures();
        CrfppChunker chunker = new CrfppChunker(features, wrap);
        chunker.deserialize(store);
        chunker.setTemplate(template);
        
        ConsolePrinter.log("--> CRFPP Chunker deserialize done ");

        return chunker;
    }

    /**
     * Train the chunker and serialize the model to a file.
     * @param description
     * @param cm
     * @return
     * @throws Exception
     */
    private Chunker train(Profile.Section description, ChunkerManager cm, TokenFeatureGenerator gen) throws Exception {
        ConsolePrinter.log("--> CRFPP Chunker train");
        String wrap = description.get(PARAM_WRAP);

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

        ConsolePrinter.log("--> Training on file=" + inputFile);

        CrfTemplate template = getTemplate(description, cm, gen);
        List<String> features = description.containsKey("features") ? loadUsedFeatures(description.get("features")) : template.getUsedFeatures();
        CrfppChunker chunker = new CrfppChunker(threads, types, features, wrap);

        chunker.setTemplate(template);

        chunker.setTrainingDataFilename(description.get("store-training-data"));
        chunker.setModelFilename(modelFilename);

        for(Document document: trainData)
             chunker.addTrainingData(document);
        chunker.train();

        return chunker;
    }

    private List<String> loadUsedFeatures(String file) throws IOException {
        ArrayList<String> usedFeatures = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = reader.readLine();
        while(line != null){
            if(!(line.isEmpty() || line.startsWith("#"))){
                usedFeatures.add(line.trim());
            }
            line = reader.readLine();
        }
        return usedFeatures;
    }

    private CrfTemplate getTemplate(Ini.Section description, ChunkerManager cm, TokenFeatureGenerator gen) throws Exception {
        String templateData = description.get("template");

        String chunkerName = description.getName().substring(8);
        CrfTemplate template = cm.getChunkerTemplate(chunkerName);
        if(template != null){
            template.setAttributeIndex(gen.getAttributeIndex());
        }
        else if(!templateData.equals("null")){
            template = TemplateFactory.parseTemplate(templateData);


            template.setAttributeIndex(gen.getAttributeIndex());
        }
        return template;
    }

}
