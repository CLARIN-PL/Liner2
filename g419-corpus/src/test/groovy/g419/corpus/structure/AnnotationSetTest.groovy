package g419.corpus.structure

import spock.lang.Shared
import spock.lang.Specification

class AnnotationSetTest extends Specification {
    @Shared
            sampleSentence
    @Shared
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
        return sampleSentence
    }

    def "Should create annotation set"() {
        given:
        AnnotationSet annotationSet = new AnnotationSet(sampleSentence);

        expect:
        annotationSet != null
        annotationSet.sentence == sampleSentence
    }

    def "Should add chunk"() {
        given:
        Annotation annotation = new Annotation(1, "", sampleSentence)
        AnnotationSet annotationSet = new AnnotationSet(sampleSentence)
        annotationSet.addChunk(annotation)

        expect:
        annotationSet.chunkSet().contains(annotation)
        annotationSet.contains(annotation)
    }

    def "Should remove chunk"() {
        given:
        Annotation annotation = new Annotation(1, "", sampleSentence)
        AnnotationSet annotationSet = new AnnotationSet(sampleSentence)
        annotationSet.addChunk(annotation)

        expect:
        annotationSet.removeChunk(annotation)
        !annotationSet.chunkSet().contains(annotation)
        !annotationSet.contains(annotation)
    }

    def "Should chunk set"() {
        given:
        Annotation annotation1 = new Annotation(1, "", sampleSentence);
        Annotation annotation2 = new Annotation(2, "", sampleSentence);
        Annotation annotation3 = new Annotation(3, "", sampleSentence);
        AnnotationSet annotationSet = new AnnotationSet(sampleSentence);

        expect:
        annotationSet.chunkSet() != null

        when:
        annotationSet.addChunk(annotation1)
        annotationSet.addChunk(annotation2)
        annotationSet.addChunk(annotation3)

        then:
        annotationSet.chunkSet().size() == 3
        annotationSet.chunkSet().contains(annotation1)
        annotationSet.chunkSet().contains(annotation2)
        annotationSet.chunkSet().contains(annotation3)
    }

    def "Should get sentence"() {
        given:
        AnnotationSet annotationSet = new AnnotationSet(sampleSentence)

        expect:
        annotationSet.getSentence() != null
        sampleSentence == annotationSet.getSentence()
    }

    def "Should contain annotations"() {
        given:
        Annotation annotation1 = new Annotation(1, "", sampleSentence)
        Annotation annotation2 = new Annotation(2, "", sampleSentence)
        Annotation annotation3 = new Annotation(3, "", sampleSentence)
        AnnotationSet annotationSet = new AnnotationSet(sampleSentence)

        annotationSet.addChunk(annotation1)
        annotationSet.addChunk(annotation2)

        expect:
        annotationSet.contains(annotation1)
        annotationSet.contains(annotation2)
        !annotationSet.contains(annotation3)
    }

    def "Should union sets on the same sentence without intersection"() {
        given:
        Annotation annotation1 = new Annotation(1, "", sampleSentence)
        Annotation annotation2 = new Annotation(2, "", sampleSentence)
        Annotation annotation3 = new Annotation(3, "", sampleSentence)
        AnnotationSet annotationSet1 = new AnnotationSet(sampleSentence)
        AnnotationSet annotationSet2 = new AnnotationSet(sampleSentence)

        annotationSet1.addChunk(annotation1)
        annotationSet1.addChunk(annotation2)
        annotationSet2.addChunk(annotation3)

        when:
        annotationSet1.union(annotationSet2)

        then:
        annotationSet1.contains(annotation1)
        annotationSet1.contains(annotation2)
        annotationSet1.contains(annotation3)
    }

    def "Should union sets on the same sentence with intersection"() {
        given:
        Annotation annotation1 = new Annotation(1, "", sampleSentence)
        Annotation annotation2 = new Annotation(2, "", sampleSentence)
        Annotation annotation3 = new Annotation(3, "", sampleSentence)
        AnnotationSet annotationSet1 = new AnnotationSet(sampleSentence)
        AnnotationSet annotationSet2 = new AnnotationSet(sampleSentence)

        annotationSet1.addChunk(annotation1)
        annotationSet1.addChunk(annotation2)
        annotationSet2.addChunk(annotation2)
        annotationSet2.addChunk(annotation3)

        when:
        annotationSet1.union(annotationSet2);

        then:
        annotationSet1.contains(annotation1)
        annotationSet1.contains(annotation2)
        annotationSet1.contains(annotation3)
    }

    def "Should union equal annotation sets on the same sentence"() {
        given:
        Annotation annotation1 = new Annotation(1, "", sampleSentence)
        Annotation annotation2 = new Annotation(2, "", sampleSentence)
        Annotation annotation3 = new Annotation(3, "", sampleSentence)
        AnnotationSet annotationSet1 = new AnnotationSet(sampleSentence)
        AnnotationSet annotationSet2 = new AnnotationSet(sampleSentence)

        annotationSet1.addChunk(annotation1)
        annotationSet1.addChunk(annotation2)
        annotationSet2.addChunk(annotation1)
        annotationSet2.addChunk(annotation2)

        when:
        annotationSet1.union(annotationSet2)

        then:
        annotationSet1.contains(annotation1)
        annotationSet1.contains(annotation2)
        !annotationSet1.contains(annotation3)
    }

    def "Should union same annotation set on the same sentence"() {
        given:
        Annotation annotation1 = new Annotation(1, "", sampleSentence)
        Annotation annotation2 = new Annotation(2, "", sampleSentence)
        Annotation annotation3 = new Annotation(3, "", sampleSentence)
        AnnotationSet annotationSet1 = new AnnotationSet(sampleSentence)

        annotationSet1.addChunk(annotation1)
        annotationSet1.addChunk(annotation2)

        when:
        annotationSet1.union(annotationSet1)

        then:
        annotationSet1.contains(annotation1)
        annotationSet1.contains(annotation2)
        !annotationSet1.contains(annotation3)
    }

    def "Should pass union of different annotation sets on different sentences"() {
        given:
        Annotation annotation1 = new Annotation(1, "", sampleSentence)
        AnnotationSet annotationSet1 = new AnnotationSet(sampleSentence)

        Sentence sampleSentence2 = sampleSentence.clone()
        Annotation annotation2 = new Annotation(2, "", sampleSentence2)
        AnnotationSet annotationSet2 = new AnnotationSet(sampleSentence2)

        annotationSet1.addChunk(annotation1)
        annotationSet2.addChunk(annotation2)

        when:
        annotationSet1.union(annotationSet2);

        then:
        annotationSet1.contains(annotation1)
        !annotationSet1.contains(annotation2)
    }

    def "Should return annotation types in empty annotation set"() {
        given:
        AnnotationSet annotationSet = new AnnotationSet(sampleSentence)

        expect:
        annotationSet.getAnnotationTypes() != null
        annotationSet.getAnnotationTypes().isEmpty()
    }

    def "Should return annotation types in annotation set filled with unique types"() {
        given:
        Annotation annotation1 = new Annotation(1, "1", sampleSentence)
        Annotation annotation2 = new Annotation(2, "2", sampleSentence)
        Annotation annotation3 = new Annotation(3, "3", sampleSentence)
        AnnotationSet annotationSet = new AnnotationSet(sampleSentence)

        when:
        annotationSet.addChunk(annotation1)
        annotationSet.addChunk(annotation2)
        annotationSet.addChunk(annotation3)

        then:
        annotationSet.getAnnotationTypes().size() == 3
        annotationSet.getAnnotationTypes().contains(annotation1.getType())
        !annotationSet.getAnnotationTypes().contains("4")
    }

    def "Should return annotation types in annotation set filled with same type"() {
        given:
        Annotation annotation1 = new Annotation(1, "1", sampleSentence)
        Annotation annotation2 = new Annotation(2, "1", sampleSentence)
        Annotation annotation3 = new Annotation(3, "2", sampleSentence)
        AnnotationSet annotationSet = new AnnotationSet(sampleSentence)

        when:
        annotationSet.addChunk(annotation1)
        annotationSet.addChunk(annotation2)

        then:
        annotationSet.getAnnotationTypes().size() == 1
        annotationSet.getAnnotationTypes().contains(annotation1.getType())
        annotationSet.getAnnotationTypes().contains(annotation2.getType())
        !annotationSet.getAnnotationTypes().contains(annotation3.getType())
    }

    def "Should return annotation types in annotation set filled with mixed types"() {
        given:
        Annotation annotation1 = new Annotation(1, "1", sampleSentence)
        Annotation annotation2 = new Annotation(2, "1", sampleSentence)
        Annotation annotation3 = new Annotation(3, "2", sampleSentence)
        AnnotationSet annotationSet = new AnnotationSet(sampleSentence)

        when:
        annotationSet.addChunk(annotation1)
        annotationSet.addChunk(annotation2)
        annotationSet.addChunk(annotation3)

        then:
        annotationSet.getAnnotationTypes().size() == 2
        annotationSet.getAnnotationTypes().contains(annotation1.getType())
        annotationSet.getAnnotationTypes().contains(annotation2.getType())
        annotationSet.getAnnotationTypes().contains(annotation3.getType())
        !annotationSet.getAnnotationTypes().contains("3")
    }

    def "Empty sets should be equal"() {
        given:
        AnnotationSet annotationSet1 = new AnnotationSet(sampleSentence)
        AnnotationSet annotationSet2 = new AnnotationSet(sampleSentence)

        expect:
        annotationSet1.equals(annotationSet2)
    }

    def "Same sets on same sentence should be equal"() {
        given:
        Annotation annotation1 = new Annotation(1, "1", sampleSentence)
        Annotation annotation2 = new Annotation(1, "1", sampleSentence)

        AnnotationSet annotationSet1 = new AnnotationSet(sampleSentence)
        AnnotationSet annotationSet2 = new AnnotationSet(sampleSentence)

        annotationSet1.addChunk(annotation1)
        annotationSet1.addChunk(annotation1)

        annotationSet2.addChunk(annotation2)
        annotationSet2.addChunk(annotation2)

        expect:
        annotationSet1 == annotationSet2
    }

    def "Same sets on different sentence should not be equal"() {
        given:
        Annotation annotation1 = new Annotation(1, "1", sampleSentence)
        Sentence anotherSentence = sampleSentence.clone()
        anotherSentence.addToken(new Token(".", new Tag(".", "interp", true), attrIndex))
        Annotation annotation2 = new Annotation(1, "1", anotherSentence)

        AnnotationSet annotationSet1 = new AnnotationSet(sampleSentence)
        AnnotationSet annotationSet2 = new AnnotationSet(anotherSentence)

        annotationSet1.addChunk(annotation1)

        annotationSet2.addChunk(annotation2)

        expect:
        annotationSet1 != annotationSet2
    }

    def "Different sets on same sentence should not be equal"() {
        given:
        Annotation annotation1 = new Annotation(1, "1", sampleSentence)
        Annotation annotation2 = new Annotation(2, "1", sampleSentence)

        AnnotationSet annotationSet1 = new AnnotationSet(sampleSentence)
        AnnotationSet annotationSet2 = new AnnotationSet(sampleSentence)

        annotationSet1.addChunk(annotation1)
        annotationSet1.addChunk(annotation1)

        annotationSet2.addChunk(annotation2)
        annotationSet2.addChunk(annotation2)

        expect:
        annotationSet1 != annotationSet2
    }

    def "Different sets on different sentence should not be equal"() {
        given:
        Annotation annotation1 = new Annotation(1, "1", sampleSentence)
        Sentence anotherSentence = sampleSentence.clone()
        anotherSentence.addToken(new Token(".", new Tag(".", "interp", true), attrIndex))
        Annotation annotation2 = new Annotation(2, "1", anotherSentence)

        AnnotationSet annotationSet1 = new AnnotationSet(sampleSentence)
        AnnotationSet annotationSet2 = new AnnotationSet(anotherSentence)

        annotationSet1.addChunk(annotation1)

        annotationSet2.addChunk(annotation2)

        expect:
        annotationSet1 != annotationSet2
    }
}
