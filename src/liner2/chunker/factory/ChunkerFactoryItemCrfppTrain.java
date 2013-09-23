package liner2.chunker.factory;

import java.io.File;
import java.io.FileReader;
import java.util.regex.Matcher;

import liner2.LinerOptions;
import liner2.Main;

import liner2.chunker.CrfppChunker;
import liner2.chunker.Chunker;
import liner2.chunker.TrainableChunkerInterface;

import liner2.features.TokenFeatureGenerator;
import liner2.reader.ReaderFactory;
import liner2.reader.StreamReader;

import liner2.structure.ParagraphSet;
import liner2.tools.CorpusFactory;
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
            String modelFilename = main.get("store");

            ParagraphSet ps;
            if ((inputFormat.equals("iob")) || (inputFormat.equals("ccl"))) {
            	Main.log("--> Training on file=" + inputFile);            
            	StreamReader reader = ReaderFactory.get().getStreamReader(inputFile, inputFormat);
                ps = reader.readParagraphSet();
            }
            else {
            	Main.log("--> Training on corpus=" + inputFile);
                ps = CorpusFactory.get().query(inputFile);
            }
            TokenFeatureGenerator gen = new TokenFeatureGenerator(cm.opts.features);
            gen.generateFeatures(ps);

            String templateName = main.get("template");
            Template template = cm.opts.getTemplate(templateName);
            File templateFile = File.createTempFile("template", ".tpl");
            TemplateFactory.store(template, templateFile.getAbsolutePath(), ps.getAttributeIndex());

            CrfppChunker chunker = new CrfppChunker(threads);
            chunker.setTemplateFilename(templateFile.getAbsolutePath());
            chunker.setModelFilename(modelFilename);

            ((TrainableChunkerInterface) chunker).train(ps);
                        
            return chunker;		
		}
		else		
			return null;
	}

}
