package g419.liner2.core.chunker;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;

import g419.corpus.schema.kpwr.KpwrNer;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;
import g419.liner2.core.tools.QuotationFinder;
import g419.liner2.core.tools.QuotationSize;
import g419.liner2.core.tools.TrieDictFinder;
import g419.liner2.core.tools.TrieDictNode;

/**
 * Chunker znajduje tytuły chrematonimów (książki, wystawy, akcje, projekty, itd.) w oparciu o wystąpienie
 * w cudzysłowiu oraz przesłanki w bliskim kontekście wskazujące na klasę obiektu.
 * 
 * @author Michał Marcińczuk
 */
public class RuleTitleChunker extends Chunker {

	private TrieDictFinder prefixBaseFinder = null;
	private TrieDictFinder prefixOrthFinder = null;
	private QuotationFinder quoationFinder = new QuotationFinder();
	
	private Set<String> titleSeparator = new HashSet<String>();
	private String annotationType = KpwrNer.NER;
	
	public RuleTitleChunker(String annotationType, Set<String> prefixes){
		this.annotationType = annotationType;
		
		titleSeparator.add(",");
		titleSeparator.add("i");
		titleSeparator.add("oraz");
		titleSeparator.add("lub");
				
		TrieDictNode titleBasePrefixes = new TrieDictNode(false);		
		for ( String prefix : prefixes ){
			titleBasePrefixes.addPhrase(prefix.split(" "));
		}		
		this.prefixBaseFinder = new TrieDictFinder(titleBasePrefixes);
		
		TrieDictNode titleOrthPrefixes = new TrieDictNode();
		titleOrthPrefixes.addPhrase("pt .".split(" "));
		this.prefixOrthFinder = new TrieDictFinder(titleOrthPrefixes);
	}
	
	/**
	 * Główna metoda chunkera.
	 */
	@Override
	public HashMap<Sentence, AnnotationSet> chunk(Document ps) {
		for ( Paragraph paragraph : ps.getParagraphs() ){
			for ( Sentence sentence : paragraph.getSentences() ){
				this.chunk(sentence);
			}
		}				
		return ps.getChunkings();
	}

	/**
	 * Rozpoznaje tytułu i dodaje jako anotacje typu KpwrNNer.NAM.
	 * @param sentence
	 */
	private void chunk(Sentence sentence){
		int i = 0;
		while ( i < sentence.getTokenNumber() ){
			QuotationSize qsizeToSkip = null;
			// Pomiń frazy w cudzysłowach, aby uniknąć dopasowania słowa kluczowego występującego wewnątrz cudzysłowu.
			while ( (qsizeToSkip = this.quoationFinder.isQuotation(sentence, i)) != null && qsizeToSkip.getTotalLength() > 0 ){
				i += qsizeToSkip.getTotalLength();
			}
			
			// Najpierwsz znajdz po basach
			int prefixLength = this.prefixBaseFinder.find(sentence, i);
			// Jeżeli nie znajdzie, to po ortach
			if ( prefixLength == 0 ){
				prefixLength = this.prefixOrthFinder.findByOrth(sentence, i, false);
			}
			if ( prefixLength > 0 ){
				QuotationSize qsize = this.quoationFinder.isQuotation(sentence, i+prefixLength);
				if ( qsize != null && qsize.getTextLength() > 0 ){
					int from = i+prefixLength+qsize.getOpeningLength();
					int to = from + qsize.getTextLength() - 1;
					Annotation an = new Annotation(from, to, this.annotationType, sentence);
					sentence.addChunk(an);
					i = to + 1 + qsize.getClosingLength();
					
					QuotationSize qsizeNext = null;
					while ( i + 1 < sentence.getTokenNumber()
								&& Sets.intersection( sentence.getTokens().get(i).getDisambBases(), this.titleSeparator).size() > 0
								&& ( qsizeNext = this.quoationFinder.isQuotation(sentence, i + 1)) != null && qsizeNext.getTextLength() > 0 ){
						int nextFrom = i + 1 + qsizeNext.getOpeningLength();
						int nextTo = nextFrom + qsizeNext.getTextLength() - 1;
						Annotation an2 = new Annotation(nextFrom, nextTo, this.annotationType, sentence);
						sentence.addChunk(an2);
						i = nextTo + 1 + qsize.getClosingLength();
					}
					
					// Cofnij indeks o 1, aby zachować zgodność z i++ spoza if-a
					i--;
				}
			}
			i++;
		}
	}

}
