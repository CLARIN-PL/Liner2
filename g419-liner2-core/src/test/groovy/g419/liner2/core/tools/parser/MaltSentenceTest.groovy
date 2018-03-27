package g419.liner2.core.tools.parser

import g419.corpus.io.reader.CclSAXStreamReader
import g419.corpus.schema.tagset.MappingNkjpToConllPos
import g419.corpus.structure.Document
import g419.corpus.structure.Sentence
import spock.lang.Specification

import java.util.stream.Collectors
import java.util.stream.IntStream

class MaltSentenceTest extends Specification {


    def "getDefaultIfEmpty should return valid value"(){
        expect:
            MaltSentence.getDefaultIfEmpty(a) == b

        where:
            a      || b
            "text" || "text"
            ""     || "_"
            null   || "_"
    }

    def "convertToCoNLL should return valid Conll representation of an document"() {
        given:
            String conll = getClass().getResource("/kpwr-1.2-disamb-rc1/00099883-sent2.conll").text
            Document document = new CclSAXStreamReader("0099883", getClass().getResourceAsStream("/kpwr-1.2-disamb-rc1/00099883.xml"), null, null).nextDocument()

        expect:
            MaltSentence ms = new MaltSentence(document.getSentences().get(1), MappingNkjpToConllPos.get())
            print ms.getMaltData().join("\n")
            ms.getMaltData().join("\n") == conll
    }

    def "getters should return values set by the constructor"() {
        given:
            Document document = new CclSAXStreamReader("0099883", getClass().getResourceAsStream("/kpwr-1.2-disamb-rc1/00099883.xml"), null, null).nextDocument()
            Sentence sentence = document.getSentences().get(1)
            MaltSentence ms = new MaltSentence(sentence, MappingNkjpToConllPos.get())

        expect:
            ms.getSentence() == sentence
            ms.getAnnotations() == sentence.getChunks()
    }

    def "getLink should return null values"(){
        given:
            Document document = new CclSAXStreamReader("0099883", getClass().getResourceAsStream("/kpwr-1.2-disamb-rc1/00099883.xml"), null, null).nextDocument()
            Sentence sentence = document.getSentences().get(1)
            MaltSentence ms = new MaltSentence(sentence, MappingNkjpToConllPos.get())

        expect:
            IntStream.range(0, sentence.getTokens().size()).forEach{i->ms.getLink(0) == null}

    }

    def "setMaltDataAnsLinks should assign valid values to maltData and links"(){
        given:
            Document document = new CclSAXStreamReader("0099883", getClass().getResourceAsStream("/kpwr-1.2-disamb-rc1/00099883.xml"), null, null).nextDocument()
            Sentence sentence = document.getSentences().get(1)
            MaltSentence ms = new MaltSentence(sentence, MappingNkjpToConllPos.get())
            String[] maltData = ms.getMaltData()
            IntStream.range(0, sentence.getTokens().size()).forEach{i->maltData[i]+="\t${i%3+1}\tlink${i}"}
            ms.setMaltDataAndLinks(maltData)

        expect:
            ms.getLink(i).getRelationType() == link
            ms.getLink(i).getSourceIndex() == source
            ms.getLink(i).getTargetIndex() == target

        where:
            i || source | target | link
            0 || 0      | 0      | "link0"
            1 || 1      | 1      | "link1"
            2 || 2      | 2      | "link2"
            3 || 3      | 0      | "link3"
            4 || 4      | 1      | "link4"
            5 || 5      | 2      | "link5"

    }

    def "getLinksByTargetIndex should return valid sets of links"(){
        given:
            Document document = new CclSAXStreamReader("0099883", getClass().getResourceAsStream("/kpwr-1.2-disamb-rc1/00099883.xml"), null, null).nextDocument()
            Sentence sentence = document.getSentences().get(1)
            MaltSentence ms = new MaltSentence(sentence, MappingNkjpToConllPos.get())
            String[] maltData = ms.getMaltData()
            IntStream.range(0, sentence.getTokens().size()).forEach{i->maltData[i]+="\t${i%3+1}\tlink${i}"}
            ms.setMaltDataAndLinks(maltData)

        expect:
            ms.getLinksByTargetIndex(0).stream().map{l->l.getSourceIndex()}.collect(Collectors.toList()) == [0, 3, 6,  9, 12, 15]
            ms.getLinksByTargetIndex(1).stream().map{l->l.getSourceIndex()}.collect(Collectors.toList()) == [1, 4, 7, 10, 13, 16]
            ms.getLinksByTargetIndex(2).stream().map{l->l.getSourceIndex()}.collect(Collectors.toList()) == [2, 5, 8, 11, 14]
            ms.getLinksByTargetIndex(3).stream().map{l->l.getSourceIndex()}.collect(Collectors.toList()) == []
    }

}
