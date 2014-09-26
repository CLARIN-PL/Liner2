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

public class ChunkerFactoryItemCrfpp extends ChunkerFactoryItem {

	public ChunkerFactoryItemCrfpp() {
		super("crfpp:([^:]*)");
	}

	@Override
	public Chunker getChunker(String description, ChunkerManager cm) throws Exception {
        if (!cm.opts.libCRFPPLoaded){
            try {
                String linerJarPath = Liner2.class.getProtectionDomain().getCodeSource().getLocation().getPath();
                System.load(linerJarPath.replace("g419-liner2-api.jar","") + "libCRFPP.so");
            } catch (UnsatisfiedLinkError e) {
                System.err.println("Cannot load the libCRFPP.so native code.\nIf you are using liner as an imported jar specify correct path as CRFlib parameter in config.\n" + e);
                System.exit(1);
            }
        }
       	Matcher matcherCRFPP = this.pattern.matcher(description);
		if (matcherCRFPP.find()){
            String iniPath = matcherCRFPP.group(1);
            Ini ini = new Ini(new File(iniPath));
            String mode = ini.get("main", "mode");
            if(mode.equals("train")){
                return train(ini, cm);
            }
            else if(mode.equals("load")){
                return load(ini);
            }
            else{
                throw new Exception("Unrecognized mode for CRFPP chunker: " + mode + "(Valid: train/load)");
            }

		}
		else		
			return null;
	}

	/**
	 * Load the model from a file.
	 * @param ini
	 * @return
	 * @throws IOException
	 */
    private Chunker load(Ini ini) throws IOException {
        Ini.Section main = ini.get("main");
        String store = main.get("store").replace("{INI_PATH}", ini.getFile().getParent());

        Logger.log("--> CRFPP Chunker deserialize from " + store);

        CrfppChunker chunker = new CrfppChunker();
        chunker.deserialize(store);

        return chunker;
    }

    /**
     * Train the chunker and serialize the model to a file.
     * @param ini
     * @param cm
     * @return
     * @throws Exception
     */
    private Chunker train(Ini ini, ChunkerManager cm) throws Exception {
        Logger.log("--> CRFPP Chunker train");
        String iniDir = ini.getFile().getParent();
        Ini.Section main = ini.get("main");
        Ini.Section dataDesc = ini.get("data");

        Converter trainingDataConverter = null;
        if ( dataDesc.containsKey("training-data-converter") ){
        	System.out.println(dataDesc.get("training-data-converter"));
        	ArrayList<String> converters = new ArrayList<String>();
        	for ( String in : dataDesc.get("training-data-converter").replace("{INI_PATH}", iniDir).split(","))
        		converters.add(in);
        	trainingDataConverter = ConverterFactory.createPipe(converters);
        }
        
        int threads = Integer.parseInt(main.get("threads"));
        String inputFile = dataDesc.get("source").replace("{INI_PATH}", iniDir);
        String inputFormat;
        String modelFilename = main.get("store").replace("{INI_PATH}", iniDir);

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
            inputFormat = dataDesc.get("format");
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
        if ( dataDesc.containsKey("types")) {
            types = LinerOptions.getGlobal().parseTypes(dataDesc.get("types").replace("{INI_PATH}", iniDir));
        }

        Logger.log("--> Training on file=" + inputFile);

        String templateData = main.get("template").replace("{INI_PATH}", iniDir);
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
