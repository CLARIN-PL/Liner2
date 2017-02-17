package g419.liner2.api.chunker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

import g419.corpus.schema.kpwr.KpwrNer;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Tag;
import g419.corpus.structure.Token;
import g419.liner2.api.tools.NeLemmatizer;

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
	
	Map<String, String> renameRules = new HashMap<String, String>();
	
	public static final String BSNLP_PREFIX = "bsnlp2017";
	public static final String BSNLP_PER = "bsnlp2017_per";
	public static final String BSNLP_LOC = "bsnlp2017_loc";
	public static final String BSNLP_ORG = "bsnlp2017_org";
	public static final String BSNLP_MISC = "bsnlp2017_misc";

	private boolean cleanup = false;
	
	NeLemmatizer lemmatizer = null;
	
	/**
	 * 
	 * @param cleanup if true, then annotations other than bsnlp2017_* are removed from documents.
	 */
    public BsnlpFixChunker(NeLemmatizer lemmatizer, boolean cleanup) {
    	renameRules.put(KpwrNer.NER_ORG_NATION, BSNLP_PER);
    	renameRules.put(KpwrNer.NER_PRO_MEDIA_RADIO, BSNLP_ORG);
    	this.cleanup = cleanup;
    	this.lemmatizer = lemmatizer;
    }
    
    /**
     * 
     */
	@Override
	public HashMap<Sentence, AnnotationSet> chunk(Document ps) {
		for (Sentence sentence : ps.getSentences()){
            this.processSentence(sentence);
            this.filterByConfidence(sentence, 0.4);
            this.joinOrgWithLoc(sentence);
            this.ruleNamJoinByHyphen(sentence);
		}
		
		for ( Annotation an : ps.getAnnotations() ){
			if ( an.getType().startsWith("bsnlp2017_") ){
				while ( this.ruleExpandAnnotationToLeft(ps, an)) {}
			}
		}
		
		this.renameByMaxConfidence(ps);
		this.renamePersonNames(ps);
		
		if ( cleanup ){
			this.cleanup(ps);
		}
		
		this.lemmatize(ps);
		
		return ps.getChunkings();
	}
	
	private void cleanup(Document ps) {
		List<Annotation> toRemove = new LinkedList<Annotation>();
		for ( Annotation an : ps.getAnnotations() ){
			if ( !an.getType().startsWith(BSNLP_PREFIX) ){
				toRemove.add(an);
			}
		}
		ps.removeAnnotations(toRemove);
	}

	/**
	 * For annotations with the same orth set the same category, which has the highest confidence among those annotations. 
	 * @param ps
	 */
	private void renameByMaxConfidence(Document ps) {
		Map<String, Annotation> maxConfidence = new HashMap<String, Annotation>();
		for ( Annotation an : ps.getAnnotations() ){
			if ( an.getType().startsWith("bsnlp2017_") ){
				if ( maxConfidence.containsKey(an.getText()) ){
					if ( an.getConfidence() > maxConfidence.get(an.getText()).getConfidence() ){
						maxConfidence.put(an.getText(), an);
					}
				} else{
					maxConfidence.put(an.getText(), an);
				}
			}
		}
		for ( Annotation an : ps.getAnnotations() ){
			if ( an.getType().startsWith("bsnlp2017_") ){
				Annotation confidence = maxConfidence.get(an.getText());
				if ( confidence != null && confidence.getConfidence() > an.getConfidence() ){
					an.setType(confidence.getType());
					an.setConfidence(confidence.getConfidence());
				}
			}
		}
	}

	/**
	 * 
	 * @param ps
	 */
	private void renamePersonNames(Document ps) {
		Map<String, Double> personWithConfidence = new HashMap<String, Double>();
		for ( Annotation an : ps.getAnnotations() ){
			if ( BSNLP_PER.equals(an.getType()) && an.getTokens().size() > 1 ){
				personWithConfidence.put(an.getText(), an.getConfidence());
				for ( int index : an.getTokens() ){
					personWithConfidence.put(an.getSentence().getTokens().get(index).getOrth(), an.getConfidence());
				}
			}
		}
		
		for ( Annotation an : ps.getAnnotations() ){
			if ( !BSNLP_PER.equals(an.getType()) ){
				Double confidence = personWithConfidence.get(an.getText());
				if ( confidence != null && confidence > an.getConfidence() ){
					an.setType(BSNLP_PER);
					//an.setConfidence(confidence);
				}
			}
		}
	}

	private void processSentence(Sentence sentence){
		Map<String, Annotation> bsnlp = new HashMap<String, Annotation>();
		for ( Annotation an : sentence.getChunks() ){
			if ( an.getType().startsWith("bsnlp2017_") ) { 
				String key = String.format("%d:%d", an.getBegin(), an.getEnd());
				bsnlp.put(key, an);
			}
		}
		
		for ( Annotation an : sentence.getChunks() ){
			String renameTo = this.renameRules.get(an.getType());
			String key = String.format("%d:%d", an.getBegin(), an.getEnd());
			Annotation anBsnlp = bsnlp.get(key);
			if ( renameTo != null && anBsnlp != null ){
				anBsnlp.setType(renameTo);
			}
		}
	}
	
	private void filterByConfidence(Sentence sentence, double minConfidence){
		Set<Annotation> toRemove = new HashSet<Annotation>();
		for ( Annotation an : sentence.getChunks() ){
			if ( an.getConfidence() < minConfidence ){
				toRemove.add(an);
			}
		}
		sentence.getChunks().removeAll(toRemove);
	}
	
	private void joinOrgWithLoc(Sentence sentence){
		Map<String, Annotation> bsnlp = new HashMap<String, Annotation>();
		for ( Annotation an : sentence.getChunks() ){
			if ( an.getType().startsWith("bsnlp2017_") ) { 
				String key = String.format("%d:%s", an.getBegin(), an.getType());
				bsnlp.put(key, an);
			}
		}

		List<Annotation> toRemove = new LinkedList<Annotation>();
		List<Annotation> newAnns = new LinkedList<Annotation>();
		for ( Annotation an : sentence.getChunks() ){
			if ( BSNLP_ORG.equals(an.getType()) && an.getEnd()+2 < sentence.getTokenNumber() ){
				String key = String.format("%d:%s", an.getEnd()+2, BSNLP_LOC);
				Annotation loc = bsnlp.get(key);
				if ( "w".equals(sentence.getTokens().get(an.getEnd()+1).getDisambTag().getBase())
						&& loc != null ){
					newAnns.add(new Annotation(an.getBegin(), loc.getEnd(), BSNLP_ORG, sentence));
					toRemove.add(an);
					toRemove.add(loc);
				}
			}
		}
		
		sentence.getChunks().removeAll(toRemove);
		sentence.getChunks().addAll(newAnns);
	}

	/**
	 * Łączy dwie anotacje nam, które połączone są dywizem bez spacji.
	 * @param sentence Obiekt zdania, na którym zostaną wykonane reguły.
	 */
	private void ruleNamJoinByHyphen(Sentence sentence){
		Map<Integer, List<Annotation>> annotataionIndex = new HashMap<Integer, List<Annotation>>();
		
		for (Annotation an : sentence.getChunks()){
			if ( an.getType().startsWith("bsnlp2017_") ){
				if ( !annotataionIndex.containsKey(an.getBegin()) )
					annotataionIndex.put(an.getBegin(), new ArrayList<Annotation>());
				annotataionIndex.get(an.getBegin()).add(an);
			}
		}
		
		List<Annotation> toRemove = new LinkedList<Annotation>();
		for (Annotation an : sentence.getChunks() ){
			if ( an.getType().startsWith("bsnlp2017_")
					&& an.getEnd()+1 < sentence.getTokenNumber()
					&& sentence.getTokens().get(an.getEnd()+1).getOrth().equals("-")
					&& sentence.getTokens().get(an.getEnd()).getNoSpaceAfter()
					&& sentence.getTokens().get(an.getEnd()+1).getNoSpaceAfter()
					&& annotataionIndex.containsKey(an.getEnd()+2)){
				for (Annotation an2 : annotataionIndex.get(an.getEnd()+2)){
					for (int idx : an.getTokens())
						an2.addToken(idx);
					an2.addToken(an.getEnd()+1);
				}
				toRemove.add(an);
			}
		}
		sentence.getChunks().removeAll(toRemove);
	}	
	
	/**
	 * Sprawdza, czy wskazana anotacja może być rozszerzona o jeden token w lewo. 
	 * Anotacja może być rozszerzona o token w lewo, jeżeli wszystkie wystąpienia tej anotacji
	 * w dokumencie są poprzedzone tym samym tokenem (o tej samej formie bazowej i pisane z dużej litery).
	 * @param document dokument, w oparciu o który dokonywana jest analiza wystąpień
	 * @param an anotacja do rozszerzenia
	 * @return true jeżeli anotacja została rozszerzona
	 */
	private boolean ruleExpandAnnotationToLeft(Document document, Annotation an){
		if ( an.getBegin() == 0 ){
			return false;
		}
		String baseBefore = an.getSentence().getTokens().get(an.getBegin()-1).getDisambTag().getBase();
		int foundAtBeginning = 0;
		int foundNotBeginning = 0;
		
		for ( Sentence sentence : document.getSentences() ){
			int i=1;
			while ( i < sentence.getTokenNumber() ){
				if ( this.matchAnnotationByBase(sentence, i, an) ){
					if ( sentence.getTokens().get(i-1).hasBase(baseBefore, true)
						&& Character.isUpperCase(sentence.getTokens().get(i-1).getOrth().charAt(0)) ){
						if ( i-1 == 0 ){
							foundAtBeginning++;
						}
						else{
							foundNotBeginning++;
						}
					}
					else{
						// Znaleziono wystąpienie nazwy, które jest poprzedzone innym tokenem niż token, o który anotacja ma być rozserzona.
						return false;
					}
				}
				i++;
			}
		}
		if ( foundAtBeginning + foundNotBeginning > 1 && foundNotBeginning > 1){
			an.addToken(an.getBegin()-1);
			return true;
		}
		else{
			return false;
		}
	}
	
	/**
	 * Sprawdza, czy od pozycji pos w zdaniu sentence znajduje się sekwencja tokenów o takich samych
	 * formach bazowych co anotacja an z dokładnością do wielkości liter.
	 *
	 * @param sentence the sentence
	 * @param pos the pos
	 * @param an the an
	 * @return true, if successful
	 */
	private boolean matchAnnotationByBase(Sentence sentence, int pos, Annotation an){
		int i=0;
		Sentence anSentence = an.getSentence();
		for ( int anToken : an.getTokens() ){
			if ( pos + i < sentence.getTokenNumber()
					&& this.equalsTokenBasesAndOrthCase(sentence.getTokens().get(pos+i), anSentence.getTokens().get(anToken))){
				i++;
			}
			else{
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Sprawdza, czy wskazane tokny mają przynajmniej jeden wspólny base oraz ten sam zapis 
	 * (tj. czy oba pisane z dużej lub małej litery).
	 *
	 * @param t1 the t 1
	 * @param t2 the t 2
	 * @return true, jeżeli tokeny mają przynajmniej jeden wspólny base i mają ten sam zapis (wielkość pierwszego znaku)
	 */
	private boolean equalsTokenBasesAndOrthCase(Token t1, Token t2){
		return ( Sets.intersection(t1.getDisambBases(), t2.getDisambBases()).size() > 0
				&& Character.isUpperCase(t1.getOrth().charAt(0)) == Character.isUpperCase(t2.getOrth().charAt(0)));
	}
	
	/**
	 * Lematyzacja nazw własnych.
	 * @param ps
	 */
	private void lemmatize(Document ps){		
		for ( Annotation an : ps.getAnnotations() ){
			this.lemmatize(an);
		}
	}
	
	/**
	 * Bezkontekstowa lematyzacja pojedynczej nazwy.
	 * @param an
	 */
	private void lemmatize(Annotation an){		
		if ( this.lemmatizer != null ){
			String lemma = this.lemmatizer.lemmatize(an.getText());
			if ( lemma != null ){
				an.setLemma(lemma);
				return;
			} else if ( BSNLP_PER.equals(an.getType()) ){
				lemma = this.lemmatizer.lemmatizePersonName(an.getText());
				if ( lemma != null ){
					an.setLemma(lemma);
					return;
				}
			}
		}

		if ( an.getTokenTokens().size() == 1 ){
			Token token = an.getSentence().getTokens().get(an.getBegin()); 
			for ( Tag tag : token.getDisambTags() ){
				if ( Character.isUpperCase(tag.getBase().charAt(0) ) ){
					// Jednoelementowa nazwa, której forma bazowa jest z dużej litery
					an.setLemma(tag.getBase());
					return;
				}
			}
		}

		an.setLemma("UNKNOWN");
	}
}
