package g419.toolbox.wordnet.struct

import spock.lang.Specification

class SynsetTest extends Specification {

    def "Synsets with the same id should be equal"() {
        given:
            def s1 = new Synset(id)
            def s2 = new Synset(id)

        expect:
            s1 == s2

        where:
            id = 100
    }

    def "Synsets with different ids should not be equal"() {
        given:
            def s1 = new Synset(id1)
            def s2 = new Synset(id2)

        expect:
            s1 != s2

        where:
            id1 = 100
            id2 = 200
    }

}
