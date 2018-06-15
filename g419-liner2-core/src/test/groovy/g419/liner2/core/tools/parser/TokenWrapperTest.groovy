package g419.liner2.core.tools.parser

import g419.corpus.io.reader.CclSAXStreamReader
import g419.corpus.structure.Document
import g419.corpus.structure.Sentence
import g419.corpus.structure.WrappedToken
import spock.lang.Specification

import java.util.regex.Pattern

class TokenWrapperTest extends Specification {

    def "wrapAnnotations should wrap annotations of a specific type"(){
        given:
            Document document = new CclSAXStreamReader("0099883", getClass().getResourceAsStream("/kpwr-1.2-disamb-rc1/00099883.xml"), null, null).nextDocument()
            List<Pattern> patterns = Arrays.asList(Pattern.compile("nam.*"))

        when:
            Sentence sw0 = TokenWrapper.wrapAnnotations(document.getSentences().get(0), patterns)

        then:
            sw0.getTokens().size() == 1
            sw0.getTokens().get(0).getOrth() == "Toronto"
            ((WrappedToken)sw0.getTokens().get(0)).getFullOrth() == "Toronto Dominion Centre"
            sw0.getTokens().get(0).getDisambTag().getBase() == "Toronto"
            sw0.getTokens().get(0).getDisambTag().getCtag() == "subst:sg:nom:n"


        when:
            Sentence sw1 = TokenWrapper.wrapAnnotations(document.getSentences().get(1), patterns)

        then:
            sw1.getTokens().size() == 14
            sw1.getTokens().get(0).getOrth() == "Toronto"
            ((WrappedToken)sw1.getTokens().get(0)).getFullOrth() == "Toronto Dominion Centre"
            sw1.getTokens().get(9).getOrth() == "Toronto"
            ((WrappedToken)sw1.getTokens().get(9)).getFullOrth() == "Toronto"
            sw1.getTokens().get(12).getOrth() == "Financial"
            ((WrappedToken)sw1.getTokens().get(12)).getFullOrth() == "Financial District"
            sw1.getTokens().get(13).getOrth() == "."
    }

    def "wrapAnnotations should not wrap any annotations for an empty list of patterns"(){
        given:
            Document document = new CclSAXStreamReader("0099883", getClass().getResourceAsStream("/kpwr-1.2-disamb-rc1/00099883.xml"), null, null).nextDocument()
            List<Pattern> patterns = Arrays.asList()

        when:
            Sentence sw0 = TokenWrapper.wrapAnnotations(document.getSentences().get(0), patterns)

        then:
            sw0.getTokens().size() == 3


        when:
            Sentence sw1 = TokenWrapper.wrapAnnotations(document.getSentences().get(1), patterns)

        then:
            sw1.getTokens().size() == 17
    }

    def "wrapAnnotations should wrap all annotations for a null list of patterns"(){
        given:
            Document document = new CclSAXStreamReader("0099883", getClass().getResourceAsStream("/kpwr-1.2-disamb-rc1/00099883.xml"), null, null).nextDocument()
            List<Pattern> patterns = null

        when:
            Sentence sw0 = TokenWrapper.wrapAnnotations(document.getSentences().get(0), patterns)

        then:
            sw0.getTokens().size() == 1
            sw0.getTokens().get(0).getOrth() == "Toronto"
            ((WrappedToken)sw0.getTokens().get(0)).getFullOrth() == "Toronto Dominion Centre"
            sw0.getTokens().get(0).getDisambTag().getBase() == "Toronto"
            sw0.getTokens().get(0).getDisambTag().getCtag() == "subst:sg:nom:n"


        when:
            Sentence sw1 = TokenWrapper.wrapAnnotations(document.getSentences().get(1), patterns)

        then:
            sw1.getTokens().size() == 4
            ((WrappedToken)sw1.getTokens().get(0)).getFullOrth() == "Toronto Dominion Centre"
            sw1.getTokens().get(1).getOrth() == "-"
            // ToDo: Should be "kompleks handlowo-kulturalny w kanadyjskim mieście Toronto, w Financial District"
            ((WrappedToken)sw1.getTokens().get(2)).getFullOrth() == "kompleks handlowo - kulturalny w kanadyjskim mieście Toronto , w Financial District"
            sw1.getTokens().get(3).getOrth() == "."
    }
}
