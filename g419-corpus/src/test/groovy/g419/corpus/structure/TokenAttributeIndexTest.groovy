package g419.corpus.structure

import spock.lang.Specification

class TokenAttributeIndexTest extends Specification{

    def "adding attribute should return ordinal number"() {
        given:
            TokenAttributeIndex attrIndex = new TokenAttributeIndex()

        expect:
            attrIndex.addAttribute("orth") == 0
            attrIndex.addAttribute("base") == 1
            attrIndex.addAttribute("orth") == 0
    }

    def "getAttribute should return list"() {
        given:
            TokenAttributeIndex attrIndex = new TokenAttributeIndex()
            attrIndex.addAttribute("orth")
            attrIndex.addAttribute("base")

        expect:
            ["orth", "base"] == attrIndex.getAttributes()
    }

    def "TokenAttributeIndex should equal to itself"() {
        given:
            TokenAttributeIndex attributeIndex = new TokenAttributeIndex()

        expect:
            attributeIndex == attributeIndex
    }

    def "Same TokenAttributeIndex's should be equal"() {
        given:
            TokenAttributeIndex attributeIndex0 = new TokenAttributeIndex()
            TokenAttributeIndex attributeIndex1 = new TokenAttributeIndex()
            attributeIndex0.addAttribute("orth")
            attributeIndex1.addAttribute("orth")
            attributeIndex0.addAttribute("base")
            attributeIndex1.addAttribute("base")

        expect:
            attributeIndex0 == attributeIndex1
    }

    def "Different TokenAttributeIndex's should not be equal"() {
        given:
            TokenAttributeIndex attributeIndex0 = new TokenAttributeIndex()
            TokenAttributeIndex attributeIndex1 = new TokenAttributeIndex()
            attributeIndex0.addAttribute("orth")
            attributeIndex1.addAttribute("base")
            attributeIndex0.addAttribute("base")
            attributeIndex1.addAttribute("orth")

        expect:
            attributeIndex0 != attributeIndex1
    }

    def "Attributes should be updated"() {
        given:
            TokenAttributeIndex attrIndex = new TokenAttributeIndex()
            attrIndex.addAttribute("orth")
            attrIndex.addAttribute("base")

        expect:
            attrIndex.getAttributes() == ["orth", "base"]

        when:
            attrIndex.update(["orth", "base", "ctag"])

        then:
            attrIndex.getAttributes() == ["orth", "base", "ctag"]
    }

    def "Attributes should return proper indexes"() {
        given:
            TokenAttributeIndex attrIndex = new TokenAttributeIndex()
            attrIndex.addAttribute("orth")
            attrIndex.addAttribute("base")

        expect:
            attrIndex.getIndex("orth") == 0
            attrIndex.getIndex("base") == 1
            attrIndex.getIndex("ctag") == -1
    }

    def "Attributes should return proper name"() {
        given:
            TokenAttributeIndex attrIndex = new TokenAttributeIndex()
            attrIndex.addAttribute("orth")
            attrIndex.addAttribute("base")

        expect:
            attrIndex.getName(0) == "orth"
            attrIndex.getName(1) == "base"

        when:
            attrIndex.getName(2)

        then:
            thrown IndexOutOfBoundsException
    }

    def "Attributes should have proper length"() {
        given:
            TokenAttributeIndex attrIndex = new TokenAttributeIndex()
            attrIndex.addAttribute("orth")

        expect:
            attrIndex.getLength() == 1

        when:
            attrIndex.addAttribute("base")

        then:
            attrIndex.getLength() == 2
    }

    def "Should return proper attribute values"() {
        given:
            TokenAttributeIndex attrIndex = new TokenAttributeIndex()
            attrIndex.addAttribute("orth")
            Token token = new Token("łódce", new Tag("łódka", "tag", true), attrIndex)

        expect:
            attrIndex.getAttributeValue(token,"orth") == "łódce"
            attrIndex.getAttributeValue(token, "base") == null
    }

    def "Should return all attributes"() {
        given:
            TokenAttributeIndex attrIndex = new TokenAttributeIndex()
            attrIndex.addAttribute("orth")
            attrIndex.addAttribute("base")

        expect:
            attrIndex.allAtributes() == ["orth", "base"]
    }
}