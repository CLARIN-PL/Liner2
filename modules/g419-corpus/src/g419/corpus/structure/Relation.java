package g419.corpus.structure;

/**
 * Klasa reprezentuje relację pomiędzy dwoma anotacjami 
 * @author Adam Kaczmarek<adamjankaczmarek@gmail.com>
 *
 */
public class Relation {
	public static final String COREFERENCE = "coreference";
	/**
	 * Typ relacji
	 */
	private String type;
	
	/**
	 * Anotacja źródłowa relacji
	 */
	private Annotation annotationFrom;
	
	/**
	 * Anotacja docelowa relacji
	 */
	private Annotation annotationTo;
	
	
	public Relation(Annotation from, Annotation to, String type){
		this.setAnnotationFrom(from);
		this.setAnnotationTo(to);
		this.setType(type);
	}


	public Annotation getAnnotationFrom() {
		return annotationFrom;
	}


	public void setAnnotationFrom(Annotation annotationFrom) {
		this.annotationFrom = annotationFrom;
	}


	public Annotation getAnnotationTo() {
		return annotationTo;
	}


	public void setAnnotationTo(Annotation annotationTo) {
		this.annotationTo = annotationTo;
	}


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}
	
}
