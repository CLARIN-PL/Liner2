package liner2.api;

import liner2.LinerOptions;
import liner2.chunker.Chunker;
import liner2.chunker.factory.ChunkerManager;
import liner2.features.TokenFeatureGenerator;
import liner2.structure.AnnotationSet;
import liner2.structure.ParagraphSet;
import liner2.structure.Sentence;

import java.util.HashMap;

public class Liner2 extends Chunker {

    private LinerOptions opts;
    private Chunker chunker;
    private TokenFeatureGenerator gen;

    public Liner2(String ini){
        opts = new LinerOptions();
        opts.loadIni(ini);

        ChunkerManager cm = new ChunkerManager(opts);
        chunker = cm.getChunkerByName(opts.getOptionUse());

        gen = new TokenFeatureGenerator(opts.features);
    }

    @Override
    public HashMap<Sentence, AnnotationSet> chunk(ParagraphSet ps) {
        try {
            gen.generateFeatures(ps);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return chunker.chunk(ps);
    }
}
