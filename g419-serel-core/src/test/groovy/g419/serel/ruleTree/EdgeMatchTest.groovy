package g419.serel.ruleTree


import g419.corpus.structure.Token
import g419.corpus.structure.TokenAttributeIndex
import spock.lang.Specification

class EdgeMatchTest extends Specification {


    Token token;

    def setup() {
        //sentence = getSampleSentenceWithAnnotations()
        TokenAttributeIndex tai = new TokenAttributeIndex();
        tai.addAttribute("deprel")

        token = new Token(tai);

        token.setAttributeValue("deprel", "nmod")
    }

    def "matches recognizes deprel"() {
        when:
            EdgeMatch em = new EdgeMatch();
            em.depRel = "nmod"
        then:
            em.matches(token) == true
    }

    def "matches recognizes deprel with any"() {
        when:
            EdgeMatch em = new EdgeMatch();
            em.matchAnyDepRel = true
        then:
            em.matches(token) == true
    }


}
