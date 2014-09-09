package g419.liner2.api.chunker.factory;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.BatchReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.CrfTemplate;
import g419.corpus.structure.Document;
import g419.liner2.api.Liner2;
import g419.liner2.api.LinerOptions;
import g419.liner2.api.chunker.AnnotationCRFClassifierChunker;
import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.CrfppChunker;
import g419.liner2.api.features.TokenFeatureGenerator;
import g419.liner2.api.tools.Logger;
import g419.liner2.api.tools.TemplateFactory;
import org.ini4j.Ini;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
/**
 * Created by michal on 9/2/14.
 */
public class ChunkerFactoryItemAnnotationCRFClassifier extends ChunkerFactoryItem {

    public ChunkerFactoryItemAnnotationCRFClassifier() {
        super("CRFclassifier:([^:]*)");
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
        Matcher matcher = this.pattern.matcher(description);
        if (matcher.find()){
            String iniPath = matcher.group(1);
            Ini ini = new Ini(new File(iniPath));
            String mode = ini.get("main", "mode");
            if(mode.equals("train")){
                return train(ini, cm);
            }
            else if(mode.equals("load")){
                return load(ini);
            }
            else{
                throw new Exception("Unrecognized mode for CRFPP annotation classifier: " + mode + "(Valid: train/load)");
            }

        }
        else
            return null;
    }

    private Chunker load(Ini ini) {

        Ini.Section main = ini.get("main");
        String store = main.get("store").replace("{INI_PATH}", ini.getFile().getParent());

        Logger.log("--> CRFPP Chunker deserialize from " + store);
        CrfppChunker baseChunker = new CrfppChunker();
        baseChunker.deserialize(store);
        AnnotationCRFClassifierChunker chunker = new AnnotationCRFClassifierChunker(null, main.get("base"), baseChunker);

        return chunker;
    }

    private Chunker train(Ini ini, ChunkerManager cm) throws Exception {
        Logger.log("--> CRFPP annotation classifier train");
        String iniDir = ini.getFile().getParent();
        Ini.Section main = ini.get("main");
        Ini.Section dataDesc = ini.get("data");

        String inputFile = dataDesc.get("source").replace("{INI_PATH}", iniDir);
        String inputFormat;
        AbstractDocumentReader reader;
        String modelFilename = main.get("store").replace("{INI_PATH}", iniDir);

        if(inputFile.equals("{CV_TRAIN}")){
            inputFormat = LinerOptions.getGlobal().getOption("cvFormat");
            reader = new BatchReader(IOUtils.toInputStream(LinerOptions.getGlobal().getOption("cvData")), "", inputFormat);
        }
        else{
            inputFormat = dataDesc.get("format");
            reader = ReaderFactory.get().getStreamReader(inputFile, inputFormat);
        }
//        System.out.println("TYPES: "+dataDesc.get("types"));
        List<Pattern> list = LinerOptions.getGlobal().parseTypes(dataDesc.get("types").replace("{INI_PATH}", iniDir));

        CrfppChunker baseChunker = new CrfppChunker(Integer.parseInt(main.get("threads")), list);
        baseChunker.setModelFilename(modelFilename);
        String templateData = main.get("template").replace("{INI_PATH}", iniDir);
        Logger.log("--> Training on file=" + inputFile);

        AnnotationCRFClassifierChunker chunker = new AnnotationCRFClassifierChunker(list, main.get("base"), baseChunker);

        TokenFeatureGenerator gen = new TokenFeatureGenerator(cm.opts.features);

        CrfTemplate template = TemplateFactory.parseTemplate(templateData);
        template.addFeature("context:"+main.get("context"));

        Document document = reader.nextDocument();
        while ( document != null ){
            gen.generateFeatures(document);
            Document wrapped = chunker.prepareData(document, "train");
            System.out.println("DATA TO TRAIN");
            for(AnnotationSet annset: wrapped.getChunkings().values()){
                System.out.println("---------");
                for(Annotation ann: annset.chunkSet()){
                    System.out.println(ann.getType()+" "+ann.getText());
                }
            }
            baseChunker.addTrainingData(wrapped);
            if(template.getAttributeIndex() == null){
                template.setAttributeIndex(wrapped.getAttributeIndex());
            }
            document = reader.nextDocument();
        }
        baseChunker.setTemplate(template);
        baseChunker.train();
        System.out.println("SERIALIZED");

        return chunker;

    }
}
