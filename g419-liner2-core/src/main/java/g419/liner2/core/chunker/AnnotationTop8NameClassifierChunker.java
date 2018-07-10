package g419.liner2.core.chunker;

import g419.corpus.schema.kpwr.KpwrNer;
import g419.corpus.structure.*;
import g419.liner2.core.features.annotations.AnnotationAtomicFeature;
import g419.liner2.core.features.annotations.AnnotationFeatureSubstList;
import g419.liner2.core.features.annotations.AnnotationFeatureSubstModifierAfter;
import g419.liner2.core.features.annotations.AnnotationFeatureSubstModifierBefore;
import g419.liner2.core.tools.FrequencyCounter;
import g419.liner2.core.tools.TypedDictionary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Combines two models (name and top8) into a single unified model top8. 
 * The annotations recognized by the name model are used to improve the recall.
 * A set of global NE features is used to improve the final precision of classification.
 * 
 * @author Michał Marcińczuk
 *
 */
public class AnnotationTop8NameClassifierChunker extends Chunker {
		
	private Chunker inputChunker = null;
	private List<AnnotationAtomicFeature> substFeatures = new ArrayList<AnnotationAtomicFeature>();  
	private TypedDictionary categoryIndicators = null;
	final private Logger logger = LoggerFactory.getLogger(getClass());
	
	/**
	 * 
	 * @param inputChunker
	 */
    public AnnotationTop8NameClassifierChunker(Chunker inputChunker, TypedDictionary categoryIndicators) {
    	this.inputChunker = inputChunker;
    	this.categoryIndicators = categoryIndicators;
    	this.substFeatures.add(new AnnotationFeatureSubstList());
    	this.substFeatures.add(new AnnotationFeatureSubstModifierAfter());
    	this.substFeatures.add(new AnnotationFeatureSubstModifierBefore());
    }

	@Override
	public Map<Sentence, AnnotationSet> chunk(Document ps) {
		Map<Sentence, AnnotationSet> inputChunks = this.inputChunker.chunk(ps);

		/** Remove redundant NAM annotations */
		for ( AnnotationSet sets : inputChunks.values() ){
			this.removeRedundantNamAnnotations(sets.chunkSet());
		}
		
		/** Group annotations by base forms */
		this.classifyAnnotationsByGroups(inputChunks);
		
		/** Remove nam annotations */
		for ( AnnotationSet sets : inputChunks.values() ){
			this.removeNamAnnotations(sets.chunkSet());
		}
		
		return inputChunks;
	}
	
	/**
	 * Removes nam annotations which are duplications of other annotations.
	 * @param ans
	 */
	private void removeRedundantNamAnnotations(Collection<Annotation> ans){
		Set<String> fineGrained = new HashSet<String>();
		List<Annotation> toRemove = new ArrayList<Annotation>();
		List<Annotation> toFilter= new ArrayList<Annotation>();
		for (Annotation an : ans){
			if ( an.getType().equals(KpwrNer.NER) ){
				toFilter.add(an);
			} else {
				fineGrained.add(String.format("%d:%d", an.getBegin(), an.getEnd()));
			}
		}
		for (Annotation an : toFilter ){
			String key = String.format("%d:%d", an.getBegin(), an.getEnd());
			if ( fineGrained.contains(key) ){
				toRemove.add(an);
			}
		}
		ans.removeAll(toRemove);
	}
	
	/**
	 * 
	 * @param ans
	 */
	private void classifyAnnotationsByGroups(Map<Sentence, AnnotationSet> inputChunks){
		Map<String, List<Annotation>> groupedAnnotations = new HashMap<String, List<Annotation>>(); 
		for ( AnnotationSet sets : inputChunks.values() ){
			for ( Annotation an : sets.chunkSet() ){
				String groupedText = an.getBaseText().toLowerCase();
				List<Annotation> group = groupedAnnotations.get(groupedText);
				if ( group == null ){
					group = new ArrayList<Annotation>();
					groupedAnnotations.put(groupedText, group);
				}
				group.add(an);
			}
		}
		
		for ( String groupText : groupedAnnotations.keySet() ){
			Set<String> types = new HashSet<String>();
			List<Annotation> anns = groupedAnnotations.get(groupText);
			
			FrequencyCounter<String> typeFrequency = new FrequencyCounter<String>();

			/* Zlicz typy anotacji przypisane przez model CRF */ 
			for ( Annotation an : groupedAnnotations.get(groupText) ){
				/* Count type frequency expect NAM */
				if ( !an.getType().equals(KpwrNer.NER) ){
					typeFrequency.add(an.getType());
				}
				types.add(an.getType());
			}

			/* Dodaj typy wynikację z przesłanek w kontekście */
			List<String> indicators = this.getAnnotationGroupCategoryIndicators(anns);
			
			/* Dodaj głowę frazy jako przesłankę */
			indicators.add(anns.get(0).getHeadToken().getDisambTag().getBase());

			/* Dodaj każdy subst w nazwie jako przesłankę */
			for (Token t : anns.get(0).getTokenTokens()){
				if ( t.getDisambTag().getPos().equals("subst") ){
					indicators.add(t.getDisambTag().getBase());
				}
			}
			
			for ( String indicator : indicators ){
				Set<String> indicatorTypes = this.categoryIndicators.getTypes(indicator);
				if ( indicatorTypes != null ){
					// Categories for indicators has double weight
					typeFrequency.addAll(indicatorTypes);
					typeFrequency.addAll(indicatorTypes);
				}					
			}

			logger.debug(String.format("MIXED TYPES: # %s [%3d]", groupText , groupedAnnotations.get(groupText).size()));
			anns.stream().map(an->String.format("MIXED TYPES:    %s:%s (confidence=%4.2f)", an.getType(), an.getText(), an.getConfidence())).forEach(logger::debug);
			logger.debug(String.format("MIXED TYPES: %s", String.join(", ", indicators)));
			if ( types.size()>1 ){
				logger.debug(String.format("MIXED TYPES: MULTITYPE"));
			}

			/* Ustaw najliczniejszą kategorię */
			Set<String> mostFrequentTypes = typeFrequency.getMostFrequent();
			if ( mostFrequentTypes.size() == 1 ){				
				String type = mostFrequentTypes.iterator().next();
				this.setCategory(anns, type);
			}
			else if ( types.size() > 1 || types.contains(KpwrNer.NER)){				
				FrequencyCounter<String> indicatorTypeFreq = new FrequencyCounter<String>();
				for ( String indicator : indicators ){
					Set<String> indicatorTypes = this.categoryIndicators.getTypes(indicator);
					if ( indicatorTypes != null ){
						indicatorTypeFreq.addAll(indicatorTypes);
					}					
				}
				Set<String> topIndicatorTypes = indicatorTypeFreq.getMostFrequent();
				if ( topIndicatorTypes.size() == 1 ){
					this.setCategory(anns, topIndicatorTypes.iterator().next());
				} else if ( topIndicatorTypes.size() == 0 && indicators.size() > 0) {
					logger.debug("MIXED TYPES INDICATOR: " + String.join(", ", indicatorTypeFreq.getMostFrequent()));
				}
			}
		}
		
	}
	
	/**
	 * Remove NAM annotations
	 * @param ans
	 */
	private void removeNamAnnotations(Collection<Annotation> ans){
		List<Annotation> toRemove = new ArrayList<Annotation>();
		for ( Annotation an : ans ){
			if ( an.getType().equals(KpwrNer.NER) ){
				toRemove.add(an);
			}
		}
		ans.removeAll(toRemove);
	}
	
	/**
	 * 
	 * @param ans
	 * @return
	 */
	private List<String> getAnnotationGroupCategoryIndicators(Collection<Annotation> ans){
		List<String> indicators = new ArrayList<String>();
		for ( AnnotationAtomicFeature aaf : this.substFeatures ){
			for ( Annotation an : ans){
				String subst = aaf.generate(an);
				if ( subst != null ){
					indicators.add(subst);
				}
			}
		}
		return indicators;
	}
	
	/**
	 * 
	 * @param anns
	 * @param type
	 */
	private void setCategory(Collection<Annotation> anns, String type){
		for ( Annotation an : anns ){
			an.setType(type);
		}		
	}
}
