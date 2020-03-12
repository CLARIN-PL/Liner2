package g419.toolbox.wordnet

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject

class Wordnet3Test extends Specification {

    @Subject
    @Shared
    def wordnet = new Wordnet3()

    def "the default constructor should load a valid number of synsets"() {
        when:
            def num = wordnet.getSynsets().size()

        then:
            num == 184240
    }

    def "getSynsets should return valid list of synsets"() {
        when:
            def synsets = wordnet.getSynsets("kot")

        then:
            synsets.size() == 7
    }

}
