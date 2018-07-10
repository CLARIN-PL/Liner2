package g419.liner2.core.chunker;

import g419.corpus.structure.*;
import g419.liner2.core.tools.QuotationFinder;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Motody do korekcji typowych błędów popełnianych przez model statystyczny.
 * @author czuk
 *
 */
public class CrfppFix extends Chunker {

	/* Lista analizowanych typów anotacji */ 
	private List<Pattern> types = new LinkedList<Pattern>();

	private static final String attrBase = "base";
	private static final String attrOrth = "orth";
	private static final String attrCtag = "ctag";

    public CrfppFix() {
		// TODO Przenieść jako parametr
		this.types.add(Pattern.compile("nam"));
    }
    
    /**
     * 
     */
	@Override
	public HashMap<Sentence, AnnotationSet> chunk(Document ps) {
		for (Sentence sentence : ps.getSentences()){
            this.ruleNamFixQuotation(sentence);
		}
		return ps.getChunkings();
	}
    
	/**
	 * Koryguje zakres anotacji z cudzysłowami.
	 * a) dla anotacji zakończonej cudzysłowem -- sprawdza, czy anotacja zawiera cudzysłów otwierający.
	 *    Jeżeli nie zawiera, to cudzysłów zamykający jest usuwany.
	 * b) dla anotacji, po której występuje cudzysłów -- sprawdza, czy anotacja zawiera cudzysłów otwierający.
	 *    Jeżeli tak, to cudzysłów zamykający dodawany jest do anotacji.
	 * @param sentence Zdanie, na którym zostaną wykonane reguły korygujące
	 */
	private void ruleNamFixQuotation(Sentence sentence) {
		List<Token> tokens = sentence.getTokens();
		for (Annotation an : sentence.getAnnotations(this.types)){
			
			/* Anotacja kończy się cydzysłowem -- sprawdz, czy w anotacji jest cudzysłów otwierający */
			if ( QuotationFinder.patternQuoteDouble.matcher(
					tokens.get(an.getEnd()).getAttributeValue(attrOrth)).find()){
				int j = an.getEnd();
				boolean opening = false;
				while ( !opening && j>=an.getBegin() ){
					opening = QuotationFinder.patternQuoteDouble.matcher(
							tokens.get(j).getAttributeValue(attrOrth)).find();
					j--;
				}
				if (!opening){
					if (an.getTokens().size() == 1)
						sentence.getChunks().remove(an);
					else
						an.replaceTokens(an.getBegin(), an.getEnd()-1);
				}					
			}
			/* Za anotacją jest cudzysłów, sprawdź, czy wewnątrz anotacji jest cudzysłów otwierający */
			else if ( an.getEnd()+1 < tokens.size()
						&& QuotationFinder.patternQuoteDouble.matcher(
								tokens.get(an.getEnd()+1).getAttributeValue(attrOrth)).find()){
				int j = an.getEnd();
				boolean opening = false;
				while ( !opening && j>=an.getBegin() ){
					opening = QuotationFinder.patternQuoteDouble.matcher(
							tokens.get(j).getAttributeValue(attrOrth)).find();
					j--;
				}
				if (opening)
					an.replaceTokens(an.getBegin(), an.getEnd()+1);		
			}
		}
	}  
	
}
