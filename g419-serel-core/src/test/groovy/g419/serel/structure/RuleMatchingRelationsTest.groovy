package g419.serel.structure

import g419.corpus.structure.Sentence
import g419.corpus.structure.SentenceAnnotationIndexTypePos
import spock.lang.Specification
import spock.lang.Unroll

class RuleMatchingRelationsTest extends Specification {


    @Unroll
    def "#re isRuleElementMatchingSerelPathElement( #se )  should return #result"() {
        given:
        def  rmr =  RuleMatchingRelations.understandRule(re)
        when:
        def res = rmr.isRuleElementMatchingSerelPathElement(0,se)
        then:
        res == result
        where:
        re  | se   || result
        "location::miasto (npos)"  | "miasto (npos)"  || true
        "location:: wieś (mnpos)"   | " wieś    (mnpos)" || true

    }



    @Unroll
    def "#re isRuleElementMatchingSerelPathElement2( #se )  should return #result"() {
        given:
        def  rmr =  RuleMatchingRelations.understandRule(re)
        when:
        def res = rmr.isRuleElementMatchingSerelPathElement(2,se)
        then:
        res == result
        where:
        re  | se   || result
        " location:: [nam_fac_goe] (root) :source < kompleks(appos) < miasto(nmod) < [nam_loc_gpe_city] (nmod) :target "  | " kompleks(appos)"  || true
        " location:: [nam_fac_goe] (root) :source < kompleks(appos) < miasto(nmod) < [nam_loc_gpe_city] (nmod) :target "  | " kompleks(appos)  "  || true

    }





    /*
    def "match for a rule with a single SentencePatternMatchTokenPos condition should return a valid list of frames"() {
        given:
        def matchers = [new SentencePatternMatchTokenPos(pos)]
        def pattern = new SentencePattern(type, matchers)

        when:
        def frames = pattern.match(annotations)

        then:
        frames.size() == 2
        and:
        frames.get(0).getType() == type

        where:
        pos = "prep"
        type = "spatial"
    }
    */


}
