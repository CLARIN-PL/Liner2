package g419.spatial.pattern

import spock.lang.Specification

class SentencePatternMatchTest extends Specification {

    def "getLabel and hasLabel shout return valid values for a match with a label"(){
        given:
            SentencePatternMatch match = [match: {-> return true}] as SentencePatternMatch

        when:
            match.withLabel(label)

        then:
            match.hasLabel()
        and:
            match.getLabel() == label

        where:
            label = "label"
    }

    def "getLabel and hasLabel shout return valid values for a match without a label"(){
        given:
            SentencePatternMatch match = [match: {-> return true}] as SentencePatternMatch

        expect:
            match.hasLabel() == false
        and:
            match.getLabel() == null
    }

}
