package g419.spatial.tools

import g419.corpus.structure.*
import g419.spatial.converter.DocumentToSpatialExpressionConverter
import g419.spatial.structure.SpatialExpression
import spock.lang.Specification

class DocumentToSpatialExpressionConverterTest extends Specification {

    def "convert should create valid spatial expressions"() {
        given:
            TokenAttributeIndex attrIndex = new TokenAttributeIndex()
            attrIndex.addAttribute("orth")
            Sentence sentence = new Sentence(attrIndex)
            Paragraph paragraph = new Paragraph("p1")
            Document document = new Document("DocTest", attrIndex)

            sentence.setId("s1")
            sentence.setDocument(document)
            sentence.addToken(new Token("Na", new Tag("na", "tag", true), attrIndex))
            sentence.addToken(new Token("zielonej", new Tag("zielony", "tag", true), attrIndex))
            sentence.addToken(new Token("łódce", new Tag("łódka", "tag", true), attrIndex))
            sentence.addToken(new Token("stoi", new Tag("stać", "tag", true), attrIndex))
            sentence.addToken(new Token("rybak", new Tag("rybak", "tag", true), attrIndex))
            sentence.addToken(new Token("z", new Tag("z", "tag", true), attrIndex))
            sentence.addToken(new Token("wędką", new Tag("wędka", "tag", true), attrIndex))

            document.addParagraph(paragraph)

            paragraph.addSentence(sentence)

            Annotation an1 = new Annotation("an1", 0, 0, "spatial_indicator3", sentence)
            Annotation an2 = new Annotation("an2", 1, 2, "spatial_object3", sentence)
            an2.setHead(2)
            Annotation an3 = new Annotation("an3", 4, 6, "spatial_object3", sentence)
            an3.setHead(4)

            sentence.addChunk(an1)
            sentence.addChunk(an2)
            sentence.addChunk(an3)

            Relation rel1 = new Relation(an1, an2, "landmark", "spatial", document)
            Relation rel2 = new Relation(an1, an3, "trajector", "spatial", document)

            DocumentToSpatialExpressionConverter converter = new DocumentToSpatialExpressionConverter()

        when:
            List<SpatialExpression> ses0 = converter.convert(document)
        then:
            ses0.size() == 0

        when:
            document.addRelation(rel1)
            document.addRelation(rel2)
            List<SpatialExpression> ses1 = converter.convert(document)

        then:
            ses1.size() == 1

    }
}
