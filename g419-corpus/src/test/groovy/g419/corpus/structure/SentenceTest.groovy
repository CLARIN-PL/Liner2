package g419.corpus.structure

import spock.lang.Shared
import spock.lang.Specification

import java.util.regex.Pattern

class SentenceTest extends Specification {
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

    def "Should create sentence"() {
        given:
        sampleSentence = getSampleSentence()

        expect:
        sampleSentence.toString() == "Ala ma kota."
    }

    def "Should add annotation"() {
        given:
        Annotation annotation = new Annotation(0, 3, "", sampleSentence)
        LinkedHashSet<Annotation> chunks = new LinkedHashSet<>()
        chunks.add(annotation)

        expect:
        chunks != sampleSentence.getChunks()

        when:
        sampleSentence.addChunk(annotation)

        then:
        chunks == sampleSentence.getChunks()
    }

    def "Should add set of annotations"() {
        given:
        Annotation annotation1 = new Annotation(0, 3, "", sampleSentence)
        Annotation annotation2 = new Annotation(1, 2, "", sampleSentence)
        Annotation annotation3 = new Annotation(0, 1, "", sampleSentence)
        AnnotationSet chunks = new AnnotationSet(sampleSentence)
        chunks.addChunk(annotation1)
        chunks.addChunk(annotation2)


        expect:
        sampleSentence.getChunks() == new LinkedHashSet()
        !sampleSentence.getChunks().contains(annotation1)

        when:
        sampleSentence.addAnnotations(chunks)

        then:
        sampleSentence.getChunks().contains(annotation1)
        !sampleSentence.getChunks().contains(annotation3)
    }

    def "Should add token"() {
        given:
        TokenAttributeIndex attrIndex = new TokenAttributeIndex()
        attrIndex.addAttribute("orth")

        expect:
        sampleSentence.getTokenNumber() == 4

        when:
        sampleSentence.addToken(new Token("ma", new Tag("mieć", "fin:sg:ter:imperf", true), attrIndex))

        then:
        sampleSentence.getTokenNumber() == 5
    }

    def "Should not have id by default"() {
        expect:
        !sampleSentence.hasId()
    }

    def "Should set and get id"() {
        given:
        def id = "test_id"

        when:
        sampleSentence.setId(id)

        then:
        sampleSentence.getId() == id
        sampleSentence.hasId()
    }

    def "Should get chunks"() {
        given:
        Annotation annotation1 = new Annotation(0, 2, "", sampleSentence)
        Annotation annotation2 = new Annotation(2, 3, "test_type", sampleSentence)
        Annotation annotation3 = new Annotation(0, 1, "test_type", sampleSentence)
        LinkedHashSet<Annotation> chunks = new LinkedHashSet()

        expect:
        sampleSentence.getChunks() == chunks

        when:
        sampleSentence.addChunk(annotation1)
        chunks.add(annotation1)

        then:
        sampleSentence.getChunks() == chunks

        when:
        sampleSentence.addChunk(annotation2)
        sampleSentence.addChunk(annotation3)
        chunks.add(annotation2)
        chunks.add(annotation3)

        then:
        sampleSentence.getChunks() == chunks
    }

    def "Should get chunk at idx"() {
        given:
        Annotation annotation1 = new Annotation(0, 2, "", sampleSentence)
        Annotation annotation2 = new Annotation(2, 3, "test_type", sampleSentence)
        Annotation annotation3 = new Annotation(0, 1, "test_type", sampleSentence)

        expect:
        sampleSentence.getChunksAt(2) == []

        when:
        sampleSentence.addChunk(annotation1)

        then:
        sampleSentence.getChunksAt(2) == [annotation1]

        when:
        sampleSentence.addChunk(annotation2)
        sampleSentence.addChunk(annotation3)

        then:
        sampleSentence.getChunksAt(2) == [annotation1, annotation2]
        sampleSentence.getChunksAt(2, [new Pattern("test_type", 0)]) == [annotation2]
    }

    def "Should get token class label"() {
        given:
        Annotation annotation1 = new Annotation(0, 2, "name", sampleSentence)
        Annotation annotation2 = new Annotation(0, 3, "sentence", sampleSentence)
        sampleSentence.addChunk(annotation1)
        sampleSentence.addChunk(annotation2)

        expect:
        sampleSentence.getTokenClassLabel(0) == "B-sentence#B-name"
        sampleSentence.getTokenClassLabel(1) == "I-sentence#I-name"
        sampleSentence.getTokenClassLabel(3) == "I-sentence"
    }

    def "Should have proper attribute index length"() {
        expect:
        sampleSentence.getAttributeIndexLength() == 1
    }

    def "Should get proper attribute index"() {
        expect:
        sampleSentence.getAttributeIndex().getAttributes() == ["orth"]
    }

    def "Should get proper token number"() {
        given:
        TokenAttributeIndex attrIndex = new TokenAttributeIndex()
        attrIndex.addAttribute("orth")

        expect:
        sampleSentence.getTokenNumber() == 4

        when:
        sampleSentence.addToken(new Token("Test", new Tag("test", "t:e:s:t", true), attrIndex))

        then:
        sampleSentence.getTokenNumber() == 5
    }

    def "Should get tokens"() {
        given:
        TokenAttributeIndex attrIndex = new TokenAttributeIndex()
        attrIndex.addAttribute("orth")
        def tok1 = new Token("Ala", new Tag("Ala", "subst:sg:nom:f", true), attrIndex)
        def tok2 = new Token("ma", new Tag("mieć", "fin:sg:ter:imperf", true), attrIndex)
        def tok3 = new Token("kota", new Tag("kot", "subst:sg:gen:m2", true), attrIndex)
        def tok4 = new Token(".", new Tag(".", "interp", true), attrIndex)

        expect:
        for (Token sampleToken : sampleSentence.getTokens()) {
            [tok1, tok2, tok3, tok4].contains(sampleToken)
        }
    }

    def "Should set attribute index"() {
        given:
        TokenAttributeIndex attrIndex = new TokenAttributeIndex()
        attrIndex.addAttribute("test")

        when:
        sampleSentence.setAttributeIndex(attrIndex)

        then:
        sampleSentence.getAttributeIndex() == attrIndex
    }

    def "Should set annotations"() {
        given:
        Annotation annotation1 = new Annotation(0, 3, "", sampleSentence)
        Annotation annotation2 = new Annotation(1, 2, "", sampleSentence)
        Annotation annotation3 = new Annotation(0, 1, "", sampleSentence)
        AnnotationSet chunks = new AnnotationSet(sampleSentence)

        expect:
        (ArrayList) sampleSentence.getChunks() == []

        when:
        chunks.addChunk(annotation1)
        sampleSentence.setAnnotations(chunks)

        then:
        (ArrayList) sampleSentence.getChunks() == [annotation1]

        when:
        chunks = new AnnotationSet(sampleSentence)
        chunks.addChunk(annotation2)
        chunks.addChunk(annotation3)
        sampleSentence.setAnnotations(chunks)

        then:
        (ArrayList) sampleSentence.getChunks() == [annotation2, annotation3]
    }

    def "Should return annotations as string"() {
        given:
        Annotation annotation1 = new Annotation(0, 3, "test_type1", sampleSentence)
        Annotation annotation2 = new Annotation(1, 2, "test_type2", sampleSentence)

        expect:
        sampleSentence.annotationsToString() == ""

        when:
        sampleSentence.addChunk(annotation1)
        sampleSentence.addChunk(annotation2)

        then:
        sampleSentence.annotationsToString() == "test_type1 | Ala ma kota.\n" \
                                                + "test_type2 | ma kota\n"
    }

    def "Should remove annotations"() {
        given:
        Annotation annotation1 = new Annotation(0, 3, "test_type1", sampleSentence)
        Annotation annotation2 = new Annotation(1, 2, "test_type2", sampleSentence)

        expect:
        sampleSentence.annotationsToString() == ""

        when:
        sampleSentence.addChunk(annotation1)
        sampleSentence.addChunk(annotation2)

        then:
        (ArrayList) sampleSentence.getChunks() == [annotation1, annotation2]

        when:
        sampleSentence.removeAnnotations("test_type1")

        then:
        (ArrayList) sampleSentence.getChunks() == [annotation2]
    }

    def "Should get annotations in channel"() {
        given:
        Annotation annotation1 = new Annotation(0, 3, "test_type1", sampleSentence)
        Annotation annotation2 = new Annotation(1, 2, "test_type1", sampleSentence)
        Annotation annotation3 = new Annotation(1, "test_type1", 1,  sampleSentence)
        Annotation annotation4 = new Annotation(0, 3, "test_type2", sampleSentence)

        expect:
        sampleSentence.getAnnotationInChannel("test_type1", 0) == null

        when:
        sampleSentence.addChunk(annotation1)
        sampleSentence.addChunk(annotation2)

        then:
        sampleSentence.getAnnotationInChannel("test_type1", 0) == annotation1
        sampleSentence.getAnnotationInChannel("test_type1", 1) != annotation2

        when:
        sampleSentence.addChunk(annotation3)

        then:
        sampleSentence.getAnnotationInChannel("test_type1", 1) == annotation3

        when:
        sampleSentence.addChunk(annotation4)

        then:
        sampleSentence.getAnnotationInChannel("test_type2", 0) == annotation4
    }

    def "Should return sentence as string"() {
        expect:
        sampleSentence.toString() == "Ala ma kota."
    }

    def "Should set tokens"() {
        expect:
        sampleSentence.toString() == "Ala ma kota."

        when:
        TokenAttributeIndex attrIndex = new TokenAttributeIndex()
        attrIndex.addAttribute("orth")
        sampleSentence = new Sentence(attrIndex)
        sampleSentence.addToken(new Token("Kot", new Tag("kot", "subst:sg:gen:m2", true), attrIndex))
        sampleSentence.addToken(new Token("jest", new Tag("mieć", "fin:sg:ter:imperf", true), attrIndex))
        Token tok = new Token("Ali", new Tag("Ala", "subst:sg:nom:f", true), attrIndex)
        tok.setNoSpaceAfter(true)
        sampleSentence.addToken(tok)
        sampleSentence.addToken(new Token(".", new Tag(".", "interp", true), attrIndex))\

        then:
        sampleSentence.toString() == 'Kot jest Ali.'
    }

    def "Should clone itself"() {
        given:
        def clonedSentence = sampleSentence.clone()

        expect:
        clonedSentence.toString() == sampleSentence.toString()
        !clonedSentence.is(sampleSentence)
    }
}
