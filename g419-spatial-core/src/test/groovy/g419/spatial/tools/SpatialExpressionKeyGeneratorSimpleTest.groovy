package g419.spatial.tools

import g419.corpus.structure.Annotation
import g419.corpus.structure.Document
import g419.corpus.structure.Paragraph
import g419.corpus.structure.Sentence
import g419.corpus.structure.Tag
import g419.corpus.structure.Token
import g419.corpus.structure.TokenAttributeIndex
import g419.spatial.structure.SpatialExpression
import spock.lang.Specification

class SpatialExpressionKeyGeneratorSimpleTest extends Specification {

    def "generateKey should return valid key"() {
        given:
            TokenAttributeIndex attrIndex = new TokenAttributeIndex()
            attrIndex.addAttribute("orth")

            Sentence sentence = new Sentence()
            Paragraph paragraph = new Paragraph("p1")
            Document doc = new Document("DocTest", attrIndex)

            sentence.setId("s1")
            sentence.setDocument(doc)
            sentence.addToken(new Token("Na", new Tag("na", "tag", true), attrIndex))
            sentence.addToken(new Token("zielonej", new Tag("zielony", "tag", true), attrIndex))
            sentence.addToken(new Token("łódce", new Tag("łódka", "tag", true), attrIndex))
            sentence.addToken(new Token("stoi", new Tag("stać", "tag", true), attrIndex))
            sentence.addToken(new Token("rybak", new Tag("rybak", "tag", true), attrIndex))
            sentence.addToken(new Token("z", new Tag("z", "tag", true), attrIndex))
            sentence.addToken(new Token("wędką", new Tag("wędka", "tag", true), attrIndex))

            doc.addParagraph(paragraph)

            paragraph.addSentence(sentence)

            Annotation an1 = new Annotation("an1", 0, 0, "spatial_indicator3", sentence)
            Annotation an2 = new Annotation("an2", 1, 2, "landmark3", sentence)
            an2.setHead(2)
            Annotation an3 = new Annotation("an3", 4, 6, "trajector3", sentence)
            an3.setHead(4)

            SpatialExpression se = new SpatialExpression()
            se.setSpatialIndicator(an1)
            se.getLandmark().setSpatialObject(an2)
            se.getTrajector().setSpatialObject(an3)

            SpatialExpressionKeyGeneratorSimple keyGenerator = new SpatialExpressionKeyGeneratorSimple()

        expect:
            keyGenerator.generateKey(se) == "doc:DocTest_sent:s1_tr-so:4_si:0_lm-so:2"


    }
}
