package g419.corpus.structure

import g419.corpus.EmptySentenceException
import io.vavr.collection.Set
import spock.lang.Shared
import spock.lang.Specification

import java.awt.List

class AnnotationTest extends Specification {
    @Shared
    sampleSentence

    def setup() {
        TokenAttributeIndex attrIndex = new TokenAttributeIndex()
        attrIndex.addAttribute("orth")
        sampleSentence = new Sentence(attrIndex)
        sampleSentence.addToken(new Token("Ala", new Tag("Ala", "subst:sg:nom:f", true), attrIndex))
        sampleSentence.addToken(new Token("ma", new Tag("mieć", "fin:sg:ter:imperf", true), attrIndex))
        Token tok = new Token("kota", new Tag("kot", "subst:sg:gen:m2", true), attrIndex)
        tok.setNoSpaceAfter(true)
        sampleSentence.addToken(tok)
        sampleSentence.addToken(new Token(".", new Tag(".", "interp", true), attrIndex))
        return sampleSentence
    }

    def "Objects with the same values should have same hashes"() {
        given:
        Annotation annotation1 = new Annotation(0, "1", sampleSentence)
        Annotation annotation2 = new Annotation(0, "1", sampleSentence)

        expect:
        annotation1.hashCode() == annotation2.hashCode()
    }

    def "Objects with different values should have same hashes"() {
        given:
        Annotation annotation1 = new Annotation(0, "1", sampleSentence)
        Annotation annotation2 = new Annotation(0, "2", sampleSentence)

        expect:
        annotation1.hashCode() != annotation2.hashCode()
    }

    def "Should create Annotation(int, int, String, Sentence)"() {
        given:
        Annotation annotation = new Annotation(1, 3, "", sampleSentence)

        expect:
        annotation.getType() == ""
        annotation.getSentence() == sampleSentence
        annotation.getTokenCount() == 3
        annotation.getChannelIdx() == 0
        annotation.getHead() == 2

        when:
        annotation = new Annotation(0, 2, "", new Sentence())

        then:
        thrown EmptySentenceException
    }

    def "Should create Annotation(int, String, Sentence)"() {
        given:
        Annotation annotation = new Annotation(0, "", sampleSentence)

        expect:
        annotation.getType() == ""
        annotation.getSentence() == sampleSentence
        annotation.getTokenCount() == 1
        annotation.getChannelIdx() == 0
        annotation.getHead() == 0

        when:
        annotation = new Annotation(0, 2, "", new Sentence())

        then:
        thrown EmptySentenceException
    }

    def "Should create Annotation(int, String, int, Sentence)"() {
        given:
        Annotation annotation = new Annotation(3, "", 1, sampleSentence)

        expect:
        annotation.getType() == ""
        annotation.getSentence() == sampleSentence
        annotation.getTokenCount() == 1
        annotation.getChannelIdx() == 1
        annotation.getHead() == 0

        when:
        annotation = new Annotation(0, 2, "", new Sentence())

        then:
        thrown EmptySentenceException
    }

    def "Should create Annotation(Collection<int>, String, int, Sentence)"() {
        given:
        Annotation annotation = new Annotation([1, 2, 3], "", sampleSentence)

        expect:
        annotation.getType() == ""
        annotation.getSentence() == sampleSentence
        annotation.getTokenCount() == 3
        annotation.getChannelIdx() == 0
        annotation.getHead() == 2

        when:
        annotation = new Annotation(0, 2, "", new Sentence())

        then:
        thrown EmptySentenceException
    }

    def "Should set and get channelIdx"() {
        given:
        Annotation annotation = new Annotation(3, "", 1, sampleSentence)

        expect:
        annotation.getChannelIdx() == 1

        when:
        annotation.setChannelIdx(2)

        then:
        annotation.getChannelIdx() == 2
    }

    def "Should have a head"() {
        given:
        Annotation annotation = new Annotation(1, 3, "", sampleSentence)

        expect:
        annotation.hasHead()
        annotation.setHead(3)
        annotation.hasHead()
    }

    def "Should set and get head"() {
        given:
        Annotation annotation = new Annotation(1, 3, "", sampleSentence)

        expect:
        annotation.getHead() == 2

        when:
        annotation.setHead(3)

        then:
        annotation.getHead() == 3

        when:
        annotation.setHead(15)

        then:
        annotation.getHead() == 3
    }

    def "Should add token"() {
        given:
        Annotation annotation = new Annotation(0, 2, "", sampleSentence)
        int token = 3

        when:
        annotation.addToken(token)

        then:
        annotation.getTokens().contains(token)
    }

    def "Should replace tokens"() {
        given:
        Annotation annotation = new Annotation(0, 2, "", sampleSentence)
        int newBegin = 1
        int newEnd = 3
        TreeSet<Integer> oldTokens = new TreeSet<Integer>()
        oldTokens.add(0)
        oldTokens.add(1)
        oldTokens.add(2)

        expect:
        annotation.getTokens() == oldTokens

        when:
        TreeSet<Integer> newTokens = new TreeSet<Integer>()
        newTokens.add(1)
        newTokens.add(2)
        newTokens.add(3)
        annotation.replaceTokens(newBegin, newEnd)

        then:
        annotation.getTokens() == newTokens
    }

    def "Should get id"() {
        given:
        Annotation annotation = new Annotation(0, 2, "", sampleSentence)
        String id = "test_id"
        annotation.setId(id)

        expect:
        annotation.getId() == id
    }

    def "Should get beginning of annotation"() {
        given:
        Annotation annotation = new Annotation(0, 2, "", sampleSentence)

        expect:
        annotation.getBegin() == 0
    }

    def "Should get ending of annotation"() {
        given:
        Annotation annotation = new Annotation(0, 2, "", sampleSentence)

        expect:
        annotation.getEnd() == 2
    }

    def "Should get tokens"() {
        given:
        Annotation annotation = new Annotation(0, 2, "", sampleSentence)
        def desiredTokens = [0, 1, 2]


        expect:
        for(final int token : annotation.getTokens()){
            desiredTokens.contains(token)
        }
    }

    def "Should get sentence"() {
        given:
        Annotation annotation = new Annotation(0, 2, "", sampleSentence)

        expect:
        annotation.getSentence() == sampleSentence
    }

    def "Should get text"() {
        given:
        Annotation annotation1 = new Annotation(0, 2, "", sampleSentence)
        Annotation annotation2 = new Annotation(0, 2, "", sampleSentence)
        String text1 = annotation1.getText()
        String text2 = annotation2.getText()

        expect:
        text1 == text2
        text1 == text1
        text2 == text2
        text1 != ""

        when:
        Annotation annotation = new Annotation(0, 3, "", sampleSentence)

        then:
        annotation.getText() == "Ala ma kota."
    }

    def "Should set and get type"() {
        given:
        Annotation annotation = new Annotation(0, 2, "test_type", sampleSentence)
        String newType = "new_test_type"

        expect:
        annotation.getType() == "test_type"

        when:
        annotation.setType(newType)

        then:
        annotation.getType() == "new_test_type"
    }

    def "Should sort chunks"() {
        given:
        Annotation annotation1 = new Annotation(0, 2, "", sampleSentence)
        Annotation annotation2 = new Annotation(1, 2, "", sampleSentence)
        Annotation annotation3 = new Annotation(0, 3, "", sampleSentence)
        Annotation annotation4 = new Annotation(2, 3, "", sampleSentence)

        java.util.Set<Annotation> chunks = new HashSet<>()
        chunks.add(annotation1)
        chunks.add(annotation2)
        chunks.add(annotation3)
        chunks.add(annotation4)

        Annotation[] sortedChunks = [annotation1, annotation3, annotation2, annotation4]

        expect:
        sortedChunks == Annotation.sortChunks(chunks)
    }

    def "Should return proper string"() {
        given:
        Annotation annotation = new Annotation(0, 2, "", sampleSentence)

        expect:
        annotation.toString() == "Annotation{" +
                "id='" + annotation.getId() + '\'' +
                ", type='" + annotation.getType() + '\'' +
                ", group='" + "null" + '\'' +
                ", sentence=" + sampleSentence.getId() +
                ", head=" + annotation.getHead() +
                ", lemma='" + annotation.getLemma() + '\'' +
                ", confidence=" + annotation.getConfidence() +
                '}'
    }

    def "Should clone annotation"() {
        given:
        Annotation annotation = new Annotation(0, 2, "", sampleSentence)
        Annotation cloned_annotation = null

        expect:
        annotation != cloned_annotation

        when:
        cloned_annotation = annotation.clone()

        then:
        annotation == cloned_annotation
    }

    def "Should correctly compare Annotations"() {
        given:
        Annotation annotation1 = new Annotation(0, 2, "", sampleSentence)
        Annotation annotation2 = new Annotation(0, 2, "", sampleSentence)
        Annotation annotation3 = new Annotation(1, 3, "", sampleSentence)
        Sentence sampleSentence2 = sampleSentence.clone()
        Annotation annotation4 = new Annotation(1, 3, "", sampleSentence2)

        expect:
        annotation1 == annotation1
        annotation1 == annotation2
        annotation1 != annotation3
        annotation3 == annotation4
    }

    def "Should get text made of bases"() {
        given:
        Annotation annotation = new Annotation(0, 2, "", sampleSentence)

        expect:
        annotation.getBaseText() == "Ala mieć kot"

        when:
        annotation = new Annotation(0, 3, "", sampleSentence)

        then:
        annotation.getBaseText() == "Ala mieć kot."
    }

    def "Should set and get id"() {
        given:
        Annotation annotation = new Annotation("test_id",0, 2, "", sampleSentence)

        expect:
        annotation.getId() == "test_id"

        when:
        annotation.setId("new_test_id")

        then:
        annotation.getId() == "new_test_id"
    }

    def "Should be equal to itself"() {
        given:
        Annotation annotation = new Annotation(0, 2, "", sampleSentence)

        expect:
        annotation.equals(annotation)
    }

    def "Should be equal to same annotation"() {
        given:
        Annotation annotation1 = new Annotation(0, 2, "", sampleSentence)
        Annotation annotation2 = new Annotation(0, 2, "", sampleSentence)

        expect:
        annotation1.equals(annotation2)
    }

    def "Should not be equal to different annotation"() {
        given:
        Annotation annotation1 = new Annotation(0, 2, "", sampleSentence)
        Annotation annotation2 = new Annotation(1, 3, "", sampleSentence)

        expect:
        !annotation1.equals(annotation2)
    }
}
