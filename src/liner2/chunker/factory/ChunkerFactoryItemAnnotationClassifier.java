package liner2.chunker.factory;

import java.io.BufferedReader;
import java.io.File;
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
import org.ini4j.Ini;

public class ChunkerFactoryItemAnnotationClassifier extends ChunkerFactoryItem {

	public ChunkerFactoryItemAnnotationClassifier() {
		super("classifier:([^:]*)(:base=(.*))?");
	}

	@Override
	public Chunker getChunker(String description, ChunkerManager cm) throws Exception {
		Main.log("Training annotation classifier");

		Matcher m = this.pattern.matcher(description);
		
		if ( !m.find() )
			return null;
        String iniPath = m.group(1);
        String iniDir = new File(iniPath).getParent();

        String inputClassifier = m.group(3);
        Ini ini = new Ini(new FileReader(iniPath));
        Ini.Section main = ini.get("main");
        Ini.Section classifierDesc = ini.get("classifier");
        Ini.Section dataDesc = ini.get("data");
        Ini.Section featuresDesc = ini.get("features");

        Chunker baseChunker = null;

		if ( inputClassifier != null ){
			baseChunker = cm.getChunkerByName(inputClassifier);
			if (baseChunker == null)
				throw new ParameterException("Annotation Classifier: undefined base chunker: " + inputClassifier);
		}

        List<String> features = new ArrayList<String>();
        for(String featureName: featuresDesc.keySet())
        features.add(featuresDesc.get(featureName).replace("{INI_DIR}",iniDir));
        String[] parameters;
        if(classifierDesc.containsKey("parameters")){
            parameters = classifierDesc.get("parameters").split(",");
        }
        else{
            parameters = new String[0];
        }
        for(String p: parameters)
        System.out.println(p);
		AnnotationClassifierChunker chunker = new AnnotationClassifierChunker(baseChunker, features, classifierDesc.get("type"), parameters, classifierDesc.get("strategy"));

        String mode = main.get("mode");
        String modelPath = main.get("store").replace("{INI_DIR}", iniDir);
        File modelFile = new File(modelPath);
        if ( mode.equals("load") && modelFile.exists()){
            chunker.deserialize(modelPath);
        }
        else {
            String inputFormat = dataDesc.get("format");
            String inputFile = dataDesc.get("source").replace("{INI_DIR}",iniDir);

            if ((inputFormat.equals("iob")) || (inputFormat.equals("ccl"))) {
                Main.log("--> Training on file=" + inputFile);
                StreamReader reader = ReaderFactory.get().getStreamReader(inputFile, inputFormat);
                ((TrainableChunkerInterface)chunker).train(reader.readParagraphSet());
            }
            else {
                Main.log("--> Training on corpus=" + inputFile);
                ((TrainableChunkerInterface)chunker).train(CorpusFactory.get().query(inputFile));
            }
            modelFile.createNewFile();
            chunker.serialize(modelPath);
        }
		return chunker;
	}

}
