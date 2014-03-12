package liner2.chunker.factory;

import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.regex.Matcher;

import liner2.Main;
import liner2.chunker.Chunker;
import liner2.chunker.CrfppChunker;
import liner2.chunker.TrainableChunkerInterface;
import liner2.features.TokenFeatureGenerator;
import liner2.reader.ReaderFactory;
import liner2.reader.AbstractDocumentReader;
import liner2.structure.Document;
import liner2.tools.Template;
import liner2.tools.TemplateFactory;

import org.ini4j.Ini;

public class ChunkerFactoryItemCrfppTrain extends ChunkerFactoryItem {

	public ChunkerFactoryItemCrfppTrain() {
		super("crfpp-train:([^:]*)");
	}

	@Override
	public Chunker getChunker(String description, ChunkerManager cm) throws Exception {

       	Matcher matcherCRFPP = this.pattern.matcher(description);
		if (matcherCRFPP.find()){
            Main.log("--> CRFPP Chunker train");
            String iniPath = matcherCRFPP.group(1);
            String iniDir = new File(iniPath).getParent();

            Ini ini = new Ini(new FileReader(iniPath));
            Ini.Section main = ini.get("main");
            Ini.Section dataDesc = ini.get("data");

            int threads = Integer.parseInt(main.get("threads"));

            String inputFormat = dataDesc.get("format");
            String inputFile = dataDesc.get("source").replace("{INI_PATH}", iniDir);
            String modelFilename = main.get("store").replace("{INI_PATH}", iniDir);
            
            HashSet<String> types = new HashSet<String>();
            if ( dataDesc.containsKey("types") && dataDesc.get("types").length() > 0 )
            	for (String line : dataDesc.get("types").split(","))
            		types.add(line);
            
        	Main.log("--> Training on file=" + inputFile);            
        	AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(inputFile, inputFormat);

            TokenFeatureGenerator gen = new TokenFeatureGenerator(cm.opts.features);

            String templateName = main.get("template");
            Template template = cm.opts.getTemplate(templateName);
            File templateFile = File.createTempFile("template", ".tpl");

            CrfppChunker chunker = new CrfppChunker(threads, types);
            chunker.setTemplateFilename(templateFile.getAbsolutePath());
            chunker.setModelFilename(modelFilename);

            Document document = reader.nextDocument();
            while ( document != null ){
                gen.generateFeatures(document);
            	((TrainableChunkerInterface) chunker).addTrainingData(document);
            	document = reader.nextDocument();
            }
            TemplateFactory.store(template, templateFile.getAbsolutePath(), gen.getAttributeIndex());
            chunker.train();
                        
            return chunker;		
		}
		else		
			return null;
	}

}
