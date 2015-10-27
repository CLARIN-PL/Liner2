//import g419.corpus.structure.Annotation;
//import g419.corpus.structure.Document;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.regex.Pattern;
//
//class OverlapAnnotationSelector extends ConfigurableAnnotationSelector {
//	
//	protected List<AnnotationDescription> overlaps;
//	protected List<Pattern> overlapPatterns;
//	
//	public OverlapAnnotationSelector(List<AnnotationDescription> annotationDescriptions, List<AnnotationDescription> overlaps) {
//		super(annotationDescriptions);
//		this.overlaps = overlaps;
//		this.overlapPatterns = new ArrayList<Pattern>();
//		for(AnnotationDescription description : this.overlaps) overlapPatterns.addAll(description.getPatterns());
//	}
//
//	@Override
//	public List<Annotation> selectAnnotations(Document document){
//		List<Annotation> selectedAnnotations = new ArrayList<Annotation>();
//		List<Annotation> preSelectedAnnotations = super.selectAnnotations(document);
//		
//		for(Annotation annotation : preSelectedAnnotations){
//			// TODO: tymczasowe rozwiązanie 
//			// Docelowo rozważyć czy liczyć dopasowania 
//			// wzorców(Pattern) czy opisów anotacji(AnnotationDescription)
//			if(annotation.isOverlappedByAny(overlapPatterns))
//				selectedAnnotations.add(annotation);
//		}
//		
//		return selectedAnnotations;
//	}
//}