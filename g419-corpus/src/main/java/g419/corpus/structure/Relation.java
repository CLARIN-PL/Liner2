package g419.corpus.structure;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.json.JSONObject;

/**
 * Klasa reprezentuje relację pomiędzy dwoma anotacjami 
 * @author Adam Kaczmarek
 *
 */
public class Relation extends IdentifiableElement{
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
	
	/**
	 * Dokument zawierający relację
	 */
	private Document document;
	public Document getDocument(){return document;}

	public JSONObject toJson(){
		JSONObject json = new JSONObject();
		json.put("name",type);
		json.put("set",set);
		json.put("from", forAnnotation(annotationFrom));
		json.put("to", forAnnotation(annotationTo));
		return json;
	}

	private JSONObject forAnnotation(Annotation ann){
		JSONObject json = new JSONObject();
		json.put("sent", ann.getSentence().getId());
		json.put("chan", ann.getType());
		json.put("value", ann.getChannelIdx());
		return  json;
	}

	public Relation(final String id, final Annotation from, final Annotation to, final String type){
		this(from,to,type);
		this.id = id;
	}

	public Relation(final Annotation from, final Annotation to, final String type){
		this.setAnnotationFrom(from);
		this.setAnnotationTo(to);
		this.setSet(type);
		this.setType(type);
	}

	public Relation(final String id, final Annotation from, final Annotation to, final String type, final String set){
		this.id = id;
		this.setAnnotationFrom(from);
		this.setAnnotationTo(to);
		this.setSet(set);
		this.setType(type);
	}

	public Relation(final Annotation from, final Annotation to, final String type, final String set, final Document document){
		this.document = document;
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
		return this.set == null ? Relation.EMPTY : this.set;
	}

	@Override
	public String toString(){
		return annotationFrom.toString() + " ->- " + type + " ->- " + annotationTo.toString();
	}
	
	@Override
	public boolean equals(Object other){
		if (other == null) return false;
	    if (other == this) return true;
	    if (!(other instanceof Relation))return false;
	    Relation otherRelation = (Relation) other;
	    
		return this.set.equalsIgnoreCase(otherRelation.set) 
				&& this.type.equalsIgnoreCase(otherRelation.type) 
				&& this.annotationFrom.equals(otherRelation.annotationFrom)
				&& this.annotationTo.equals(otherRelation.annotationTo);
	}
	
	@Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31). 
            append(set).
            append(type).
            append(annotationFrom).
            append(annotationTo).
            toHashCode();
    }
	
}
