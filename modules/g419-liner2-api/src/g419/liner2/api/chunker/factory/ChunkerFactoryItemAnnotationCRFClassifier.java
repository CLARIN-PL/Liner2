package g419.liner2.api.chunker.factory;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.*;
import g419.liner2.api.Liner2;
import g419.liner2.api.LinerOptions;
import g419.liner2.api.chunker.AnnotationCRFClassifierChunker;
import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.CrfppChunker;
import g419.liner2.api.features.TokenFeatureGenerator;
import g419.corpus.Logger;
import g419.liner2.api.tools.TemplateFactory;
import org.ini4j.Ini;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by michal on 9/2/14.
 */
public class ChunkerFactoryItemAnnotationCRFClassifier extends ChunkerFactoryItem {

    public ChunkerFactoryItemAnnotationCRFClassifier() {
        super("CRFclassifier");
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
            return load(description, cm);
        }
        else{
            throw new Exception("Unrecognized mode for CRFPP annotation classifier: " + mode + "(Valid: train/load)");
        }
    }

    private List<String> parseAnnotationFeatures(String filePath) throws IOException {
        List<String> features = new ArrayList<String>();
        if(filePath != null) {
            File featuresFile = new File(filePath);
            if (!featuresFile.exists()) {
                throw new FileNotFoundException("Error while parsing features:" + filePath + " is not an existing file!");
            }
            String iniPath = featuresFile.getAbsoluteFile().getParentFile().getAbsolutePath();
            BufferedReader br = new BufferedReader(new FileReader(featuresFile));
            StringBuffer sb = new StringBuffer();
            String feature = br.readLine();
            while (feature != null) {
                if (!feature.isEmpty() && !feature.startsWith("#")) {
                    feature = feature.trim().replace("{INI_PATH}", iniPath);
                    features.add(feature);
                }
                feature = br.readLine();
            }
        }
        return features;
    }

    private Chunker load(Ini.Section description, ChunkerManager cm) throws Exception {


        String store = description.get("store");

        Logger.log("--> CRFPP Chunker deserialize from " + store);
        CrfppChunker baseChunker = new CrfppChunker();
        baseChunker.deserialize(store);
        TokenFeatureGenerator gen = new TokenFeatureGenerator(cm.opts.features);
        AnnotationCRFClassifierChunker chunker = new AnnotationCRFClassifierChunker(null, description.get("base-annotation"), baseChunker, gen, parseAnnotationFeatures(description.get("annotationFeatures")), description.get("context"));

        return chunker;
    }

    private Chunker train(Ini.Section description, ChunkerManager cm) throws Exception {
        Logger.log("--> CRFPP annotation classifier train");

        String inputFile = description.get("training-data");
        String inputFormat;

        String modelFilename = description.get("store");
        TokenFeatureGenerator gen = new TokenFeatureGenerator(cm.opts.features);

        ArrayList<Document> trainData = new ArrayList<Document>();
        if(inputFile.equals("{CV_TRAIN}")){
            trainData = cm.trainingData;
        }
        else{
            inputFormat = description.get("format");
            AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(inputFile, inputFormat);
            Document document = reader.nextDocument();
            while ( document != null ){
                gen.generateFeatures(document);
                trainData.add(document);
                document = reader.nextDocument();
            }
        }
        List<Pattern> list = LinerOptions.getGlobal().parseTypes(description.get("types"));

        CrfppChunker baseChunker = new CrfppChunker(Integer.parseInt(description.get("threads")), list);
        baseChunker.setModelFilename(modelFilename);
        String templateData = description.get("template");
        Logger.log("--> Training on file=" + inputFile);



        CrfTemplate template = TemplateFactory.parseTemplate(templateData);
        template.addFeature("context:" + description.get("context"));
        for(String feature: new ArrayList<String>(template.getFeatureNames())){
            if(!(feature.contains("/") || feature.equals("context"))){
                String[] windowDesc = template.getFeatures().get(feature);
                for(int i=1; i < windowDesc.length; i++){
                    template.addFeature(feature + ":" + windowDesc[i] + "/context:0");
                }
            }
        }

        baseChunker.setTemplate(template);
        AnnotationCRFClassifierChunker chunker = new AnnotationCRFClassifierChunker(list, description.get("base-annotation"), baseChunker, gen, parseAnnotationFeatures(description.get("annotation-features")), description.get("context"));

        for(Document document: trainData){
            gen.generateFeatures(document);
            Document wrapped = chunker.prepareData(document, "train");
            baseChunker.addTrainingData(wrapped);
            if(template.getAttributeIndex() == null){
                template.setAttributeIndex(wrapped.getAttributeIndex());
            }
        }
        baseChunker.train();

        return chunker;

    }
}
