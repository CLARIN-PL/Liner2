package g419.crete.core.features.annotations.pair;

import g419.corpus.structure.Annotation;
import g419.toolbox.wordnet.Wordnet;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Pair;

import pl.wroc.pwr.ci.plwordnet.plugins.princetonadapter.da.PrincetonDataRaw;

public class AnnotationPairFeatureSemanticLinkAgP extends AbstractAnnotationPairFeature<Float>{

	private static final float LONG_PATH = 1000000.0f; 
	private final HashMap<String, String[]> mapping;
	private static Wordnet wn;
	
	enum WordnetRelation{
		
		HYPERONYMY("hypero", 10, 1),
		MERONYMY("mero", 4, 3);
		
		private final String name;
		private final int maxDepth;
		private final float depthMultiplier;
		
		WordnetRelation(String name, int maxDepth, float depthMultiplier){
			this.name = name;
			this.maxDepth = maxDepth;
			this.depthMultiplier = depthMultiplier;
		}
	}
	
	public AnnotationPairFeatureSemanticLinkAgP(Wordnet wordnet, HashMap<String, String[]> mapping){
		this.mapping = mapping; 
		this.wn = wordnet;
	}
	
	public float calculatePath(Wordnet wn, PrincetonDataRaw synset1, PrincetonDataRaw synset2, WordnetRelation relation, int depth){
		if(synset1 == synset2) return (float) depth * relation.depthMultiplier;
		if(depth >= relation.maxDepth) return LONG_PATH;
		// Assume synset1 more general than synset2
		float path = LONG_PATH;
		for(PrincetonDataRaw hyper : wn.getHypernyms(synset2)){
			float currentPath = calculatePath(wn, synset1, hyper, relation, depth+1);
			if(currentPath < path) path = currentPath;
		}
		
		return path;
	}
	
	@Override
	public void generateFeature(Pair<Annotation, Annotation> input) {
		// TODO: dodaj nową cechę: Baza1 + Baza2 i przeprowadź analizę błędów
		Annotation agp = input.getLeft();
		Annotation named = input.getRight();
		
		assert(named.getType().startsWith("nam"));
		assert(agp.getType().equalsIgnoreCase("anafora_wyznacznik"));
		
		String neWord = named.getSentence().getTokens().get(Math.max(named.getTokens().first(), named.getHead())).getAttributeValue("base");
		String agpWord =  agp.getSentence().getTokens().get(Math.max(agp.getTokens().first(), agp.getHead())).getAttributeValue("base");
		
		List<PrincetonDataRaw> neSynsets;
		if(this.mapping.get(named.getType()) != null){
			neSynsets = new ArrayList<>();
			String[] synsetNames = this.mapping.get(named.getType());
			for(String nameAndSense : synsetNames){
				int lastSpace = nameAndSense.lastIndexOf(" "); 
//				System.out.println(nameAndSense);
				String word = nameAndSense.substring(0, lastSpace);
				int sense = Integer.parseInt(nameAndSense.substring(lastSpace + 1).replaceAll("[^0-9]", ""));
				neSynsets.addAll(this.wn.getSynsets(word, sense));
			}
		}
		else{
			neSynsets = wn.getSynsets(neWord);
		}
		List<PrincetonDataRaw> agpSynsets = wn.getSynsets(agpWord);
		
		float shortestPath = LONG_PATH;
		
		for(PrincetonDataRaw neSynset : neSynsets){
			for(PrincetonDataRaw agpSynset : agpSynsets){
				float path = calculatePath(wn, neSynset, agpSynset, WordnetRelation.HYPERONYMY, 0);
				if(path < shortestPath) shortestPath = path;
				// Reverse order
				path = calculatePath(wn, agpSynset, neSynset, WordnetRelation.HYPERONYMY, 0);
				if(path < shortestPath) shortestPath = path;
			}
		}				
		
		this.value = shortestPath > 0 ? (float) 1.0 / shortestPath : 2.0f;
		if(this.value > 0.25 && this.value < 1.0){
			System.out.println(this.value);
			System.out.println(named);
			System.out.println(agp);
		}
	}

	@Override
	public String getName() {
		return "annotationpair_semantic_link";
	}

	@Override
	public Class<Float> getReturnTypeClass() {
		return Float.class;
	}

}
