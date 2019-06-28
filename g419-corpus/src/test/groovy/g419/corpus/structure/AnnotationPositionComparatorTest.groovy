package g419.corpus.structure

import spock.lang.Shared
import spock.lang.Specification

class AnnotationPositionComparatorTest extends Specification {
    @Shared
            sampleSentence1
    @Shared
            sampleSentence2
    @Shared
            comparator
    @Shared
            lower
    @Shared
            equal
    @Shared
            greater

    def setupSpec() {
        lower = -1
        equal = 0
        greater = 1
        comparator = new AnnotationPositionComparator()
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
        sampleSentence2.addToken(new Token("Kot", new Tag("Ala", "subst:sg:gen:m2subst:sg:nom:f", true), attrIndex))
        sampleSentence2.addToken(new Token("jest", new Tag("być", "fin:sg:ter:imperf", true), attrIndex))
        Token tok1 = new Token("Ali", new Tag("kot", "subst:sg:nom:f", true), attrIndex)
        tok.setNoSpaceAfter(true)
        sampleSentence2.addToken(tok1)
        sampleSentence2.addToken(new Token(".", new Tag(".", "interp", true), attrIndex))
    }

    def createSampleDocument() {
        // TODO
    }

    def createSampleParagraph() {
        // TODO
    }

    def "Annotation should be equal to itself"() {
        given:
        Annotation annotation = new Annotation(0, 3, "test_type", sampleSentence1)

        expect:
        comparator.compare(annotation, annotation) == equal
    }

    def "Annotation should be equal to same annotation"() {
        given:
        Annotation annotation1 = new Annotation(0, 3, "test_type", sampleSentence1)
        Annotation annotation2 = new Annotation(0, 3, "test_type", sampleSentence1)

        expect:
        comparator.compare(annotation1, annotation2) == equal
    }

    def "Annotation occurring earlier in sentence should be lower"() {
        given:
        Annotation annotation1 = new Annotation(0, 3, "test_type", sampleSentence1)
        Annotation annotation2 = new Annotation(1, 3, "test_type", sampleSentence1)

        expect:
        comparator.compare(annotation1, annotation2) == lower
    }

    def "Annotation occurring later in sentence should be greater"() {
        Annotation annotation1 = new Annotation(0, 3, "test_type", sampleSentence1)
        Annotation annotation2 = new Annotation(1, 3, "test_type", sampleSentence1)

        expect:
        comparator.compare(annotation2, annotation1) == greater
    }

    def "Annotation with smaller annotation set should be lower"() {
        given:
        Annotation annotation1 = new Annotation(1, 2, "test_type", sampleSentence1)
        Annotation annotation2 = new Annotation(1, 3, "test_type", sampleSentence1)

        expect:
        comparator.compare(annotation1, annotation2) == lower
    }

    def "Annotation with bigger annotation set should be greater"() {
        given:
        Annotation annotation1 = new Annotation(1, 3, "test_type", sampleSentence1)
        Annotation annotation2 = new Annotation(1, 2, "test_type", sampleSentence1)

        expect:
        comparator.compare(annotation1, annotation2) == greater
    }

    // TODO cases when sentences are different
}
