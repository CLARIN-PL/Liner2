package g419.corpus.structure

import spock.lang.Shared
import spock.lang.Specification

class RelationAtSameAnnotationComparatorTest extends  Specification {
    @Shared
            sampleSentence,
            attrIndex

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
    }

    def "Should create comparator"() {
        given:
        RelationAtSameAnnotationComparator comparator = new RelationAtSameAnnotationComparator()

        expect:
        comparator != null
    }

    def "Relation should be equal to itself"() {
        given:
        Annotation annotation = new Annotation(0, 2, "annotation_test_type", sampleSentence)
        RelationAtSameAnnotationComparator comparator = new RelationAtSameAnnotationComparator()
        Relation relation = new Relation(annotation, annotation, 'relation_type_test')

        expect:
        comparator.compare(relation, relation) == 0
    }

    def "Same annotations same types relations should be equal"() {
        given:
        RelationAtSameAnnotationComparator comparator = new RelationAtSameAnnotationComparator()
        Annotation annotation1 = new Annotation(0, 2, "annotation_test_type", sampleSentence)
        Annotation annotation2 = new Annotation(1, 3, "annotation_test_type", sampleSentence)
        Relation relation1 = new Relation(annotation1, annotation2, 'relation_type_test')
        Relation relation2 = new Relation(annotation1, annotation2, 'relation_type_test')

        expect:
        comparator.compare(relation1, relation2) == 0
    }

    def "Same annotations different types relations should be equal"() {
        given:
        RelationAtSameAnnotationComparator comparator = new RelationAtSameAnnotationComparator()
        Annotation annotation1 = new Annotation(0, 2, "annotation_test_type", sampleSentence)
        Annotation annotation2 = new Annotation(1, 3, "annotation_test_type", sampleSentence)
        Relation relation1 = new Relation(annotation1, annotation2, 'relation_type_test_1')
        Relation relation2 = new Relation(annotation1, annotation2, 'relation_type_test_2')

        expect:
        comparator.compare(relation1, relation2) == 0
    }

    def "Relation starting on earlier occurring annotation should be lower"() {
        given:
        RelationAtSameAnnotationComparator comparator = new RelationAtSameAnnotationComparator()
        Annotation annotation1 = new Annotation(0, 1, "annotation_test_type", sampleSentence)
        Annotation annotation2 = new Annotation(1, 2, "annotation_test_type", sampleSentence)
        Annotation annotation3 = new Annotation(2, 3, "annotation_test_type", sampleSentence)
        Relation relation1 = new Relation(annotation1, annotation3, 'relation_type_test_1')
        Relation relation2 = new Relation(annotation2, annotation3, 'relation_type_test_2')

        expect:
        comparator.compare(relation1, relation2) == -1
    }

    def "Relation starting on later occurring annotation should be greater"() {
        given:
        RelationAtSameAnnotationComparator comparator = new RelationAtSameAnnotationComparator()
        Annotation annotation1 = new Annotation(0, 1, "annotation_test_type", sampleSentence)
        Annotation annotation2 = new Annotation(1, 2, "annotation_test_type", sampleSentence)
        Annotation annotation3 = new Annotation(2, 3, "annotation_test_type", sampleSentence)
        Relation relation1 = new Relation(annotation2, annotation3, 'relation_type_test_1')
        Relation relation2 = new Relation(annotation1, annotation3, 'relation_type_test_2')

        expect:
        comparator.compare(relation1, relation2) == 1
    }

    def "Relation ending on earlier occurring annotation should be lower"() {
        given:
        RelationAtSameAnnotationComparator comparator = new RelationAtSameAnnotationComparator()
        Annotation annotation1 = new Annotation(0, 1, "annotation_test_type", sampleSentence)
        Annotation annotation2 = new Annotation(0, 2, "annotation_test_type", sampleSentence)
        Annotation annotation3 = new Annotation(0, 3, "annotation_test_type", sampleSentence)
        Relation relation1 = new Relation(annotation1, annotation2, 'relation_type_test_1')
        Relation relation2 = new Relation(annotation1, annotation3, 'relation_type_test_1')

        expect:
        comparator.compare(relation1, relation2) == -1
    }

    def "Relation ending on later occurring annotation should be greater"() {
        given:
        RelationAtSameAnnotationComparator comparator = new RelationAtSameAnnotationComparator()
        Annotation annotation1 = new Annotation(0, 1, "annotation_test_type", sampleSentence)
        Annotation annotation2 = new Annotation(0, 2, "annotation_test_type", sampleSentence)
        Annotation annotation3 = new Annotation(0, 3, "annotation_test_type", sampleSentence)
        Relation relation1 = new Relation(annotation1, annotation2, 'relation_type_test_1')
        Relation relation2 = new Relation(annotation1, annotation3, 'relation_type_test_1')

        expect:
        comparator.compare(relation2, relation1) == 1
    }
}