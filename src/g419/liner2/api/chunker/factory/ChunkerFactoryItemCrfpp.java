package g419.liner2.api.chunker.factory;


import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.CrfTemplate;
import g419.corpus.structure.Document;
import g419.liner2.api.Liner2;
import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.CrfppChunker;
import g419.liner2.api.features.TokenFeatureGenerator;
import g419.liner2.api.tools.Logger;
import g419.liner2.api.tools.TemplateFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.regex.Matcher;

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
                System.load(linerJarPath.replace("liner2.jar","") + "lib/libCRFPP.so");
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

    private Chunker load(Ini ini) throws IOException {
        Ini.Section main = ini.get("main");
        String store = main.get("store").replace("{INI_PATH}", ini.getFile().getParent());

        Logger.log("--> CRFPP Chunker deserialize from " + store);

        CrfppChunker chunker = new CrfppChunker();
        chunker.deserialize(store);

        return chunker;
    }

    private Chunker train(Ini ini, ChunkerManager cm) throws Exception {
        Logger.log("--> CRFPP Chunker train");
        String iniDir = ini.getFile().getParent();
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

        Logger.log("--> Training on file=" + inputFile);
        AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(inputFile, inputFormat);

        TokenFeatureGenerator gen = new TokenFeatureGenerator(cm.opts.features);

        String templateName = main.get("template");
        CrfTemplate template = cm.opts.getTemplate(templateName);
        File templateFile = File.createTempFile("template", ".tpl");

        CrfppChunker chunker = new CrfppChunker(threads, types);
        chunker.setTemplateFilename(templateFile.getAbsolutePath());
        chunker.setModelFilename(modelFilename);

        Document document = reader.nextDocument();
        while ( document != null ){
            gen.generateFeatures(document);
             chunker.addTrainingData(document);
            document = reader.nextDocument();
        }
        TemplateFactory.store(template, templateFile.getAbsolutePath(), gen.getAttributeIndex());
        chunker.train();

        return chunker;
    }

}
