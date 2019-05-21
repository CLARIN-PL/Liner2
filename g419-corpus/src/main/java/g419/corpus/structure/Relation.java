package g419.corpus.structure;

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Klasa reprezentuje relację pomiędzy dwoma anotacjami
 *
 * @author Adam Kaczmarek
 */
public class Relation extends IdentifiableElement {
  public static final String COREFERENCE = "Coreference";
  public static final String SUBJECT = "subj";
  public static final String OBJECT = "obj";
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

  public Document getDocument() {
    return document;
  }

  public Relation(final String id, final Annotation from, final Annotation to, final String type) {
    this(from, to, type);
    this.id = id;
  }

  public Relation(final Annotation from, final Annotation to, final String type) {
    setAnnotationFrom(from);
    setAnnotationTo(to);
    setSet(type);
    setType(type);
  }

  public Relation(final String id, final Annotation from, final Annotation to, final String type, final String set) {
    this.id = id;
    setAnnotationFrom(from);
    setAnnotationTo(to);
    setSet(set);
    setType(type);
  }

  public Relation(final Annotation from, final Annotation to, final String type, final String set, final Document document) {
    this.document = document;
    setAnnotationFrom(from);
    setAnnotationTo(to);
    setSet(set);
    setType(type);
  }

  public Annotation getAnnotationFrom() {
    return annotationFrom;
  }


  public void setAnnotationFrom(final Annotation annotationFrom) {
    this.annotationFrom = annotationFrom;
  }


  public Annotation getAnnotationTo() {
    return annotationTo;
  }


  public void setAnnotationTo(final Annotation annotationTo) {
    this.annotationTo = annotationTo;
  }


  public String getType() {
    return type;
  }


  public void setType(final String type) {
    this.type = type;
  }

  public void setSet(final String set) {
    this.set = set;
  }

  public String getSet() {
    return set == null ? Relation.EMPTY : set;
  }

  @Override
  public String toString() {
    return annotationFrom.toString() + " ->- " + type + " ->- " + annotationTo.toString();
  }

  @Override
  public boolean equals(final Object other) {
    if (other == null) {
      return false;
    }
    if (other == this) {
      return true;
    }
    if (!(other instanceof Relation)) {
      return false;
    }
    final Relation otherRelation = (Relation) other;

    return set.equalsIgnoreCase(otherRelation.set)
        && type.equalsIgnoreCase(otherRelation.type)
        && annotationFrom.equals(otherRelation.annotationFrom)
        && annotationTo.equals(otherRelation.annotationTo);
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
