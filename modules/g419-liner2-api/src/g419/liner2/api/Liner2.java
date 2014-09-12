package g419.liner2.api;



import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.factory.ChunkerManager;
import g419.liner2.api.features.TokenFeatureGenerator;

import java.util.HashMap;

public class Liner2 extends Chunker {

    private LinerOptions opts;
    private Chunker chunker;
    private TokenFeatureGenerator gen;

    public Liner2(String ini){
        opts = new LinerOptions();
        opts.parseModelIni(ini);

        ChunkerManager cm = new ChunkerManager(LinerOptions.getGlobal());
        try {
            cm.loadChunkers();
        } catch (Exception e) {
            System.out.println("Error while creating chunkers:\n");
            e.printStackTrace();
        }
        chunker = cm.getChunkerByName(opts.getOptionUse());

        gen = new TokenFeatureGenerator(opts.features);
    }

    @Override
    public HashMap<Sentence, AnnotationSet> chunk(Document ps) {
        try {
            gen.generateFeatures(ps);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return chunker.chunk(ps);
    }
}
