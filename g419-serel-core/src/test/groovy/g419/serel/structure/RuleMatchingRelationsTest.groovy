package g419.serel.structure

import spock.lang.Specification

class RuleMatchingRelationsTest extends Specification {

    def "should accept single segment as a rule"(){
        given:
        RuleMatchingRelations rmr = RuleMatchingRelations.understandRule(" *")

        expect:
        rmr != null
    }

    def "should accept single segment with XPOS as a rule"(){
        given:
        RuleMatchingRelations rmr = RuleMatchingRelations.understandRule("[subst] *")

        expect:
        rmr != null
    }

    def "should not accept single not valid segment with XPOS as a rule"(){
        given:
        RuleMatchingRelations rmr = RuleMatchingRelations.understandRule("[subst *")

        expect:
        rmr != null
    }




}
