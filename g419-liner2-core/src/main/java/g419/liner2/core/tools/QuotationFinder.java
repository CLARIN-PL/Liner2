package g419.liner2.core.tools;

import java.util.regex.Pattern;

import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;

/**
 * Rozpoznaje sekwencje tekstu występujące w cudzysłowiu.
 * @author Michał Marcińczuk
 *
 */
public class QuotationFinder{

	public static Pattern patternQuoteSingle = Pattern.compile("^[‘’'‘’‚’]$");
	public static Pattern patternQuoteDouble = Pattern.compile("^[“”\"“””„““”]$");
	public static Pattern patternQuoteOther = Pattern.compile("^[«»]$");

	/**
	 * Sprawdza, czy poczynając od pozycji startPos w zdaniu znajduje się fragment
	 * tekstu otoczony cydzysłowami. Metoda zwraca długość takiego fragmentu.
	 * @param sentence Zdanie, w którym sprawdzane jest wystąpienie cytowania.
	 * @param startPos Indeks tokenu, od którego następuje sprawdzenie.
	 * @return 0 jeżeli od pozycji startPos nie ma cytowania, wpp. długość cytowania razem z cudzysłowami.
	 */
	public QuotationSize isQuotation(Sentence sentence, int startPos){
		if ( startPos >= sentence.getTokenNumber() ){
			return null;
		}
		
		String orth = sentence.getTokens().get(startPos).getOrth();
		boolean singleQuote = patternQuoteSingle.matcher(orth).find();
		boolean doubleQuote = patternQuoteDouble.matcher(orth).find();
		boolean otherQuote = patternQuoteOther.matcher(orth).find();
		
		if (!singleQuote && !doubleQuote && !otherQuote){
			return null;
		}

		boolean openSingle = singleQuote;
		boolean openDouble = doubleQuote;
		boolean openOther = otherQuote;
		int quotationEnded = -1;
		
		for ( int i = startPos+1; i<sentence.getTokens().size() && quotationEnded==-1; i++ ) {
			Token token = sentence.getTokens().get(i);
			singleQuote = patternQuoteSingle.matcher(token.getOrth()).find();
			doubleQuote = patternQuoteDouble.matcher(token.getOrth()).find();
			otherQuote = patternQuoteOther.matcher(token.getOrth()).find();
			
			if ( (openSingle && singleQuote) || (openDouble && doubleQuote) || (openOther && otherQuote) ){
				openSingle = false;
				openDouble = false;
				openOther = false;
				quotationEnded = i;
			}
		}
		
		if ( quotationEnded!=-1 ){
			return new QuotationSize(1, quotationEnded-startPos+1 - 2, 1);
		}
		else{
			return null;
		}
	}

	/**
	 * 
	 * @param sentence
	 * @param pos
	 * @return
	 */
	int isSingleQuotationMark(Sentence sentence, int pos){
		boolean singleQuote = patternQuoteSingle.matcher(sentence.getTokens().get(pos).getOrth()).find();
		if ( !singleQuote ){
			return 0;
		}
		if ( pos + 1 == sentence.getTokens().size() ){
			return 1;
		}
		else if ( patternQuoteSingle.matcher(sentence.getTokens().get(pos+1).getOrth()).find() ){
			// Kolejny znak to pojedynczy znak cytowania, więc z poprzednim tworzą dwuznakowy znak cytowania
			return 0;
		}
		else{
			return 1;
		}
	}
	
	/**
	 * 
	 * @param sentence
	 * @param pos
	 * @return
	 */
	int isDoubleQuotationMark(Sentence sentence, int pos){
		boolean singleQuote = patternQuoteSingle.matcher(sentence.getTokens().get(pos).getOrth()).find();
		boolean doubleQuote = patternQuoteDouble.matcher(sentence.getTokens().get(pos).getOrth()).find();
		if ( doubleQuote ){
			return 1;
		}
		if ( singleQuote
				&& pos + 1 == sentence.getTokens().size()
				&& patternQuoteSingle.matcher(sentence.getTokens().get(pos+1).getOrth()).find() ){
			return 2;
		}
		else {
			return 0;
		}
	}	
	
	/**
	 * 
	 * @param sentence
	 * @param pos
	 * @return
	 */
	int isOtherQuotationMark(Sentence sentence, int pos){
		boolean otherQuote = patternQuoteOther.matcher(sentence.getTokens().get(pos).getOrth()).find();
		return otherQuote ? 1 : 0;
	}
	
}
