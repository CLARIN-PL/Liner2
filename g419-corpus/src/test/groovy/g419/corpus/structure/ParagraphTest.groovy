package g419.corpus.structure

import spock.lang.Shared
import spock.lang.Specification

class ParagraphTest extends Specification {
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
    }

    def sampleParagraph() {
        return new Paragraph("test_id", sampleSentence.getAttributeIndex())
    }

    def "Should create paragraph from (String, TokenAttributeIndex)"() {
        given:
        TokenAttributeIndex attrIndex = new TokenAttributeIndex()
        attrIndex.addAttribute("orth")

        when:
        Paragraph paragraph = new Paragraph("test_id", attrIndex)

        then:
        paragraph.id == "test_id"
        paragraph.attributeIndex == attrIndex
    }

    def "Should create paragraph from (String)"() {
        when:
        def paragraph = new Paragraph("test_id")

        then:
        paragraph.id == "test_id"
    }

    def "Should add sentences"() {
        given:
        Paragraph paragraph = sampleParagraph()

        expect:
        paragraph.sentences == []

        when:
        paragraph.addSentence(sampleSentence)

        then:
        paragraph.sentences == [sampleSentence]

        when:
        Sentence sampleSentence2 = sampleSentence.clone()
        sampleSentence2.addToken(new Token("test", new Tag("test", "t:e:s:t", true), attrIndex))
        paragraph.addSentence(sampleSentence2)

        then:
        paragraph.sentences == [sampleSentence, sampleSentence2]
    }

    def "Should set and get attribute index"() {
        given:
        Paragraph paragraph = sampleParagraph()

        expect:
        paragraph.getAttributeIndex() == attrIndex

        when:
        TokenAttributeIndex attrIndex2 = new TokenAttributeIndex()
        attrIndex2.addAttribute("orth")
        paragraph.setAttributeIndex(attrIndex2)

        then:
        !paragraph.getAttributeIndex().is(attrIndex)
        paragraph.getAttributeIndex().is(attrIndex2)
    }

    def "Should get id"() {
        given:
        Paragraph paragraph = sampleParagraph()

        expect:
        paragraph.getId() == "test_id"
    }

    def "Should get sentences"() {
        given:
        Paragraph paragraph = sampleParagraph()

        expect:
        paragraph.getSentences() == []

        when:
        paragraph.addSentence(sampleSentence)

        then:
        paragraph.getSentences() == [sampleSentence]
    }

    def "Should clone paragraph"() {
        given:
        Paragraph paragraph = sampleParagraph()
        Paragraph clonedParagraph = paragraph.clone()

        expect:
        paragraph.getSentences() == clonedParagraph.getSentences()
        !clonedParagraph.is(paragraph)
    }

    def "Should return proper number of sentences"() {
        given:
        Paragraph paragraph = sampleParagraph()

        expect:
        paragraph.numSentences() == 0

        when:
        paragraph.addSentence(sampleSentence)

        then:
        paragraph.numSentences() == 1

        when:
        paragraph.addSentence(sampleSentence.clone())

        then:
        paragraph.numSentences() == 2
    }

    def "Should set chunk meta data"() {
        given:
        Paragraph paragraph = sampleParagraph()

        expect:
        paragraph.chunkMetaData == new HashMap<String, String>()

        when:
        HashMap<String, String> chunkMetaData = new HashMap<>()
        chunkMetaData.put("testKey1", "testMetaData1")
        paragraph.setChunkMetaData(chunkMetaData.clone())

        then:
        paragraph.chunkMetaData == chunkMetaData

        when:
        chunkMetaData.put("testKey2", "testMetaData2")

        then:
        paragraph.chunkMetaData != chunkMetaData

        when:
        paragraph.setChunkMetaData(chunkMetaData)

        then:
        paragraph.chunkMetaData == chunkMetaData
    }

    def "Should get chunk meta data"() {
        given:
        Paragraph paragraph = sampleParagraph()
        HashMap<String, String> chunkMetaData = new HashMap<>()
        chunkMetaData.put("testKey1", "testMetaData1")
        chunkMetaData.put("testKey2", "testMetaData2")
        paragraph.chunkMetaData = chunkMetaData

        expect:
        paragraph.getChunkMetaData("testKey2") == "testMetaData2"
    }

    def "Should get meta data keys"() {
        given:
        Paragraph paragraph = sampleParagraph()
        HashMap<String, String> chunkMetaData = new HashMap<>()
        chunkMetaData.put("testKey1", "testMetaData1")
        chunkMetaData.put("testKey2", "testMetaData2")
        paragraph.chunkMetaData = chunkMetaData

        expect:
        paragraph.getKeysChunkMetaData() == chunkMetaData.keySet()
    }
}
