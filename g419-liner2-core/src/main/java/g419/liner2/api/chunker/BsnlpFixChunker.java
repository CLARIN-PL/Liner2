package g419.liner2.api.chunker;

import g419.corpus.schema.kpwr.KpwrNer;
import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;

import java.util.HashMap;
import java.util.Map;

/**
 * Motody do korekcji typowych błędów popełnianych przez model statystyczny.
 * 
 * @author Michał Marcińczuk
 *
 */
public class BsnlpFixChunker extends Chunker {

	private static final String attrBase = "base";
	private static final String attrOrth = "orth";
	private static final String attrCtag = "ctag";

    public BsnlpFixChunker() {
    }
    
    /**
     * 
     */
	@Override
	public HashMap<Sentence, AnnotationSet> chunk(Document ps) {
		for (Sentence sentence : ps.getSentences()){
            this.processSentence(sentence);
		}
		return ps.getChunkings();
	}
	
	private void processSentence(Sentence sentence){
		Map<String, Annotation> anNations = new HashMap<String, Annotation>();
		for ( Annotation an : sentence.getChunks() ){
			if ( KpwrNer.NER_ORG_NATION.equals(an.getType()) ){
				String key = String.format("%d:%d", an.getBegin(), an.getEnd());
				anNations.put(key, an);
			}
		}
		for ( Annotation an : sentence.getChunks() ){
			if ( "bsnlp2017_org".equals(an.getType()) ) { 
				String key = String.format("%d:%d", an.getBegin(), an.getEnd());
				Annotation nation = anNations.get(key);
				if ( nation != null ){
					an.setType( "bsnlp2017_per" );
				}
			}
		}
	}
    	
}
