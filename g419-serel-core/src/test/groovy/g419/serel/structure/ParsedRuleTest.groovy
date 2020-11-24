package g419.serel.structure

import org.antlr.v4.runtime.misc.ParseCancellationException
import spock.lang.Specification

class ParsedRuleTest extends Specification {

    def "ParseRule should parse single segment"() {
        given:
        ParsedRule result = ParsedRule.parseRule("miasto")
        result.dump()
        expect:
        result != null
        and:
        result.rootNodeMatch.text.equals("miasto")
    }

    def "ParseRule should not parse single invalid segment"() {
        when:
        ParsedRule result = ParsedRule.parseRule("miasto <")
        then:
        thrown ParseCancellationException
    }

    def "ParseRule should accept single char * as a text pattern"() {
        given:
        ParsedRule result = ParsedRule.parseRule(" * ")
        expect:
        result.rootNodeMatch != null
    }

    def "ParseRule should not accept *  mixed with text as a text pattern"() {
        when:
        ParsedRule result = ParsedRule.parseRule(" mias* ")
        then:
        thrown ParseCancellationException
    }

    def "ParseRule should recognize and accept xPos part"() {
        given:
        ParsedRule result = ParsedRule.parseRule("[subst] miasto ")
        expect:
        result.rootNodeMatch.text.equals("miasto")
        result.rootNodeMatch.getXPos().equals("subst")

    }

    def "ParseRule should recognize namedEntity name with role"() {
        given:
        ParsedRule result = ParsedRule.parseRule("miasto   / nam_geo_loc:source ")
        expect:
        result.rootNodeMatch.text.equals("miasto")
        result.rootNodeMatch.namedEntity.equals("nam_geo_loc")
        result.rootNodeMatch.role.equals("source")
    }

    def "ParseRule should recognize xPos with namedEntity name with role"() {
        given:
        ParsedRule result = ParsedRule.parseRule(" [subst] ^stolica / nam_geo_loc:source ")
        expect:
        result.rootNodeMatch.isMatchLemma() == true
        result.rootNodeMatch.text.equals("stolica")
        result.rootNodeMatch.getXPos().equals("subst")
        result.rootNodeMatch.namedEntity.equals("nam_geo_loc")
        result.rootNodeMatch.role.equals("source")
    }


    def "ParseRule should recognize two segments"() {
        given:
        ParsedRule result = ParsedRule.parseRule("jest > stolicą ")
        expect:
        result.rootNodeMatch.text.equals("stolicą")
        result.rootNodeMatch.edgeMatchList.size() == 1;
        result.rootNodeMatch.edgeMatchList.get(0).side.equals("left")
        result.rootNodeMatch.edgeMatchList.get(0).nodeMatch.text.equals("jest")
    }

    def "ParseRule should recognize depRel of link of two segments"() {
        given:
        ParsedRule result = ParsedRule.parseRule("jest (nmod) > stolicą ")
        expect:
        result.rootNodeMatch.text.equals("stolicą")
        result.rootNodeMatch.edgeMatchList.size() == 1;
        result.rootNodeMatch.edgeMatchList.get(0).side.equals("left")
        result.rootNodeMatch.edgeMatchList.get(0).depRel.equals("nmod")
        result.rootNodeMatch.edgeMatchList.get(0).nodeMatch.text.equals("jest")
    }


    def "ParseRule should recognize relation type"() {
        given:
        ParsedRule result = ParsedRule.parseRule(" location ::  */ nam_fac_goe: source <(root) * <( nmod ) * / nam_loc_gpe_city:target ")
        expect:
        result.relationType.equals("location")
        result.rootNodeMatch.isMatchAny() == true
        result.rootNodeMatch.namedEntity.equals("nam_fac_goe");
        result.rootNodeMatch.role.equals("source");
        result.rootNodeMatch.edgeMatchList.size() == 1;
        result.rootNodeMatch.edgeMatchList.get(0).side.equals("right")
        result.rootNodeMatch.edgeMatchList.get(0).depRel.equals("root")
        result.rootNodeMatch.edgeMatchList.get(0).nodeMatch.isMatchAny() == true
        result.rootNodeMatch.edgeMatchList.get(0).nodeMatch.edgeMatchList.size() == 1;
        result.rootNodeMatch.edgeMatchList.get(0).nodeMatch.edgeMatchList.get(0).depRel.equals("nmod");
        result.rootNodeMatch.edgeMatchList.get(0).nodeMatch.edgeMatchList.get(0).nodeMatch.isMatchAny() == true
        result.rootNodeMatch.edgeMatchList.get(0).nodeMatch.edgeMatchList.get(0).nodeMatch.namedEntity.equals("nam_loc_gpe_city")
        result.rootNodeMatch.edgeMatchList.get(0).nodeMatch.edgeMatchList.get(0).nodeMatch.role.equals("target")


    }

    def "ParseRule should recognize badly formed depRels "() {
        when:
        ParsedRule result = ParsedRule.parseRule(" W > stolicy < * > kraju > *")
        then:
        thrown ParseCancellationException
    }

    def "ParseRule should recognize misspeling in brackets for xPos formed depRels "() {
        when:
        ParsedRule result = ParsedRule.parseRule(" W >[susbst stolicy < * > kraju > *")
        then:
        thrown ParseCancellationException
    }

    def "ParseRule entityName should always have a role  "() {
        when:
        ParsedRule result = ParsedRule.parseRule("Stolica / nam_log_geo < * < kraju")
        then:
        thrown ParseCancellationException
    }

    def "ParseRule - there should be always root node   "() {
        when:
        ParsedRule result = ParsedRule.parseRule("Stolica  < * > kraju")
        then:
        thrown ParseCancellationException
    }

}

