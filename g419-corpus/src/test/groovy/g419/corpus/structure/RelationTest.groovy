package g419.corpus.structure

import spock.lang.Shared
import spock.lang.Specification


class RelationTest extends Specification{
    @Shared
            attrIndex
    @Shared
            sampleSentence
    @Shared
            annotation1
    @Shared
            annotation2

    def setup() {
        attrIndex = new TokenAttributeIndex()
        attrIndex.addAttribute("orth")
        sampleSentence = new Sentence(attrIndex)
        sampleSentence.addToken(new Token("Ala", new Tag("Ala", "subst:sg:nom:f", true), attrIndex))
        sampleSentence.addToken(new Token("ma", new Tag("mieć", "fin:sg:ter:imperf", true), attrIndex))
        Token tok = new Token("kota", new Tag("kot", "subst:sg:gen:m2", true), attrIndex)
        tok.setNoSpaceAfter(true)
        sampleSentence.addToken(tok)
        sampleSentence.addToken(new Token(".", new Tag(".", "interp", true), attrIndex))

        annotation1 = new Annotation(0, 2, "test_annotation_type", sampleSentence)
        annotation2 = new Annotation(2, 2, "test_annotation_type", sampleSentence)
    }

    def "Should create relation"() {
        given:
        Relation relation = new Relation("test_relation_id", annotation1, annotation2, "test_relation_type")

        expect:
        relation.id == "test_relation_id"
        relation.type == "test_relation_type"
        relation.set == "test_relation_type"
        relation.annotationFrom == annotation1
        relation.annotationTo == annotation2
        relation.document == null

        when:
        relation = new Relation("test_relation_id", annotation1, annotation2, "test_relation_type", "test_relation_set")

        then:
        relation.id == "test_relation_id"
        relation.type == "test_relation_type"
        relation.set == "test_relation_set"
        relation.annotationFrom == annotation1
        relation.annotationTo == annotation2
        relation.document == null

        when:
        relation = new Relation(annotation1, annotation2, "test_relation_type")

        then:
        relation.id == null
        relation.type == "test_relation_type"
        relation.set == "test_relation_type"
        relation.annotationFrom == annotation1
        relation.annotationTo == annotation2
        relation.document == null

        when:
        AnnotationSet annSet = new AnnotationSet(sampleSentence)
        annSet.addChunk(annotation1)
        annSet.addChunk(annotation2)
        sampleSentence.addAnnotations(annSet)

        Paragraph paragraph = new Paragraph("test_paragraph", attrIndex)
        paragraph.addSentence(sampleSentence)

        Document document = new Document("test_document_name", attrIndex)
        document.addParagraph(paragraph)
        relation = new Relation(annotation1, annotation2, "test_relation_type", "test_relation_set", document)

        then:
        relation.id == null
        relation.type == "test_relation_type"
        relation.set == "test_relation_set"
        relation.annotationFrom == annotation1
        relation.annotationTo == annotation2
        relation.document.toString() == document.toString()
    }

    def "Should get document"() {
        given:
        Relation relation = new Relation("test_relation_id", annotation1, annotation2, "test_relation_type")

        expect:
        relation.getDocument() == null

        when:
        AnnotationSet annSet = new AnnotationSet(sampleSentence)
        annSet.addChunk(annotation1)
        annSet.addChunk(annotation2)
        sampleSentence.addAnnotations(annSet)

        Paragraph paragraph = new Paragraph("test_paragraph", attrIndex)
        paragraph.addSentence(sampleSentence)

        Document document = new Document("test_document_name", attrIndex)
        document.addParagraph(paragraph)
        relation = new Relation(annotation1, annotation2, "test_relation_type", "test_relation_set", document)

        then:
        relation.getDocument().toString() == document.toString()
    }

    def "Should get annotation from"() {
        given:
        Relation relation = new Relation("test_relation_id", annotation1, annotation2, "test_relation_type")

        expect:
        relation.getAnnotationFrom() == annotation1
    }

    def "Should get annotation to"() {
        given:
        Relation relation = new Relation("test_relation_id", annotation1, annotation2, "test_relation_type")

        expect:
        relation.getAnnotationTo() == annotation2
    }

    def "Should get type"() {
        given:
        Relation relation = new Relation("test_relation_id", annotation1, annotation2, "test_relation_type")

        expect:
        relation.getType() == "test_relation_type"
    }

    def "Should get set"() {
        given:
        Relation relation = new Relation("test_relation_id", annotation1, annotation2, "test_relation_type")

        expect:
        relation.getSet() == "test_relation_type"

        when:
        relation = new Relation("test_relation_id", annotation1, annotation2, "test_relation_type", "test_relation_set")

        then:
        relation.getSet() == "test_relation_set"
    }

    def "Should set annotation from"() {
        given:
        Relation relation = new Relation("test_relation_id", annotation1, annotation2, "test_relation_type")

        expect:
        relation.annotationFrom == annotation1

        when:
        relation.setAnnotationFrom(annotation2)

        then:
        relation.annotationFrom == annotation2
    }

    def "Should set annotation to"() {
        given:
        Relation relation = new Relation("test_relation_id", annotation1, annotation2, "test_relation_type")

        expect:
        relation.annotationTo == annotation2

        when:
        relation.setAnnotationTo(annotation1)

        then:
        relation.annotationTo == annotation1
    }

    def "Should set type"() {
        given:
        Relation relation = new Relation("test_relation_id", annotation1, annotation2, "test_relation_type")

        expect:
        relation.type == "test_relation_type"

        when:
        relation.setType("test_relation_set")

        then:
        relation.type == "test_relation_set"
    }

    def "Should set set"() {
        given:
        Relation relation = new Relation("test_relation_id", annotation1, annotation2, "test_relation_type")

        expect:
        relation.set == "test_relation_type"

        when:
        relation.setSet("test_relation_set")

        then:
        relation.set == "test_relation_set"
    }

    def "Should get as string"() {
        given:
        Relation relation = new Relation("test_relation_id", annotation1, annotation2, "test_relation_type")

        expect:
        relation.toString() == "Annotation{id='null', type='test_annotation_type', group='null', sentence=null, head=0, lemma='null', confidence=1.0} " +
                "->- test_relation_type ->- " +
                "Annotation{id='null', type='test_annotation_type', group='null', sentence=null, head=0, lemma='null', confidence=1.0}"
    }

    def "Same relations should be equal"() {
        given:
        Relation relation1 = new Relation("test_relation_id", annotation1, annotation2, "test_relation_type")
        Relation relation2 = new Relation("test_relation_id", annotation1, annotation2, "test_relation_type")

        expect:
        relation1 == relation2
    }

    def "Relations with different id should be equal"() {
        given:
        Relation relation1 = new Relation("test_relation_id", annotation1, annotation2, "test_relation_type")
        Relation relation2 = new Relation("test_relation_id_2", annotation1, annotation2, "test_relation_type")

        expect:
        relation1 == relation2
    }

    def "Relations with different type should not be equal"() {
        given:
        Relation relation1 = new Relation("test_relation_id", annotation1, annotation2, "test_relation_type")
        Relation relation2 = new Relation("test_relation_id", annotation1, annotation2, "test_relation_type_2")

        expect:
        relation1 != relation2
    }

    def "Relations with different set should not be equal"() {
        given:
        Relation relation1 = new Relation("test_relation_id", annotation1, annotation2, "test_relation_type", "test_relation_set")
        Relation relation2 = new Relation("test_relation_id", annotation1, annotation2, "test_relation_type", "test_relation_set_2")

        expect:
        relation1 != relation2
    }

    def "Relations with different annotation from should not be equal"() {
        given:
        Relation relation1 = new Relation("test_relation_id", annotation2, annotation2, "test_relation_type", "test_relation_set")
        Relation relation2 = new Relation("test_relation_id", annotation1, annotation2, "test_relation_type", "test_relation_set_2")

        expect:
        relation1 != relation2
    }

    def "Relations with different annotation to should not be equal"() {
        given:
        Relation relation1 = new Relation("test_relation_id", annotation1, annotation1, "test_relation_type", "test_relation_set")
        Relation relation2 = new Relation("test_relation_id", annotation1, annotation2, "test_relation_type", "test_relation_set_2")

        expect:
        relation1 != relation2
    }
}
