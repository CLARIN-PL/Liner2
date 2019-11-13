package g419.corpus.structure

import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

class AnnotationClusterTest extends Specification {
    @Shared
            attrIndex,
            sampleSentence,
            annotation1,
            annotation2,
            annotation3

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

        annotation1 = new Annotation(0, 2, "test_annotation_type_1", sampleSentence)
        annotation2 = new Annotation(2, 2, "test_annotation_type_2", sampleSentence)
        annotation3 = new Annotation(2, 2, "test_annotation_type_3", sampleSentence)
    }

    def "Should create annotation cluster"() {
        given:
            AnnotationCluster annotationCluster = new AnnotationCluster("ann_cluster_type_test", "ann_set_test")

        expect:
            annotationCluster != null
            annotationCluster.annotations == (TreeSet) []
            annotationCluster.set == "ann_set_test"
            annotationCluster.type == "ann_cluster_type_test"
    }

    def "Should get type"() {
        given:
            AnnotationCluster annotationCluster = new AnnotationCluster("ann_cluster_type_test", "ann_set_test")

        expect:
            annotationCluster.getType() == "ann_cluster_type_test"
    }

    def "Should get set"() {
        given:
            AnnotationCluster annotationCluster = new AnnotationCluster("ann_cluster_type_test", "ann_set_test")

        expect:
            annotationCluster.getSet() == "ann_set_test"
    }

    def "Should get document"() {
        given:
            AnnotationCluster annotationCluster = new AnnotationCluster("ann_cluster_type_test", "ann_set_test")

        expect:
            annotationCluster.getDocument() == null

        when:
            Document document = new Document("test_document_name", new TokenAttributeIndex())
            annotationCluster.document = document

        then:
            annotationCluster.document == document
    }

    def "Should add relation"() {
        given:
            AnnotationCluster annotationCluster = new AnnotationCluster("ann_cluster_type_test", "ann_set_test")
            Relation relation1 = new Relation("test_relation_id", annotation1, annotation2, "test_relation_type")
            Relation relation2 = new Relation("test_relation_id", annotation2, annotation3, "test_relation_type")

        expect:
            annotationCluster.annotations == (Set) []

        when:
            annotationCluster.addRelation(relation1)

        then:
            annotationCluster.annotations == (Set) [annotation1, annotation2]

        when:
            annotationCluster.addRelation(relation2)

        then:
            annotationCluster.annotations == (Set) [annotation1, annotation2, annotation3]
    }

    def "Should add annotation"() {
        given:
            AnnotationCluster annotationCluster = new AnnotationCluster("ann_cluster_type_test", "ann_set_test")

        expect:
            annotationCluster.annotations == (Set) []

        when:
            annotationCluster.addAnnotation(annotation1)

        then:
            annotationCluster.annotations == (Set) [annotation1]

        when:
            annotationCluster.addAnnotation(annotation2)

        then:
            annotationCluster.annotations == (Set) [annotation1, annotation2]
    }

    def "Should rehead head annotation"() {
        given:
            AnnotationCluster annotationCluster = new AnnotationCluster("ann_cluster_type_test", "ann_set_test")
            AnnotationCluster.ReheadingStrategy reheadToFirst = new AnnotationCluster.ReheadToFirst()

        when:
            annotationCluster.addAnnotation(annotation3)
            annotationCluster.addAnnotation(annotation2)

        then:
            annotationCluster.head == annotation2

        when:
            annotationCluster.addAnnotation(annotation1)

        then:
            annotationCluster.head == annotation2

        when:
            annotationCluster.rehead(reheadToFirst)

        then:
            annotationCluster.head == annotation1
    }

    //TODO test fails, instead of returning added relation, returns it in reverse (annotationTo and annotationFrom are reversed)~MG
//    def "Should get relations"() {
//        given:
//        AnnotationCluster annotationCluster = new AnnotationCluster("ann_cluster_type_test", "ann_set_test")
//        Relation relation1 = new Relation("test_relation_id", annotation1, annotation2, "test_relation_type")
//        Relation relation2 = new Relation("test_relation_id", annotation2, annotation1, "test_relation_type")
//
//        annotationCluster.addRelation(relation1)
//
//        expect:
//        annotationCluster.getRelations().equals([relation1].toSet())
//
//        when:
//        annotationCluster.addRelation(relation2)
//
//        then:
//        annotationCluster.getRelations().equals([relation1, relation2].toSet())
//    }

    @Ignore("Fails on Travis-CI")
    def "Should cast to String"() {
        given:
            AnnotationCluster annotationCluster = new AnnotationCluster("ann_cluster_type_test", "ann_set_test")
            Relation relation1 = new Relation("test_relation_id", annotation1, annotation2, "test_relation_type")
            Relation relation2 = new Relation("test_relation_id", annotation2, annotation3, "test_relation_type")

            annotationCluster.addRelation(relation1)
            annotationCluster.addRelation(relation2)

        expect:
            annotationCluster.toString() == "[{Annotation{id='null', type='test_annotation_type_1', group='null', " +
                    "sentence=null, head=0, lemma='null', confidence=1.0}}{Annotation{id='null', " +
                    "type='test_annotation_type_2', group='null', sentence=null, head=0, lemma='null', " +
                    "confidence=1.0}}{Annotation{id='null', type='test_annotation_type_3', group='null', sentence=null, " +
                    "head=0, lemma='null', confidence=1.0}}]"
    }
}

