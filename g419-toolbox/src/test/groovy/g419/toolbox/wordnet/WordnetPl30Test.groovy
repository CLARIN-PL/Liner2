package g419.toolbox.wordnet

import g419.toolbox.wordnet.struct.WordnetPl
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject

class WordnetPl30Test extends Specification {

    @Shared
    @Subject
    WordnetPl wordnetPl = WordnetPl30.load()

    def "wordnetpl loaded from resources should not be null"() {
        expect:
            wordnetPl != null

    }

    def "wordnetpl loaded from resources should have specific number of lexical units"() {
        when:
            def num = wordnetPl.lexicalUnits.size()

        then:
            num == 477881
    }

    def "wordnetpl loaded from resources should have specific number of lexical unit relations"() {
        when:
            def num = wordnetPl.getLexicalUnitRelationCount()

        then:
            num == 316520
    }

    def "wordnetpl loaded from resources should have specific number of synset relations"() {
        when:
            def num = wordnetPl.getSynsetRelationCount()

        then:
            num == 758553
    }

    def "wordnetpl loaded from resources should have specific number of synset relation types"() {
        when:
            def types = wordnetPl.getSynsetRelationTypes()

        then:
            types.size() == 101
    }
}
