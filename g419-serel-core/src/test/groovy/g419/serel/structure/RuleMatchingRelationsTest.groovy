package g419.serel.structure

import spock.lang.Specification

class RuleMatchingRelationsTest extends Specification {

    def "should accept single segment as a rule"(){
        given:
        ParsedRule rmr = ParsedRule.parseRule(" *")

        expect:
        rmr != null
    }

    def "should accept single segment with XPOS as a rule"(){
        given:
        ParsedRule rmr = ParsedRule.parseRule("[subst] *")

        expect:
        rmr != null
    }

    def "should not accept single not valid segment with XPOS as a rule"(){
        given:
        ParsedRule rmr = ParsedRule.parseRule("[subst *")

        expect:
        rmr != null
    }




}
