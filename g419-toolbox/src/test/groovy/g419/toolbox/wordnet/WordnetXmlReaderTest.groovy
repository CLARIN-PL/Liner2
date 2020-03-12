package g419.toolbox.wordnet

import g419.toolbox.wordnet.struct.WordnetPl
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject

class WordnetXmlReaderTest extends Specification {

    @Shared
    @Subject
    WordnetPl wordnetPl = WordnetPl30.load()

    def "lexical unit should have all attributes set"() {
        given:
            def lu = wordnetPl.getLexicalUnit(670536)

        expect:
            lu.getId() == 670536
            lu.getName() == "11-godzinny"
            lu.getPos() == "przymiotnik"
            lu.getDomain() == "jak"
            lu.getDescription() == "##K: og. ##D: taki, który trwa jedenaście godzin."
            lu.getWorkstate() == "Nie przetworzone"
            lu.getSource() == "użytkownika"
            lu.getVariant() == 1
    }

    def "synset should have all attributes set"() {
        given:
            def synset = wordnetPl.getSynset(17)

        expect:
            synset.getId() == 17
            synset.getWorkstate() == "Nie przetworzone"
            synset.getSplit() == 1
            synset.getOwner() == ""
            synset.getDefinition() == "prawnik zajmujący się prowadzeniem spraw w sądzie, udzielaniem porad prawnych, obroną oskarżonego"
            synset.getDescription() == ""
            synset.isAbstract() == false
            synset.getLexicalUnits().size() == 3
    }
}
