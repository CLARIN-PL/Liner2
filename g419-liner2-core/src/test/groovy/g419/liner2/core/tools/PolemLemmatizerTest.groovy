package g419.liner2.core.tools

import g419.corpus.structure.*
import spock.lang.Specification

//@Ignore
class PolemLemmatizerTest extends Specification {

    def "lemmatize should return valid lemma for orths, bases, ctags and debug parameters"(){
        given:
            PolemLemmatizer polem = new PolemLemmatizer()

        expect:
            polem.lemmatize("Rady Ministrów", "rada minister", "subst:sg:gen:f subst:pl:gen:m1") == "rada ministrów"
    }

    def "lemmatize should return valid lemma for annotation parameter"(){
        given:
            Sentence sentence = getSampleSentence()
            Annotation an1 = new Annotation("an1", 1, 2, "artifact", sentence)
            sentence.addChunk(an1)
            PolemLemmatizer polem = new PolemLemmatizer()

        expect:
            an1.getLemmaOrText() == "zielonej łódce"

        when:
            polem.lemmatize(an1)

        then:
            an1.getLemmaOrText() == "zielona łódka"
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
        sentence.addToken(new Token("Na", new Tag("na", "prep:loc", true), attrIndex))
        sentence.addToken(new Token("zielonej", new Tag("zielony", "adj:sg:loc:f:pos", true), attrIndex))
        sentence.addToken(new Token("łódce", new Tag("łódka", "subst:sg:loc:f", true), attrIndex))
        sentence.addToken(new Token("stoi", new Tag("stać", "fin:sg:ter:imperf", true), attrIndex))
        sentence.addToken(new Token("rybak", new Tag("rybak", "subst:sg:nom:m1", true), attrIndex))
        sentence.addToken(new Token("z", new Tag("z", "prep:inst:nwok", true), attrIndex))
        sentence.addToken(new Token("wędką", new Tag("wędka", "subst:sg:inst:f", true), attrIndex))
        return sentence
    }
}
