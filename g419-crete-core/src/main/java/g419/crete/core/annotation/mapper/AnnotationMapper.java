package g419.crete.core.annotation.mapper;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.crete.core.annotation.AbstractAnnotationSelector;

import java.util.Comparator;
import java.util.HashMap;

/**
 * Created by akaczmarek on 13.12.15.
 */
public class AnnotationMapper {
    Comparator<Annotation> comparator;
    AbstractAnnotationSelector selector;
//		List<Pattern> annotationTypes;

    boolean teiRemap;

//		public AnnotationMapper(Comparator<Annotation> comparator, List<Pattern> annotationTypes){
//			this.annotationTypes = annotationTypes;
//			this.comparator = comparator;
//			this.teiRemap = true;
//		}

    public AnnotationMapper(Comparator<Annotation> comparator, AbstractAnnotationSelector selector) {
        this.selector = selector;
        this.comparator = comparator;
        this.teiRemap = true;
    }

	/*
     * Zwraca mapowanie z anotacji dokumentu systemowego na anotacje w dokumencie referencyjnym
     */
    public HashMap<Annotation, Annotation> createMapping(Document referenceDocument, Document systemDocument) {
        HashMap<Annotation, Annotation> mapping = new HashMap<Annotation, Annotation>();

		for (Annotation sysAnnotation : selector.selectAnnotations(systemDocument)) {
            for (Annotation refAnnotation : selector.selectAnnotations(referenceDocument)) {
                if (comparator.compare(refAnnotation, sysAnnotation) == 0) {
                    // Przeniesione porównanie zdań
//						if(systemDocument.getSentences().indexOf(sysAnnotation.getSentence()) == referenceDocument.getSentences().indexOf(refAnnotation.getSentence())){
					if (this.teiRemap && refAnnotation.getType().endsWith("nam")) {
						sysAnnotation.setType(refAnnotation.getType());
					}
					mapping.put(sysAnnotation, refAnnotation);
					break;
//						}
                }
            }
        }

		return mapping;
    }
}
