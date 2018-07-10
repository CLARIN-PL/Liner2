package g419.spatial.tools

import com.google.common.collect.Lists
import com.google.common.collect.Maps
import g419.corpus.structure.*
import org.junit.Ignore
import spock.lang.Specification
import spock.lang.Unroll

import java.util.stream.Collectors

class SentenceAnnotationIndexTypePosTest extends Specification {

    Sentence sentence
    SentenceAnnotationIndexTypePos index
    Map<String, Annotation> map
    Annotation an0

    def setup() {
        sentence = getSampleSentenceWithAnnotations()
        index = new SentenceAnnotationIndexTypePos(sentence)
        map = getAnnotationMap(sentence)
        an0 = new Annotation("an0", 0, 0, "null", sentence)
    }

    @Unroll("getLongestAtPos(#pos) should return annotaiton with id=#anId")
    def "getLongestAtPos should return valid annotation"() {
        when:
            def a = index.getLongestAtPos(pos)

        then:
            a.getId() == anId

        where:
            pos || anId
            1   || "an1"
            2   || "an1"
            3   || "an5"
            4   || "an3"
            5   || "an3"
    }

    def "getLongestAtPos should return null"() {
        expect:
            index.getLongestAtPos(0) == null
        and:
            index.getLongestAtPos(10) == null
    }

    @Unroll("hasAnnotationOfTypeAtPosition(#type,#pos) should return #result")
    def "hasAnnotationOfTypeAtPosition should return valid results"() {
        expect:
            index.hasAnnotationOfTypeAtPosition(type, pos) == result

        where:
            type       | pos || result
            "artifact" | 0   || false
            "person"   | 0   || false
            "xxx"      | 0   || false
            "artifact" | 1   || true
            "person"   | 1   || false
            "artifact" | 2   || true
            "person"   | 4   || true
            "artifact" | 4   || false
            "action"   | 4   || true
            "action"   | 9   || false
    }

    @Unroll("getAnnotationsOfTypeAtPosition(#type,#pos) should return #ids")
    def "getAnnotationsOfTypeAtPosition should return valid list of annotations"() {
        when:
            def annotations = index.getAnnotationsOfTypeAtPos(type, pos)

        then:
            toAnnotationIds(annotations) == ids

        where:
            type       | pos || ids
            "artifact" | 0   || []
            "artifact" | 1   || ["an1"]
            "artifact" | 2   || ["an1", "an2"]
            "action"   | 2   || []
            "action"   | 4   || ["an5"]
            "person"   | 4   || ["an3", "an4"]
    }

    @Unroll("getAtPos(#pos) should return #len #ann")
    def "getAtPos should return valid list of annotations"() {
        when:
            def annotations = index.getAtPos(pos)

        then:
            annotations.size() == len

        where:
            pos || len
            0   || 0
            1   || 1
            2   || 2
            3   || 1
            4   || 3
            5   || 1
            6   || 1
            7   || 0
            ann = len == 1 ? "annotation" : "annotations"
    }

    def "getLongestAtPosFromSet should return null for empty list"() {
        when:
            def annotation = index.getLongestAtPosFromSet(0, [])

        then:
            annotation == null
    }

    @Unroll("getLongestAtPosFromSet(#pos,#ans) should return annotation with id=#anId")
    def "getLongestAtPosFromSet should return valid annotation"() {
        when:
            def annotation = index.getLongestAtPosFromSet(pos, toAnnotations(ans))

        then:
            annotation.getId() == anId

        where:
            pos | ans            || anId
            1   | ["an1", "an2"] || "an1"
            2   | ["an1", "an2"] || "an1"
            2   | ["an2"]        || "an2"
            2   | ["an2", "an5"] || "an2"


    }

    def "sortAnnotationsLengthDescBeginAsc should correctly sort a list of annotations"() {
        given:
            Sentence sentence = getSampleSentence()
            Annotation an1 = new Annotation("an1", 1, 2, "artifact", sentence)
            Annotation an2 = new Annotation("an2", 2, 2, "artifact", sentence)
            Annotation an3 = new Annotation("an3", 4, 6, "person", sentence)
            Annotation an4 = new Annotation("an4", 4, 4, "person", sentence)
            Annotation an5 = new Annotation("an5", 3, 4, "action", sentence)
            sentence.addChunk(an1)
            sentence.addChunk(an2)
            sentence.addChunk(an3)
            sentence.addChunk(an4)
            sentence.addChunk(an5)
            List<Annotation> list1 = Lists.newArrayList(an2, an1)
            List<Annotation> list2 = Lists.newArrayList(an3, an1, an5)
            List<Annotation> list3 = Lists.newArrayList(an5, an1, an2, an4)

        when:
            SentenceAnnotationIndexTypePos.sortAnnotationsLengthDescBeginAsc(list1)
        then:
            list1 == [an1, an2]

        when:
            SentenceAnnotationIndexTypePos.sortAnnotationsLengthDescBeginAsc(list2)
        then:
            list2 == [an3, an1, an5]

        when:
            SentenceAnnotationIndexTypePos.sortAnnotationsLengthDescBeginAsc(list3)
        then:
            list3 == [an1, an5, an2, an4]
    }

    @Unroll("getFirstInRangeFromSet(#start,#end,[]) should return null")
    def "getFirstInRangeFromSet should return null"() {
        when:
            def annotation = index.getFirstInRangeFromSet(start, end, [])

        then:
            annotation == null

        where:
            start | end
            0     | 0
            0     | 1
    }

    @Unroll("getFirstInRangeFromSet(#start,#end,#anns) should return annotation with id=#anId")
    def "getFirstInRangeFromSet should return valid annotation"() {
        given:
            def annotation = index.getFirstInRangeFromSet(start, end, toAnnotations(anns))

        expect:
            annotation.getId() == anId

        where:
            start | end | anns           || anId
            0     | 6   | ["an1"]        || "an1"
            0     | 6   | ["an3", "an5"] || "an5"
            4     | 6   | ["an1", "an5"] || "an5"
    }


    @Ignore
    @Unroll("getAnnotationOfTypeStartingFrom(#type, #pos) should return null")
    def "getAnnotationOfTypeStartingFrom should return null"() {
        when:
            def annotation = index.getAnnotationOfTypeStartingFrom(type, pos)

        then:
            annotation.isEmpty()

        where:
            type     | pos
            "any"    | 0
            "any"    | 1
            "action" | 4
    }

    @Unroll("getAnnotationOfTypeStartingFrom(#type, #pos) should return annotation with id=#anId")
    def "getAnnotationOfTypeStartingFrom should return valid value"() {
        when:
            def annotation = index.getAnnotationOfTypeStartingFrom(type, pos)

        then:
            annotation.get().getId() == anId

        where:
            type       | pos || anId
            "artifact" | 1   || "an1"
            "artifact" | 2   || "an2"
            "action"   | 3   || "an5"
    }

    def toAnnotationIds(List<Annotation> annotations) {
        return annotations.stream().map { an -> an.getId() }.collect(Collectors.toList())
    }

    def toAnnotations(List<String> ids) {
        return ids.stream().map { id -> map.get(id) }.collect(Collectors.toList())
    }

    def getAnnotationMap(Sentence sentence) {
        Map<String, Annotation> map = Maps.newHashMap()
        sentence.getChunks().stream().forEach { a -> map.put(a.getId(), a) }
        return map;
    }

    /**
     *  Na zielonej łódce stoi rybak z wędką
     *  0- 1------- 2---- 3--- 4---- 5 6----
     *     ┗━an1━━━━━━━━┛      ┗━an3━━━━━━━┛
     *              ┗an2┛      ┗an4┛
     *                    ┗━an5━━━━┛
     */
    def getSampleSentenceWithAnnotations() {
        Sentence sentence = getSampleSentence()
        sentence.addChunk(new Annotation("an1", 1, 2, "artifact", sentence))
        sentence.addChunk(new Annotation("an2", 2, 2, "artifact", sentence))
        sentence.addChunk(new Annotation("an3", 4, 6, "person", sentence))
        sentence.addChunk(new Annotation("an4", 4, 4, "person", sentence))
        sentence.addChunk(new Annotation("an5", 3, 4, "action", sentence))
        return sentence
    }

    /**
     *  Na zielonej łódce stoi rybak z wędką
     *  0- 1------- 2---- 3--- 4---- 5 6----
     */
    def getSampleSentence() {
        TokenAttributeIndex attrIndex = new TokenAttributeIndex()
        attrIndex.addAttribute("orth")
        Sentence sentence = new Sentence()
        sentence.addToken(new Token("Na", new Tag("na", "tag", true), attrIndex))
        sentence.addToken(new Token("zielonej", new Tag("zielony", "tag", true), attrIndex))
        sentence.addToken(new Token("łódce", new Tag("łódka", "tag", true), attrIndex))
        sentence.addToken(new Token("stoi", new Tag("stać", "tag", true), attrIndex))
        sentence.addToken(new Token("rybak", new Tag("rybak", "tag", true), attrIndex))
        sentence.addToken(new Token("z", new Tag("z", "tag", true), attrIndex))
        sentence.addToken(new Token("wędką", new Tag("wędka", "tag", true), attrIndex))
        return sentence
    }
}
