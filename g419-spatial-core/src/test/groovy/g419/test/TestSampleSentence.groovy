package g419.test

import g419.corpus.structure.*

trait TestSampleSentence {

    /**
     *  Na zielonej łódce stoi rybak z wędką
     *  0- 1------- 2---- 3--- 4---- 5 6----
     *     ┗━an1━━━━━━━━┛      ┗━an3━━━━━━━┛
     *              ┗an2┛      ┗an4┛
     *                    ┗━an5━━━━┛
     *                    ┗━an6━━━━┛
     */
    def getSampleSentenceWithAnnotations() {
        Sentence sentence = getSampleSentence()
        sentence.addChunk(new Annotation("an1", 1, 2, "artifact", sentence))
        sentence.addChunk(new Annotation("an2", 2, 2, "artifact", sentence))
        sentence.addChunk(new Annotation("an3", 4, 6, "person", sentence))
        sentence.addChunk(new Annotation("an4", 4, 4, "person", sentence))
        sentence.addChunk(new Annotation("an5", 3, 4, "action", sentence))
        sentence.addChunk(new Annotation("an6", 3, 4, "artifact", sentence))
        sentence.addChunk(new Annotation("an7", 0, 0, "preposition", sentence))
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
        sentence.addToken(new Token("Na", new Tag("na", "prep:tag", true), attrIndex))
        sentence.addToken(new Token("zielonej", new Tag("zielony", "adj:tag", true), attrIndex))
        sentence.addToken(new Token("łódce", new Tag("łódka", "subst:tag", true), attrIndex))
        sentence.addToken(new Token("stoi", new Tag("stać", "tag", true), attrIndex))
        sentence.addToken(new Token("rybak", new Tag("rybak", "tag", true), attrIndex))
        sentence.addToken(new Token("z", new Tag("z", "prep:tag", true), attrIndex))
        sentence.addToken(new Token("wędką", new Tag("wędka", "tag", true), attrIndex))
        return sentence
    }

}