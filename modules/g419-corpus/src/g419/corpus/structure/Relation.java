package g419.corpus.structure;

/**
 * Klasa reprezentuje relację pomiędzy dwoma anotacjami 
 * @author Adam Kaczmarek<adamjankaczmarek@gmail.com>
 *
 */
public class Relation {
	public static final String COREFERENCE = "Coreference";
	public static final String SUBJECT = "subj";
	public static final String OBJECT="obj";
	public static final String COPULA = "cop";

	public static final String EMPTY = "";
	
	public static final AnnotationPositionComparator annPosComparator = new AnnotationPositionComparator();
	/**
	 * Typ relacji
	 */
	private String type;
	
	/**
	 * Podzbiór relacji
	 */
	private String set;
	
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
		this.setSet(type);
		this.setType(type);
	}
	
	public Relation(Annotation from, Annotation to, String type, String set){
		this.setAnnotationFrom(from);
		this.setAnnotationTo(to);
		this.setSet(set);
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
	
	public void setSet(String set){
		this.set = set;
	}

	public String getSet() {
		if(this.set == null) return Relation.EMPTY;
		return this.set;
	}
	
	public String toString(){
		return annotationFrom.toString() + " >-- " + type + " --> " + annotationTo.toString();
	}
	
}
