package g419.toolbox.wordnet.struct

import g419.toolbox.wordnet.WordnetPl30
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class WordnetPlTest extends Specification {

    @Shared
    @Subject
    WordnetPl wordnetPl = WordnetPl30.load()

    def "getRoots should return a valist set of synsets"() {
        when:
            def count = wordnetPl.getRoots().size()

        then:
            count == 169343
    }

    @Unroll
    def "getDepth(#synset) should return #depth"() {
        when:
            def result = wordnetPl.getSynset(synset).getDepth()

        then:
            result == depth

        where:
            synset || depth
            28358  || 1 // coś
            1027   || 1 // człowiek
            12842  || 6
            12880  || 5
            7250   || 5
    }

    def "getSynsetsWithLemma should return a valid set of synsets"() {
        when:
            def synset = wordnetPl.getSynsetsWithLemma("mysz")

        then:
            // mysz -> urządzenie elektroniczne (7250), gryzoń z rodziny myszowatych (12842)
            synset.collect { it.getId() } as Set == [7250, 12842] as Set
    }

    @Unroll
    def "getCommonHypernyms (#synset1, #synset2) should return a set of common hypernyms with #common item(s)"() {
        when:
            def synsets = wordnetPl.getCommonHypernyms(
                    wordnetPl.getSynset(synset1), wordnetPl.getSynset(synset2)
            )

        then:
            synsets.size() == common

        where:
            synset1 | synset2 || common
            12842   | 12880   || 20         // mysz (gryzoń)     | królik
            7250    | 12880   || 0          // mysz (urządzenie) | królik
    }

    @Unroll
    def "getShortestDistanceFromSynsetToHypernym(#synset1, #synset2) should return a distance of #distance"() {
        when:
            def pathDistance = wordnetPl.getShortestDistanceFromSynsetToHypernym(
                    wordnetPl.getSynset(synset1), wordnetPl.getSynset(synset2)
            )

        then:
            pathDistance == distance

        where:
            synset1 | synset2 || distance
            12880   | 39497   || 1           // królik | ssak łożyskowy
            12880   | 39520   || 3           // królik | organizm wielokomórkowy
            7250    | 12880   || -1          // ssak łożyskowy | królik
    }

    @Unroll
    def "getShortestPathDistance(#synset1, #synset2) should a distance of #distance"() {
        when:
            def result = wordnetPl.getShortestPathDistance(
                    wordnetPl.getSynset(synset1), wordnetPl.getSynset(synset2)
            )

        then:
            result.orElse(-1) == distance

        where:
            synset1 | synset2 || distance
            12842   | 12880   || 3         // mysz (gryzoń)     | królik
            12842   | 5168    || 4         // mysz (gryzoń)     | kot
            12842   | 4609    || 7         // mysz (gryzoń)     | drzewo (roślina)
            7250    | 12880   || -1        // mysz (urządzenie) | królik
    }

    @Unroll
    def "getDirectHypernyms(#synset) should return #hpernyms hypernym(s)"() {
        when:
            def result = wordnetPl.getDirectHypernyms(wordnetPl.getSynset(synset)).size()

        then:
            result == hypernyms

        where:
            synset || hypernyms
            12880  || 3             // królik  ->  ssak łożyskowy, roślinożerca, zwierzę futerkowe
            471400 || 1             // kraft   ->  piwo
    }

    def "getDirectHiponyms should return valid set of hiponyms"() {
        given:
            def synset = wordnetPl.getSynset(39497)

        when:
            def hiponyms = wordnetPl.getDirectHiponyms(synset)

        then:
            // ssak łożyskowy -> m.in. królik (12880)
            hiponyms.collect { it.getId() } as Set ==
                    [18403, 24299, 389409, 252222, 252224, 4168, 254595, 399130, 252208, 12880, 387652, 252095,
                     252206, 252209, 252267, 249797, 73849, 71989, 252098, 24104, 252093, 12840, 78888, 252207,
                     252225, 39495, 252097, 252212, 252211, 388959, 71988, 12895, 32210, 34140, 12855] as Set
    }

    def "getAllHypernyms should return a valid set of hypernyms"() {
        given:
            def synset = wordnetPl.getSynset(12880)

        when:
            def hypernyms = wordnetPl.getAllHypernyms(synset)

        then:
            hypernyms.size() == 21
    }

}
