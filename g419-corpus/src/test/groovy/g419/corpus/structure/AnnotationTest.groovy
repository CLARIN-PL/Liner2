package g419.corpus.structure

import spock.lang.Specification
import spock.lang.Unroll

class AnnotationTest extends Specification {

    def "Constructor should throw an exception for empty list of tokens"() {
        given:
            Sentence sentence = getSampleSentence()

        when:
            Annotation an = new Annotation([] as Set, type, sentence)

        then:
            RuntimeException ex = thrown()
            ex.message == "List of token indices cannot be empty"

        where:
            type = "any type"
    }

    def "Constructor should throw an exception for null annotaiton type"() {
        given:
            Sentence sentence = getSampleSentence()

        when:
            Annotation an1 = new Annotation("an1", 0, 0, null, sentence)

        then:
            RuntimeException ex = thrown()
            ex.message == "Annotation type cannot be null"
    }


    @Unroll
    def "findFirstTokenWithPos should return #index for #pos and indices #tokens"() {
        given:
            Sentence sentence = getSampleSentence()
            Annotation an = new Annotation(tokens as Set, type, sentence)

        when:
            def tokenIndex = an.findFirstTokenWithPos(pos)

        then:
            tokenIndex.getOrElse(-1) == index

        where:
            tokens | pos     || index
            [0, 1] | "ign"   || -1
            [0, 1] | "praet" || 0
            [0, 1] | "aglt"  || 1

            type = "any type"
    }

    Sentence getSampleSentence() {
        TokenAttributeIndex index = new TokenAttributeIndex().with("orth").with("base").with("ctag")

        Sentence sentence = new Sentence(index)
        sentence.addToken(new Token("Spotkał", new Tag("spotkać", "praet:sg:m1:perf", true), index))
        sentence.addToken(new Token("em", new Tag("być", "aglt:sg:pri:imperf:wok", true), index))
        sentence.addToken(new Token("Karola", new Tag("Karol", "subst:sg:acc:m1", true), index))
        sentence.addToken(new Token("w", new Tag("w", "prep:loc:nwok", true), index))
        sentence.addToken(new Token("Zielonej", new Tag("zielony", "adj:sg:loc:f:pos", true), index))
        sentence.addToken(new Token("Górze", new Tag("góra", "subst:sg:loc:f", true), index).withNoSpaceAfter(true))
        sentence.addToken(new Token(".", new Tag(".", "interp", true), index))

        return sentence;
    }

    Document getSampleDocument() {
        Sentence sentence = getSampleSentece()

        Annotation an = new Annotation(4, 5, "loc", sentence).withLemma("Zielona Góra")
        sentence.addChunk(an)

        Paragraph paragraph = new Paragraph("p1", sentence.getAttributeIndex())
        paragraph.addSentence(sentence)

        Document document = new Document("sample", sentence.getAttributeIndex())
        document.addParagraph(paragraph)

        return document
    }
}
