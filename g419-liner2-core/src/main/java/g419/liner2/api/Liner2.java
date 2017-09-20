package g419.liner2.api;

import java.util.Map;

import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.factory.ChunkerManager;
import g419.liner2.api.features.TokenFeatureGenerator;

public class Liner2 extends Chunker {

    private LinerOptions opts = null;
    private Chunker chunker = null;
    private TokenFeatureGenerator gen = null;

    public Liner2(String ini) throws Exception{
        this.opts = new LinerOptions();
        this.opts.parseModelIni(ini);

        ChunkerManager cm = new ChunkerManager(this.opts);
        try {
            cm.loadChunkers();
        } catch (Exception e) {
            System.out.println("Error while creating chunkers:\n");
            e.printStackTrace();
        }
        this.chunker = cm.getChunkerByName(opts.getOptionUse());
        if ( chunker == null ){
        	throw new Exception(
        		String.format("Chunker named '%s' not found in %s", opts.getOptionUse(), ini));
        }

        this.gen = new TokenFeatureGenerator(opts.features);
    }

    @Override
    public Map<Sentence, AnnotationSet> chunk(Document ps) {
        try {
        	this.gen.generateFeatures(ps);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this.chunker.chunk(ps);
    }
}
