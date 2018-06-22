package g419.liner2.core.chunker;

import g419.corpus.schema.kpwr.KpwrNer;
import g419.corpus.structure.*;
import g419.liner2.core.tools.TrieDictNode;

import java.util.*;

/**
 * @author Michał Marcińczuk
 */
public class RuleRoadChunker extends Chunker {
	
	private TrieDictNode dictRoads = null;
	private TrieDictNode dictPrefixOuter = new TrieDictNode(false);
	private TrieDictNode dictPrefixInner = new TrieDictNode(false);
	private TrieDictNode dictPrefixList = new TrieDictNode(false);
	private Set<String> roadListSeparators = new HashSet<String>();
	
	private String annotationType = KpwrNer.NER_FAC_ROAD;
	
	public RuleRoadChunker(String annotationType, TrieDictNode node) {
		this.dictRoads = node;
		this.annotationType = annotationType;
		
		this.dictPrefixOuter.addPhrase("ulica".split(" "));
		this.dictPrefixOuter.addPhrase("ulicą".split(" "));
		this.dictPrefixOuter.addPhrase("ulicę".split(" "));
		this.dictPrefixOuter.addPhrase("aleja".split(" "));
		this.dictPrefixOuter.addPhrase("aleją".split(" "));
		this.dictPrefixOuter.addPhrase("aleję".split(" "));
		this.dictPrefixOuter.addPhrase("ul .".split(" "));
		this.dictPrefixOuter.addPhrase("al .".split(" "));
		//this.dictPrefixOuter.addPhrase("d .".split(" "));
		this.dictPrefixOuter.addPhrase("na".split(" "));
		this.dictPrefixOuter.addPhrase("trasa".split(" "));
		this.dictPrefixOuter.addPhrase("trasy".split(" "));
		
		this.dictPrefixInner.addPhrase("Al .".split(" "));
		this.dictPrefixInner.addPhrase("Aleja".split(" "));
		this.dictPrefixInner.addPhrase("Trasa".split(" "));
		this.dictPrefixInner.addPhrase("Trasy".split(" "));
		this.dictPrefixInner.addPhrase("Szosa".split(" "));
		this.dictPrefixInner.addPhrase("Szosie".split(" "));
		this.dictPrefixInner.addPhrase("Rondo".split(" "));
		this.dictPrefixInner.addPhrase("Ronda".split(" "));
		this.dictPrefixInner.addPhrase("Rondem".split(" "));
		
		this.dictPrefixList.addPhrase("ulice :".split(" "));
		this.dictPrefixList.addPhrase("pomiędzy ulicą".split(" "));
		this.dictPrefixList.addPhrase("ulic".split(" "));
		this.dictPrefixList.addPhrase("ulicami".split(" "));
		
		this.roadListSeparators.add(",");
		this.roadListSeparators.add("/");
		this.roadListSeparators.add("oraz");
		this.roadListSeparators.add("i");
	}
	
	/**
	 * 
	 * @param sentence
	 * @return
	 */
	private AnnotationSet chunkSentence(Sentence sentence) {
		AnnotationSet chunking = new AnnotationSet(sentence);
		int i=0;
		List<Annotation> foundAnnotations = new LinkedList<Annotation>();
		while ( i < sentence.getTokenNumber() ){
			foundAnnotations.clear();

			/* Lista nazw ulic poprzedzonych prefiksem typu "ulice:" */
			if ( foundAnnotations.size() == 0 ){
				int j = i + this.match(this.dictPrefixList, sentence.getTokens(), i, false);				
				while ( j > i && j < sentence.getTokens().size() ){
					Annotation an = this.matchRoadAny(sentence, j);
					if ( an != null ){
						foundAnnotations.add(an);
						j = an.getTokens().last();
					}
					else if (!this.roadListSeparators.contains(sentence.getTokens().get(j).getOrth().toLowerCase())) {
						break;
					}
					j++;
				}					
			}

			/* Fraza od ROAD do ROAD */
			if ( foundAnnotations.size() == 0 ){
				if ( sentence.getTokens().get(i).getOrth().toLowerCase().equals("od") ){
					Annotation firstRoad = this.matchRoadAny(sentence, i+1);
					if ( firstRoad != null && sentence.getTokens().get(firstRoad.getTokens().last()+1).getOrth().toLowerCase().equals("do") ){
						Annotation secondRoad = this.matchRoadAny(sentence, firstRoad.getTokens().last() + 2);
						if ( secondRoad != null ){
							foundAnnotations.add(firstRoad);
							foundAnnotations.add(secondRoad);
							i = secondRoad.getTokens().last();
						}
					}
				}
			}

			/* Nazwa ulicy poprzedzona lub zaczynająca się prefiksem */
			if ( foundAnnotations.size() == 0 ){
				Annotation an = this.matchRoadWithPrefix(sentence, i);
				if ( an != null ){
					foundAnnotations.add(an);
				}
			}

			/* Dodaj rozpoznane anotacje do zbioru i uaktualnij aktualny indeks na koniec ostatniej rozpoznanej anotacji */
			for ( Annotation an : foundAnnotations ){
				chunking.addChunk(an);
				i = Math.max(i, an.getTokens().last());
			}
			i++;
		}
		
		/** Usuń wcześniejsze anotacje, które pokrywają się z nazwami ulic */
		Set<Integer> toRemove = new HashSet<Integer>();
		for ( Annotation an : chunking.chunkSet() ){
			toRemove.addAll(an.getTokens());
		}
		Set<Annotation> annToRemove = new HashSet<Annotation>();
		for ( Annotation an : sentence.getChunks() ){
			for ( Integer n : an.getTokens() ){
				if ( toRemove.contains(n) ){
					annToRemove.add(an);
				}
			}
		}
		sentence.getChunks().removeAll(annToRemove);
		
		return chunking;
	}	
	
	/**
	 * Dopasowuje nazwę ulicy (dictRoads) poprzedzoną prefiksem lub zaczynającą się od prefiksu
	 * @param sentence
	 * @param index
	 * @return
	 */
	private Annotation matchRoadWithPrefix(Sentence sentence, int index){
		Annotation an = null;
		
		/* Nazwa ulicy poprzedzona zewnętrznym prefiksem */
		if ( an == null ){
			int prefixLength = this.match(this.dictPrefixOuter, sentence.getTokens(), index, false);
			if ( prefixLength > 0 ){
				int roadLength = this.match(this.dictRoads, sentence.getTokens(), index+prefixLength, true);
				if ( roadLength > 0 ){
					an = new Annotation(index+prefixLength, index+prefixLength+roadLength-1, this.annotationType, sentence);
				}				
			}
		}
		
		/* Nazwa ulicy poprzedzona wewnętrznym prefiksem */
		if ( an == null ){
			int prefixLength = this.match(this.dictPrefixInner, sentence.getTokens(), index, false);
			if ( prefixLength > 0 ){
				int roadLength = this.match(this.dictRoads, sentence.getTokens(), index+prefixLength, true);
				if ( roadLength > 0 ){
					an = new Annotation(index, index+prefixLength+roadLength-1, this.annotationType, sentence);
				}				
			}
		}
		
		return an;
	}
	
	/**
	 * Dopasowuje nazwę ulicy bez względu, czy jest poprzedzona prefiksem.
	 * @param sentence
	 * @param index
	 * @return
	 */
	private Annotation matchRoadAny(Sentence sentence, int index){
		Annotation an = this.matchRoadWithPrefix(sentence, index);
		if ( an == null ){
			int matched = this.match(this.dictRoads, sentence.getTokens(), index, true);
			if ( matched > 0 ){
				an = new Annotation(index, index+matched-1, this.annotationType, sentence);
			}
		}
		return an;
	}
	
	/**
	 * Funkcja sprawdza, czy sekwencja tokenów zaczynająca się od indeksu index zandjuje się w słowniku.
	 * 
	 * @param tokens
	 * @param index
	 * @return
	 */
	private int match(TrieDictNode dict, List<Token> tokens, int index, boolean useInflection){
		TrieDictNode currentNode = dict;
		int longestMatch = 0;
		int offset = 0;
		while ( currentNode != null && index+offset < tokens.size() ){
			String word = tokens.get(index+(offset++)).getOrth();
			TrieDictNode nextNode = currentNode.getChild(word);
			
			// Jeżeli brak dopasowania i opcja useInflection jest aktywan to sprawdź, czy jest to forma odmieniona
			if ( nextNode == null && useInflection ){
				if ( word.endsWith("skiej") ){
					nextNode = currentNode.getChild(word.subSequence(0, word.length()-5) + "ska");
				}
				else if ( word.endsWith("ckiej") ){
					nextNode = currentNode.getChild(word.subSequence(0, word.length()-5) + "cka");
				}
				else if ( word.endsWith("iem") ){
					nextNode = currentNode.getChild(word.subSequence(0, word.length()-3) + "");
				}
				else if ( word.endsWith("im") ){
					nextNode = currentNode.getChild(word.subSequence(0, word.length()-1) + "" );
				}
				else if ( word.endsWith("ą") ){
					nextNode = currentNode.getChild(word.subSequence(0, word.length()-1) + "a");
				}
			}
			
			if ( nextNode != null && nextNode.isTerminal() ){
				longestMatch = offset;
			}
			// Jeżeli nie było dopasowania, a tokenem jest określony znak, to pomiń ten znak
			if ( nextNode == null && word.equals(".") ){
				nextNode = currentNode;
			}
			currentNode = nextNode; 
		}
		return longestMatch;
	}
	
	
	@Override
	public HashMap<Sentence, AnnotationSet> chunk(Document ps) {
		HashMap<Sentence, AnnotationSet> chunkings = new HashMap<Sentence, AnnotationSet>();
		for ( Paragraph paragraph : ps.getParagraphs() )
			for (Sentence sentence : paragraph.getSentences())
				chunkings.put(sentence, this.chunkSentence(sentence));
		return chunkings;
	}
}
