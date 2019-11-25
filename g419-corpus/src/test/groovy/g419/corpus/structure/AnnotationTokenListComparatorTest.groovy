package g419.corpus.structure

import org.spockframework.runtime.SpecificationContext
import spock.lang.Shared
import spock.lang.Specification

class AnnotationTokenListComparatorTest extends Specification {
    @Shared
            sampleSentence1,
            sampleSentence2

    def setup() {
        TokenAttributeIndex attrIndex = new TokenAttributeIndex()
        attrIndex.addAttribute("orth")
        sampleSentence1 = new Sentence(attrIndex)
        sampleSentence1.addToken(new Token("Ala", new Tag("Ala", "subst:sg:nom:f", true), attrIndex))
        sampleSentence1.addToken(new Token("ma", new Tag("mieć", "fin:sg:ter:imperf", true), attrIndex))
        Token tok = new Token("kota", new Tag("kot", "subst:sg:gen:m2", true), attrIndex)
        tok.setNoSpaceAfter(true)
        sampleSentence1.addToken(tok)
        sampleSentence1.addToken(new Token(".", new Tag(".", "interp", true), attrIndex))

        sampleSentence2 = new Sentence(attrIndex)
        sampleSentence2.addToken(new Token("Kot", new Tag("Ala", "subst:sg:nom:m2", true), attrIndex))
        sampleSentence2.addToken(new Token("jest", new Tag("być", "fin:sg:ter:imperf", true), attrIndex))
        Token tok2 = new Token("Ali", new Tag("kot", "subst:sg:gen:f", true), attrIndex)
        tok.setNoSpaceAfter(true)
        sampleSentence2.addToken(tok2)
        sampleSentence2.addToken(new Token(".", new Tag(".", "interp", true), attrIndex))
    }

    def "Should create AnnotationTokenListComparator()"() {
        given:
        AnnotationTokenListComparator comparator = new AnnotationTokenListComparator()

        expect:
        comparator != null

        when:
        comparator = new AnnotationTokenListComparator(true)

        then:
        comparator != null

        when:
        comparator = new AnnotationTokenListComparator(false)

        then:
        comparator != null
    }

    def "Annotation token list should be equal to itself"() {
        given:
        AnnotationTokenListComparator comparator = new AnnotationTokenListComparator(false)
        Annotation annotation1 = new Annotation(0, 2, "test_annotation_type", sampleSentence1)

        expect:
        comparator.compare(annotation1, annotation1) == 0

        when:
        comparator = new AnnotationTokenListComparator(true)

        then:
        comparator.compare(annotation1, annotation1) == 0
    }

    def "Same tokens same type same sentence annotations should be equal"() {
        given:
        AnnotationTokenListComparator comparator = new AnnotationTokenListComparator(false)
        Annotation annotation1 = new Annotation(0, 2, "test_annotation_type", sampleSentence1)
        Annotation annotation2 = new Annotation(0, 2, "test_annotation_type", sampleSentence1)

        expect:
        comparator.compare(annotation1, annotation2) == 0

        when:
        comparator = new AnnotationTokenListComparator(true)

        then:
        comparator.compare(annotation1, annotation2) == 0
    }

    def "Same tokens different type same sentence annotations should be equal only if sameChannel is false"() {
        given:
        AnnotationTokenListComparator comparator = new AnnotationTokenListComparator(false)
        Annotation annotation1 = new Annotation(0, 2, "test_annotation_type_1", sampleSentence1)
        Annotation annotation2 = new Annotation(0, 2, "test_annotation_type_2", sampleSentence1)

        expect:
        comparator.compare(annotation1, annotation2) == 0

        when:
        comparator = new AnnotationTokenListComparator(true)

        then:
        comparator.compare(annotation1, annotation2) == -1
    }

    def "Different tokens same type same sentence annotations should be different"() {
        given:
        AnnotationTokenListComparator comparator = new AnnotationTokenListComparator(false)
        Annotation annotation1 = new Annotation(0, 1, "test_annotation_type", sampleSentence1)
        Annotation annotation2 = new Annotation(0, 2, "test_annotation_type", sampleSentence1)

        expect:
        comparator.compare(annotation1, annotation2) == -1

        when:
        comparator = new AnnotationTokenListComparator(true)

        then:
        comparator.compare(annotation1, annotation2) == -1
    }

    def "Same tokens same type different sentence annotations should be equal"() {
        given:
        AnnotationTokenListComparator comparator = new AnnotationTokenListComparator(false)
        Annotation annotation1 = new Annotation(0, 2, "test_annotation_type", sampleSentence1)
        Annotation annotation2 = new Annotation(0, 2, "test_annotation_type", sampleSentence2)

        expect:
        comparator.compare(annotation1, annotation2) == 0

        when:
        comparator = new AnnotationTokenListComparator(true)

        then:
        comparator.compare(annotation1, annotation2) == 0
    }

    def "Same tokens different type different sentences annotations should be equal only if sameChannel if false"() {
        given:
        AnnotationTokenListComparator comparator = new AnnotationTokenListComparator(false)
        Annotation annotation1 = new Annotation(0, 2, "test_annotation_type_1", sampleSentence1)
        Annotation annotation2 = new Annotation(0, 2, "test_annotation_type_2", sampleSentence2)

        expect:
        comparator.compare(annotation1, annotation2) == 0

        when:
        comparator = new AnnotationTokenListComparator(true)

        then:
        comparator.compare(annotation1, annotation2) == -1
    }

    def "Different tokens same type different sentences annotations should not be equal"() {
        given:
        AnnotationTokenListComparator comparator = new AnnotationTokenListComparator(false)
        Annotation annotation1 = new Annotation(1, 2, "test_annotation_type_1", sampleSentence1)
        Annotation annotation2 = new Annotation(0, 1, "test_annotation_type_1", sampleSentence2)

        expect:
        comparator.compare(annotation1, annotation2) == -1

        when:
        comparator = new AnnotationTokenListComparator(true)

        then:
        comparator.compare(annotation1, annotation2) == -1
    }
}
