package g419.spatial.tools

import g419.corpus.structure.Annotation
import g419.corpus.structure.Sentence
import g419.corpus.structure.Tag
import g419.corpus.structure.Token
import g419.corpus.structure.TokenAttributeIndex
import spock.lang.Specification

class SentenceAnnotationIndexTypePosTest extends Specification {

    def "getLongestAtPos should return valid annotation"(){
        given:
            Sentence sentence = getSampleSentence()
            SentenceAnnotationIndexTypePos index = new SentenceAnnotationIndexTypePos(sentence)

        expect:
            index.getLongestAtPos(pos).getId() == an

        where:
            pos || an
              1 || "an1"
              2 || "an1"
              3 || "an5"
              4 || "an3"
              5 || "an3"
    }

    def "getLongestAtPos should return null"(){
        given:
            Sentence sentence = getSampleSentence()
            SentenceAnnotationIndexTypePos index = new SentenceAnnotationIndexTypePos(sentence)

        expect:
            index.getLongestAtPos(0) == null
            index.getLongestAtPos(10) == null
    }

    def "hasAnnotationOfTypeAtPosition should return valid results"(){
        given:
            Sentence sentence = getSampleSentence()
            Annotation an1 = new Annotation(1,2,"artifact",sentence)
            Annotation an2 = new Annotation(2,2,"artifact",sentence)
            Annotation an3 = new Annotation(4,6,"person",sentence)
            Annotation an4 = new Annotation(4,4,"person",sentence)
            Annotation an5 = new Annotation(3,4,"action",sentence)

            sentence.addChunk(an1)
            sentence.addChunk(an2)
            sentence.addChunk(an3)
            sentence.addChunk(an4)
            sentence.addChunk(an5)

            SentenceAnnotationIndexTypePos index = new SentenceAnnotationIndexTypePos(sentence)

        expect:
            index.hasAnnotationOfTypeAtPosition(type, pos) == result

        where:
            type       | pos || result
            "artifact" |   0 ||  false
            "person"   |   0 ||  false
            "xxx"      |   0 ||  false
            "artifact" |   1 ||  true
            "person"   |   1 ||  false
            "artifact" |   2 ||  true
            "person"   |   4 ||  true
            "artifact" |   4 ||  false
            "action"   |   4 ||  true
    }


    /**
     *  Na zielonej łódce stoi rybak z wędką
     *  0- 1------- 2---- 3--- 4---- 5 6----
     *     ┗━an1━━━━━━━━┛      ┗━an3━━━━━━━┛
     *              ┗an2┛      ┗an4┛
     *                    ┗━an5━━━━┛
     */
    def getSampleSentence(){
        TokenAttributeIndex attrIndex = new TokenAttributeIndex()
        attrIndex.addAttribute("orth")
        Sentence sentence = new Sentence()
        sentence.addToken(new Token("Na", new Tag("na","tag",true), attrIndex))
        sentence.addToken(new Token("zielonej", new Tag("zielony","tag",true), attrIndex))
        sentence.addToken(new Token("łódce", new Tag("łódka","tag",true), attrIndex))
        sentence.addToken(new Token("stoi", new Tag("stać","tag",true), attrIndex))
        sentence.addToken(new Token("rybak", new Tag("rybak","tag",true), attrIndex))
        sentence.addToken(new Token("z", new Tag("z","tag",true), attrIndex))
        sentence.addToken(new Token("wędką", new Tag("wędka","tag",true), attrIndex))

        Annotation an1 = new Annotation("an1",1,2,"artifact",sentence)
        Annotation an2 = new Annotation("an2",2,2,"artifact",sentence)
        Annotation an3 = new Annotation("an3",4,6,"person",sentence)
        Annotation an4 = new Annotation("an4",4,4,"person",sentence)
        Annotation an5 = new Annotation("an5",3,4,"action",sentence)

        sentence.addChunk(an1)
        sentence.addChunk(an2)
        sentence.addChunk(an3)
        sentence.addChunk(an4)
        sentence.addChunk(an5)

        return sentence
    }
}
