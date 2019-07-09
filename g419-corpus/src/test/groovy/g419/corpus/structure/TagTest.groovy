package g419.corpus.structure

import spock.lang.Specification

class TagTest extends Specification {
    def "Should create tag"() {
        given:
        Tag tag = new Tag()

        expect:
        tag.base == null
        tag.ctag == null
        !tag.disamb
        tag.pos == ""
        tag.cas == ""


        when:
        tag = new Tag("Ala", "subst:sg:nom:f", true)

        then:
        tag.base == "Ala"
        tag.ctag == "subst:sg:nom:f"
        tag.disamb
        tag.pos == "subst"
        tag.cas == "nom"
    }

    def "Should get base"() {
        given:
        Tag tag = new Tag("Ala", "subst:sg:nom:f", true)

        expect:
        tag.getBase() == "Ala"
    }

    def "Should get ctag"() {
        given:
        Tag tag = new Tag("Ala", "subst:sg:nom:f", true)

        expect:
        tag.getCtag() == "subst:sg:nom:f"
    }

    def "Should get disamb"() {
        given:
        Tag tag = new Tag("Ala", "subst:sg:nom:f", true)

        expect:
        tag.getDisamb()
    }

    def "Should get pos"() {
        given:
        Tag tag = new Tag("Ala", "subst:sg:nom:f", true)

        expect:
        tag.getPos() == "subst"
    }

    def "Should get cas"() {
        given:
        Tag tag = new Tag("Ala", "subst:sg:nom:f", true)

        expect:
        tag.getCase() == "nom"
    }

    def "Should set disamb"() {
        given:
        Tag tag = new Tag("Ala", "subst:sg:nom:f", true)

        expect:
        tag.disamb

        when:
        tag.setDisamb(false)

        then:
        !tag.disamb
    }

    def "Should set base"() {
        given:
        Tag tag = new Tag("Ala", "subst:sg:nom:f", true)

        expect:
        tag.base == "Ala"

        when:
        tag.setBase("kot")

        then:
        tag.base == "kot"
    }

    def "Should get as string"() {
        given:
        Tag tag = new Tag("Ala", "subst:sg:nom:f", true)

        expect:
        tag.toString() == "Tag{base='Ala', ctag='subst:sg:nom:f', disamb=true}"
    }
}
