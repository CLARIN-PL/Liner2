package g419.crete.core.features.annotations.pair;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationPositionComparator;
import g419.crete.core.annotation.AbstractAnnotationSelector;
import g419.crete.core.annotation.AnnotationSelectorFactory;
import g419.crete.core.structure.AnnotationUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class AnnotationPairFeaturePronounLink extends AbstractAnnotationPairFeature<Float> {

	private static final int SCORE_NO_INTERMEDIATE = 3;
	private static final int SCORE_GND_MATCH = 2;
	private static final int SCORE_NMB_MATCH = 1;
	private static final int SCORE_PERS_MATCH = 1;
	
	private AnnotationPairFeatureGenderAgreement genderAgreement = new AnnotationPairFeatureGenderAgreement(false);
	private AnnotationPairFeatureNumberAgreement numberAgreement = new AnnotationPairFeatureNumberAgreement();
	private AnnotationPairFeaturePersonAgreement personAgreement = new AnnotationPairFeaturePersonAgreement();
	
	private Comparator<Annotation> positionComparator = new AnnotationPositionComparator();
	
	private AnnotationPairFeatureSemanticLinkAgP linker;
	
	public AnnotationPairFeaturePronounLink(AnnotationPairFeatureSemanticLinkAgP linker) {
		this.linker = linker;
	}
	
	private boolean noPotentialIntermediateNamMatch(Annotation named, Annotation pronoun){
		List<Annotation> intermediateNamAnnotations = AnnotationUtil.annotationsBetweenAnnotations(named, pronoun, named.getSentence().getDocument(), Arrays.asList(new Pattern[]{Pattern.compile("nam.*")}));
		for(Annotation intermediate: intermediateNamAnnotations){
			// TODO: verify why only gender // vide: IKAR
			genderAgreement.generateFeature(new ImmutablePair<Annotation, Annotation>(intermediate, pronoun));
			if(genderAgreement.getValue()) return false;
		}
		return true;
	}
	
	private List<Annotation> findPotentialBridges(Annotation named, Annotation pronoun){
		AbstractAnnotationSelector agpSelector = AnnotationSelectorFactory.getFactory().getInitializedSelector("agp_mention_selector");
		List<Annotation> agpList = agpSelector.selectAnnotations(named.getSentence().getDocument());
		List<Annotation> intermediatePointerAnnotations = AnnotationUtil.annotationsBetweenAnnotations(named, pronoun, named.getSentence().getDocument(), Arrays.asList(new Pattern[]{Pattern.compile("anafora_wyznacznik*")}));
		
		return intermediatePointerAnnotations.stream().filter(a -> agpList.contains(a)).collect(Collectors.toList());
	}
	
	@Override
	public void generateFeature(Pair<Annotation, Annotation> input) {
		// TODO: dodaj nową cechę: Baza1 + Baza2 i przeprowadź analizę błędów
		Annotation pron = input.getLeft();
		Annotation named = input.getRight();
		
		assert(named.getType().startsWith("nam"));
		assert(pron.getType().equalsIgnoreCase("anafora_wyznacznik"));
		
		String neWord = named.getSentence().getTokens().get(Math.max(named.getTokens().first(), named.getHead())).getAttributeValue("base");
		String pronWord = pron.getSentence().getTokens().get(Math.max(pron.getTokens().first(), pron.getHead())).getAttributeValue("base");
		
		this.value = 0.0f;
		
		if(noPotentialIntermediateNamMatch(named, pron)) this.value += SCORE_NO_INTERMEDIATE;
		
		genderAgreement.generateFeature(input);
		numberAgreement.generateFeature(input);
		personAgreement.generateFeature(input);
		
		if(genderAgreement.getValue()) this.value += SCORE_GND_MATCH;
		if(numberAgreement.getValue()) this.value += SCORE_NMB_MATCH;
		if(personAgreement.getValue()) this.value += SCORE_PERS_MATCH;
		
		// Znajdź potencjalne mosty i posortuj od najbliższego
		List<Annotation> bridges = findPotentialBridges(named, pron);
		bridges.sort(positionComparator);
		Collections.reverse(bridges);
		for(Annotation bridge : bridges){
			boolean goodBridge = true;
			Pair<Annotation, Annotation> bridgePair = new ImmutablePair<Annotation, Annotation>(bridge, pron);
			genderAgreement.generateFeature(bridgePair);
			numberAgreement.generateFeature(bridgePair);
			personAgreement.generateFeature(bridgePair);
			if(!genderAgreement.getValue())goodBridge = false;
			if(!numberAgreement.getValue()) goodBridge = false;
			if(!personAgreement.getValue()) goodBridge = false;
			
			if(goodBridge){
				linker.generateFeature(new ImmutablePair<Annotation, Annotation>(named, bridge));
				float bridgeScore = 4 + linker.getValue();
				if(bridgeScore > this.value) this.value = bridgeScore;
				break;
			}
		}
	}

	@Override
	public String getName() {
		return "annotationpair_pronoun_link";
	}

	@Override
	public Class<Float> getReturnTypeClass() {
		return Float.class;
	}

}
