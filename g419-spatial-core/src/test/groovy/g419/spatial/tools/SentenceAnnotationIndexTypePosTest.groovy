package g419.spatial.tools

import com.google.common.collect.Lists
import com.google.common.collect.Maps
import g419.corpus.structure.*
import spock.lang.Specification

import java.util.stream.Collector
import java.util.stream.Collectors

class SentenceAnnotationIndexTypePosTest extends Specification {

    def "getLongestAtPos should return valid annotation"() {
        given:
            Sentence sentence = getSampleSentenceWithAnnotations()
            SentenceAnnotationIndexTypePos index = new SentenceAnnotationIndexTypePos(sentence)

        expect:
            index.getLongestAtPos(pos).getId() == an

        where:
            pos || an
            1   || "an1"
            2   || "an1"
            3   || "an5"
            4   || "an3"
            5   || "an3"
    }

    def "getLongestAtPos should return null"() {
        given:
            Sentence sentence = getSampleSentenceWithAnnotations()
            SentenceAnnotationIndexTypePos index = new SentenceAnnotationIndexTypePos(sentence)

        expect:
            index.getLongestAtPos(0) == null
            index.getLongestAtPos(10) == null
    }

    def "hasAnnotationOfTypeAtPosition should return valid results"() {
        given:
            Sentence sentence = getSampleSentenceWithAnnotations()
            SentenceAnnotationIndexTypePos index = new SentenceAnnotationIndexTypePos(sentence)

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

    def "getAnnotationsOfTypeAtPosition should return valid list of annotations"(){
        given:
            Sentence sentence = getSampleSentenceWithAnnotations()
            SentenceAnnotationIndexTypePos index = new SentenceAnnotationIndexTypePos(sentence)

        expect:
            index.getAnnotationsOfTypeAtPos(type, pos).stream().map{an->an.getId()}.collect(Collectors.toList()).sort() == list

        where:
            type       | pos || list
            "artifact" | 0   || []
            "artifact" | 1   || ["an1"]
            "artifact" | 2   || ["an1","an2"]
            "action"   | 2   || []
            "action"   | 4   || ["an5"]
            "person"   | 4   || ["an3","an4"]

    }

    def "getAtPos should return valid list of annotations"() {
        given:
            Sentence sentence = getSampleSentenceWithAnnotations()
            SentenceAnnotationIndexTypePos index = new SentenceAnnotationIndexTypePos(sentence)

        expect:
            index.getAtPos(pos).size() == len

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
    }

    def "getLongestAtPosFromSet should return valid annotation"(){
        given:
            Sentence sentence = getSampleSentence()
            Annotation an0 = new Annotation("an0", 0, 0, "null", sentence)
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
            SentenceAnnotationIndexTypePos index = new SentenceAnnotationIndexTypePos(sentence)
            Map<String,Annotation> idToAnnotation = Maps.newHashMap()
            sentence.getChunks().stream().forEach{a->idToAnnotation.put(a.getId(),a)}

        expect:
            Optional.ofNullable(index.getLongestAtPosFromSet(
                    pos, ans.stream().map{id->idToAnnotation.get(id)}.collect(Collectors.toList()))).orElse(an0).getId() == an

        where:
            pos | ans           || an
              0 | []            || "an0"
              1 | ["an1","an2"] || "an1"
              2 | ["an1","an2"] || "an1"
              2 | ["an2"]       || "an2"
              2 | ["an2","an5"] || "an2"


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
