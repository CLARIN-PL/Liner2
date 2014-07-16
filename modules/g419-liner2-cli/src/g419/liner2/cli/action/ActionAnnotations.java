package g419.liner2.cli.action;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.writer.AnnotationArffWriter;
import g419.corpus.io.writer.WriterFactory;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.Document;
import g419.liner2.api.LinerOptions;
import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.factory.ChunkerFactory;
import g419.liner2.api.chunker.factory.ChunkerManager;
import g419.liner2.api.features.AnnotationFeatureGenerator;
import g419.liner2.api.features.TokenFeatureGenerator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by michal on 7/16/14.
 */
public class ActionAnnotations extends Action {
    @Override
    public void run() throws Exception {
        LinerOptions.getGlobal().setDefaultDataFormats("ccl", "ccl");
        HashSet<Pattern> typePatterns = LinerOptions.getGlobal().getTypes();
        List<String> annFeatures = parseFeaturesFile(LinerOptions.getGlobal().annotationFeatures);
        AnnotationFeatureGenerator annGen = new AnnotationFeatureGenerator(annFeatures);

        AbstractDocumentReader reader = LinerOptions.getGlobal().getInputReader();
        TokenFeatureGenerator gen = null;

        if (!LinerOptions.getGlobal().features.isEmpty()){
            gen = new TokenFeatureGenerator(LinerOptions.getGlobal().features);
        }

		/* Create all defined chunkers. */
        ChunkerManager cm = ChunkerFactory.loadChunkers(LinerOptions.getGlobal());
        Chunker chunker = cm.getChunkerByName(LinerOptions.getGlobal().getOptionUse());

        AnnotationArffWriter writer = WriterFactory.get().getArffAnnotationWriter(LinerOptions.getGlobal().getOption(LinerOptions.OPTION_OUTPUT_FILE) ,annFeatures);

        Document ps = reader.nextDocument();
        while ( ps != null ){
            if ( gen != null )
                gen.generateFeatures(ps);
            if( chunker != null){
                chunker.chunkInPlace(ps);
            }
            for(AnnotationSet annotations: ps.getChunkings().values()){
                for(Annotation ann: annotations.chunkSet()){
                    if(!typePatterns.isEmpty()){
                        for(Pattern patt: typePatterns){
                            if(patt.matcher(ann.getType()).find()){
                                writer.writeAnnotation(ann.getType(), annGen.generate(ann));
                            }
                        }
                    }
                    else{
                        writer.writeAnnotation(ann.getType(), annGen.generate(ann));
                    }

                }
            }


            ps = reader.nextDocument();
        }
        writer.close();
        reader.close();
    }

    private List<String> parseFeaturesFile(String path) throws IOException{
        List<String> annotationFeatures = new ArrayList<String>();
        BufferedReader br = new BufferedReader(new FileReader(path));
        String line = br.readLine();
        while(line != null) {
            annotationFeatures.add(line);
            line = br.readLine();
        }
        return annotationFeatures;
    }
}
