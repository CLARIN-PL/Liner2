package g419.serel.ruleTree

import g419.corpus.structure.RelationDesc
import g419.corpus.structure.Token
import g419.corpus.structure.TokenAttributeIndex
import spock.lang.Specification

class NodeMatchTest extends Specification {


    Token token;

    def setup() {
        //sentence = getSampleSentenceWithAnnotations()
        TokenAttributeIndex tai = new TokenAttributeIndex();
        tai.addAttribute("orth")
        tai.addAttribute("lemma")
        tai.addAttribute("xpos")
        tai.addAttribute("misc")

        token = new Token(tai);

        token.setAttributeValue("orth", "stolicą");
        token.setAttributeValue("lemma", "stolica")
        token.setAttributeValue("xpos", "noun")

        List<String> bois = List.of("B-nam_fac_goe", "B-nam_loc_gpe_city")

        RelationDesc relDesc1 = RelationDesc.from("location:1:nam_fac_goe:12:nam_loc_gpe_city");
        RelationDesc relDesc2 = RelationDesc.from("location:1:nam_fac_goe:15:nam_loc_gpe_district");
        List<RelationDesc> namRels = List.of(relDesc1, relDesc2)

        Map<String, Object> miscMap = new HashMap();
        miscMap.put("bois", bois)
        miscMap.put("nam_rels", namRels)

        token.extAttr = miscMap;
    }


    def "matches recognizes orth"() {
        when:
            NodeMatch nm = new NodeMatch();
            nm.text = "stolicą"
        then:
            nm.matches(token) == true
    }

    def "matches recognizes lemma"() {
        when:
            NodeMatch nm = new NodeMatch();
            nm.matchLemma = true;
            nm.text = "stolica"
        then:
            nm.matches(token) == true
    }

    def "matches recognizes match any text"() {
        when:
            NodeMatch nm = new NodeMatch();
            nm.matchAnyText = true;
        then:
            nm.matches(token) == true
    }

    def "matches recognizes xpos"() {
        when:
            NodeMatch nm = new NodeMatch();
            nm.matchAnyText = true
            nm.xPos = "noun"

        then:
            nm.matches(token) == true
    }

    def "matches recognizes nam_rels: namedEntity"() {
        when:
            NodeMatch nm = new NodeMatch();
            nm.matchAnyText = true
            nm.xPos = "noun"
            nm.namedEntity = "nam_fac_goe"

        then:
            nm.matches(token) == true
    }

    def "matches recognizes boi"() {
        when:
            NodeMatch nm = new NodeMatch();
            nm.matchAnyText = true
            nm.namedEntity = "nam_loc_gpe_city"

        then:
            nm.matches(token) == true
    }

    def "matches recognizes multiple boi"() {
        when:
            NodeMatch nm = new NodeMatch();
            nm.matchAnyText = true
            nm.namedEntity = "nam_loc_gpe_city"

            NodeMatch nm2 = new NodeMatch();
            nm2.matchAnyText = true
            nm2.namedEntity = "nam_fac_goe"

        then:
            nm.matches(token) == true
            nm2.matches(token) == true
    }


    def "isLeaf should return true when node is leaf"() {
        when:
            NodeMatch nm = new NodeMatch()
        then:
            nm.isLeaf() == true
    }

    def "isLeaf should return false when node is not leaf"() {
        when:
            NodeMatch nm = new NodeMatch()
            EdgeMatch em = new EdgeMatch();
            nm.getEdgeMatchList().add(em)
            em.setParentNodeMatch(nm)
        then:
            nm.isLeaf() == false
    }

}
