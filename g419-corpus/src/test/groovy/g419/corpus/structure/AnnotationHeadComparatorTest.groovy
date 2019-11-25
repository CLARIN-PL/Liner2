package g419.corpus.structure

import spock.lang.Shared
import spock.lang.Specification

class AnnotationHeadComparatorTest extends Specification {
    @Shared
            sampleAnnotation1,
            sampleAnnotation2,
            sampleAnnotation3,
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
        sampleSentence.withId("test_sentence_id")

        sampleAnnotation1 = new Annotation(0, 2,"annotation_test_type_1", sampleSentence)
        sampleAnnotation2 = new Annotation(1, 3,"annotation_test_type_2", sampleSentence)
        sampleAnnotation3 = new Annotation(0, "annotation_test_type_3", sampleSentence)
    }

    def "Same annotations should return 0"() {
        given:
        AnnotationHeadComparator comparator = new AnnotationHeadComparator()

        expect:
        comparator.compare(sampleAnnotation1, sampleAnnotation1) == 0
    }

    def "Different annotations with same heads should return 0"() {
        given:
        AnnotationHeadComparator comparator = new AnnotationHeadComparator()

        expect:
        sampleAnnotation1.head == sampleAnnotation3.head
        comparator.compare(sampleAnnotation1, sampleAnnotation3) == 0
    }

    def "Annotation with head occurring earlier compared with one with head occurring later should return -1"() {
        given:
        AnnotationHeadComparator comparator = new AnnotationHeadComparator()

        expect:
        sampleAnnotation1.head < sampleAnnotation2.head
        comparator.compare(sampleAnnotation1, sampleAnnotation2) == -1
    }

    def "Annotation with head occurring later compared with one with head occurring earlier should return 1"() {
        given:
        AnnotationHeadComparator comparator = new AnnotationHeadComparator()

        expect:
        sampleAnnotation1.head < sampleAnnotation2.head
        comparator.compare(sampleAnnotation2, sampleAnnotation1) == 1
    }

    def "Annotations on sentences with different id or null id should return -1"() {
        given:
        Sentence sampleSentence2 = new Sentence(attrIndex)
        sampleSentence2.addToken(new Token("Ala", new Tag("Ala", "subst:sg:nom:f", true), attrIndex))
        Annotation sampleAnnotation4 = new Annotation(0,"annotation_test_type_1", sampleSentence2)

        AnnotationHeadComparator comparator = new AnnotationHeadComparator()

        expect:
        !sampleSentence2.hasId()
        comparator.compare(sampleAnnotation1, sampleAnnotation4) == -1

        when:
        sampleSentence2.withId("test_sentence_another_id")

        then:
        sampleSentence2.hasId()
        sampleSentence.id != sampleSentence2.id
        comparator.compare(sampleAnnotation1, sampleAnnotation4) == -1
        comparator.compare(sampleAnnotation4, sampleAnnotation1) == -1
    }
}
