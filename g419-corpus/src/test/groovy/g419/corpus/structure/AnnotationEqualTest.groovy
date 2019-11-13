package g419.corpus.structure

import spock.lang.Specification
import spock.lang.Unroll

class AnnotationEqualTest extends Specification {

    def setup() {
    }

    @Unroll("equalsWithTrueOnNull(#o1,#o2) should return #result")
    def "equalsWithTrueOnNull should return valid result"() {
        expect:
            Annotation.equalsWithTrueOnNull(o1, o2) == result

        where:
            o1    | o2    || result
            null  | null  || true
            null  | "one" || true
            "one" | null  || true
            "one" | "two" || false
            "one" | "one" || true
    }

    def "Annotation is equal to itself"() {
        given:
            Sentence sentence = getSampleSentence()
            Annotation an1 = new Annotation("an1", 0, 0, "spatial_indicator3", sentence)

        expect:
            an1.equals(an1)
    }

    @Unroll("With an1.id=#id1 and an2.id=#id2 the annotations should be equal")
    def "Two annotations with the same data or missing id are equal"() {
        given:
            Sentence sentence = getSampleSentence()
            Annotation an1 = new Annotation(id1, begin, end, type, sentence)
            Annotation an2 = new Annotation(id2, begin, end, type, sentence)

        expect:
            !an1.is(an2)
        and:
            an1.equals(an2)

        where:
            id1   | id2
            "an1" | "an1"
            null  | "an1"
            "an1" | null
            null  | null

            begin = 0
            end = 0
            type = "spatial_indicator3"
    }

    def "Two annotations with the same data expect id are not equal"() {
        given:
            Sentence sentence = getSampleSentence()
            Annotation an1 = new Annotation("an1", begin, end, type, sentence)
            Annotation an2 = new Annotation("an2", begin, end, type, sentence)

        expect:
            !an1.is(an2)
        and:
            !an1.equals(an2)

        where:
            begin = 0
            end = 0
            type = "spatial_indicator3"
    }

    def "Two annotations with the same data but from sentences with different ids are not equal"() {
        given:
            Annotation an1 = new Annotation(id, begin, end, type, getSampleSentence("s1"))
            Annotation an2 = new Annotation(id, begin, end, type, getSampleSentence("s2"))

        expect:
            !an1.is(an2)
        and:
            !an1.equals(an2)

        where:
            id = "an1"
            begin = 0
            end = 0
            type = "spatial_indicator3"
    }

    @Unroll("Annotation(id=#id1, begin=#begin1, end=#end1, type=#type1) not equal to Annotation(id=#id2, begin=#begin2, end=#end2, type=#type2)")
    def "Two annotations with different attributes are not equal"() {
        given:
            Sentence s = getSampleSentence()
            Annotation an1 = new Annotation(id1, begin1, end1, type1, s)
            Annotation an2 = new Annotation(id2, begin2, end2, type2, s)

        expect:
            !an1.is(an2)
        and:
            !an1.equals(an2)

        where:
            id1   | begin1 | end1 | type1   | id2   | begin2 | end2 | type2
            "id1" | 0      | 1    | "type1" | "id1" | 0      | 1    | "type2"
            "id1" | 0      | 1    | "type1" | "id1" | 0      | 2    | "type1"
    }

    def getSampleSentence(String id = "s1") {
        Sentence sentence = new Sentence(new TokenAttributeIndex().with("orth"))
        sentence.setId(id)
        sentence.addToken(new Token("Na", new Tag("na", "tag", true), sentence.getAttributeIndex()))
        sentence.addToken(new Token("zielonej", new Tag("zielony", "tag", true), sentence.getAttributeIndex()))
        sentence.addToken(new Token("łódce", new Tag("łódka", "tag", true), sentence.getAttributeIndex()))
        sentence.addToken(new Token("stoi", new Tag("stać", "tag", true), sentence.getAttributeIndex()))
        sentence.addToken(new Token("rybak", new Tag("rybak", "tag", true), sentence.getAttributeIndex()))
        sentence.addToken(new Token("z", new Tag("z", "tag", true), sentence.getAttributeIndex()))
        sentence.addToken(new Token("wędką", new Tag("wędka", "tag", true), sentence.getAttributeIndex()))
        return sentence
    }
}
