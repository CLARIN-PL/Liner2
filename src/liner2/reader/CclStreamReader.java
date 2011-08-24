package liner2.reader;

import liner2.structure.Sentence;

public class CclStreamReader extends StreamReader {

	@Override
	public Sentence readSentence() {
		
		/*
		 * Przy wczytywaniu z CCL-a do indeksu nazw trafią 3 pola:
		 * orth, base i ctag dla analizy oznaczonej jako disamb.
		 * Jeżeli brak, to bierzemy pierwszy z brzegu.
		 * Poza tym, wszystkie analizy morfologiczne mają być dodane
		 * do atrybutu tags.
		 */
		
		// TODO Auto-generated method stub
		return null;
	}

}
