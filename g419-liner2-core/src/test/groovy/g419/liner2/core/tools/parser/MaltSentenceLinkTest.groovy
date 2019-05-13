package g419.liner2.core.tools.parser

import spock.lang.Specification

class MaltSentenceLinkTest extends Specification {

    def "getters should return values set by setters"(){
        given:
            MaltSentenceLink link = new MaltSentenceLink(0, 1, "link")
            link.setSourceIndex(10)
            link.setTargetIndex(20)
            link.setRelationType("30")

        expect:
            link.getSourceIndex() == 10
            link.getTargetIndex() == 20
            link.getRelationType() == "30"
    }

}
