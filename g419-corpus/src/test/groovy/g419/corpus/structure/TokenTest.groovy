package g419.corpus.structure

import spock.lang.Specification

class TokenTest extends  Specification {
    def "Should create Token from TokenAttributeIndex"() {
        given:
        TokenAttributeIndex attrIndex = new TokenAttributeIndex()
        attrIndex.addAttribute("orth")
        Token token = new Token(attrIndex)

        expect:
        token.getAttributesAsString() == "null"
        token.getTags() == []
    }

    def "Should create Token from from String Tag TokenAttributeIndex"() {
        given:
        TokenAttributeIndex attrIndex = new TokenAttributeIndex()
        attrIndex.addAttribute("orth")
        String orth = "łódce"
        Tag tag = new Tag("łódka", "subst:sg:loc:f", true)
        ArrayList<Tag> tagArr = new ArrayList<>()
        tagArr.add(tag)
        Token token = new Token(orth, tag, attrIndex)

        expect:
        token.getAttributesAsString() == "łódce"
        token.getTags() == tagArr
    }

    def "Should clear attributes"() {
        given:
        TokenAttributeIndex attrIndex = new TokenAttributeIndex()
        attrIndex.addAttribute("orth")
        String orth = "łódce"
        Tag tag = new Tag("łódka", "subst:sg:loc:f", true)
        Token token = new Token(orth, tag, attrIndex)

        when:
        token.clearAttributes()

        then:
        token.getAttributesAsString() == ""
    }

    def "Should remove attribute with given index"() {
        given:
        TokenAttributeIndex attrIndex = new TokenAttributeIndex()
        attrIndex.addAttribute("orth")
        String orth = "łódce"
        Tag tag = new Tag("łódka", "subst:sg:loc:f", true)
        Token token = new Token(orth, tag, attrIndex)

        expect:
        token.getAttributeValue(attrIndex.getIndex("orth")) == "łódce"

        when:
        token.removeAttribute(attrIndex.getIndex("orth"))
        String result = token.getAttributeValue(attrIndex.getIndex("orth"))

        then:
        result ==""
        //thrown IndexOutOfBoundsException
    }

    def "Should return attribute value"() {
        given:
        TokenAttributeIndex attrIndex = new TokenAttributeIndex()
        attrIndex.addAttribute("orth")
        String orth = "łódce"
        Tag tag = new Tag("łódka", "subst:sg:loc:f", true)
        Token token = new Token(orth, tag, attrIndex)

        expect:
        token.getAttributeValue(0) == "łódce"
        token.getAttributeValue("orth") == "łódce"
    }

    def "Should return proper number of attributes"() {
        given:
        TokenAttributeIndex attrIndex = new TokenAttributeIndex()
        attrIndex.addAttribute("orth")
        String orth = "łódce"
        Tag tag = new Tag("łódka", "subst:sg:loc:f", true)
        Token token = new Token(orth, tag, attrIndex)

        expect:
        token.getNumAttributes() == 1
    }

    def "Should return orth"() {
        given:
        TokenAttributeIndex attrIndex = new TokenAttributeIndex()
        attrIndex.addAttribute("orth")
        String orth = "łódce"
        Tag tag = new Tag("łódka", "subst:sg:loc:f", true)
        Token token = new Token(orth, tag, attrIndex)

        expect:
        token.getOrth() == "łódce"
    }

    def "Should add proper tag"() {
        given:
        TokenAttributeIndex attrIndex = new TokenAttributeIndex()
        attrIndex.addAttribute("orth")
        Tag tag = new Tag("łódka", "subst:sg:loc:f", true)
        Token token = new Token(attrIndex)

        expect:
        token.getTags() == []

        when:
        token.addTag(tag)

        then:
        token.getTags() == [tag]
    }

    def "Should get disamb bases"() {
        given:
        TokenAttributeIndex attrIndex = new TokenAttributeIndex()
        attrIndex.addAttribute("orth")
        Tag tag1 = new Tag("rok", "ctag", true)
        Tag tag2 = new Tag("R", "ctag", false)
        Token token = new Token("r", tag1, attrIndex)
        token.addTag(tag2)
        Set<String> bases = new HashSet<>()
        bases.add("rok")

        expect:
        token.getDisambBases() == bases
    }

    def "Should print token"() {
        given:
        TokenAttributeIndex attrIndex = new TokenAttributeIndex()
        attrIndex.addAttribute("orth")
        String orth = "łódce"
        Tag tag = new Tag("łódka", "subst:sg:loc:f", true)
        Token token = new Token(orth, tag, attrIndex)

        expect:
        token.toString() ==
                "Token{" +
                "attrIdx=" + attrIndex +
                ", attributes=" + [orth] +
                ", tags=" + [tag] +
                ", props=" + "{}" +
                ", noSpaceAfter=" + false +
                '}'
    }

    def "Should get disamb tags"() {
        given:
        TokenAttributeIndex attrIndex = new TokenAttributeIndex()
        attrIndex.addAttribute("orth")
        String orth = "Łódzi"
        Tag tag1 = new Tag("łódka", "subst:sg:loc:f", false)
        Tag tag2 = new Tag("Łódź", "subst:sg:loc:f", false)
        Tag tag3 = new Tag("test_tag", "test:tag", true)
        Tag tag4 = new Tag("test_tag_2", "test:tag:2", true)
        Token token = new Token(orth, tag1, attrIndex)
        token.addTag(tag2)
        token.addTag(tag3)
        token.addTag(tag4)
        Set set = [tag3, tag4]

        expect:
        token.getDisambTag() == tag3
        token.getDisambTags() == set
        // for some reason groovy returns false at the same time saying in content comparision that there is no difference
        // token.getDisambTags() == [tag3, tag4]
    }

    def "Should set attribute value"() {
        given:
        TokenAttributeIndex attrIndex = new TokenAttributeIndex()
        attrIndex.addAttribute("orth")
        Token token = new Token(attrIndex)

        expect:
        token.setAttributeValue(0, "łódce")
        token.getAttributeValue(0) == "łódce"

        when:
        token.setAttributeValue("orth", "łódkach")

        then:
        token.getAttributeValue("orth") == "łódkach"

    }

    def "Should set no space after"() {
        given:
        TokenAttributeIndex attrIndex = new TokenAttributeIndex()
        attrIndex.addAttribute("orth")
        Token token = new Token(attrIndex)
        token.setNoSpaceAfter(false)

        expect:
        !token.getNoSpaceAfter()

        when:
        token.setNoSpaceAfter(true)

        then:
        token.getNoSpaceAfter()
    }

    def "Should get attributes as string"() {
        given:
        TokenAttributeIndex attrIndex = new TokenAttributeIndex()
        attrIndex.addAttribute("orth")
        attrIndex.addAttribute("test")
        String orth = "łódce"
        Tag tag = new Tag("łódka", "subst:sg:loc:f", true)
        Token token = new Token(orth, tag, attrIndex)

        expect:
        token.getAttributesAsString() == "łódce, null"
    }

    def "Should clone an indetifiable token"() {
        given:
        TokenAttributeIndex attrIndex = new TokenAttributeIndex()
        attrIndex.addAttribute("orth")
        String orth = "łódce"
        Tag tag = new Tag("łódka", "subst:sg:loc:f", true)
        Token token = new Token(orth, tag, attrIndex)
        Token other_token = token.clone()

        expect:
        other_token.getAttributeValue("orth") == token.getAttributeValue("orth")
        other_token.getDisambBases() == token.getDisambBases()
        other_token.getDisambTags() == token.getDisambTags()
        other_token != token
    }

    def "Should check if Token has a given base"() {
        given:
        TokenAttributeIndex attrIndex = new TokenAttributeIndex()
        attrIndex.addAttribute("orth")
        String orth = "łódzi"
        Tag tag1 = new Tag("łódka", "subst:sg:loc:f", false)
        Tag tag2 = new Tag("Łódź", "subst:sg:loc:f", false)
        Token token = new Token(orth, tag1, attrIndex)
        token.addTag(tag2)

        expect:
        token.hasBase("Łódź", false)
        !token.hasBase("Łódź", true)
    }
}
