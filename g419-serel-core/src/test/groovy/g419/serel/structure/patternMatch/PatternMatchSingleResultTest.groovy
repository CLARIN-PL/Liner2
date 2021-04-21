package g419.serel.structure.patternMatch


import spock.lang.Specification

class PatternMatchSingleResultTest extends Specification {
    def "ConcatenateWith"() {
    }

    def "IsTheSameAs"() {
        given:
            PatternMatchSingleResult p1 = new PatternMatchSingleResult();
            p1.docName = "doc1"
            p1.sentenceNumber = 1;
            p1.relationType = "location"
            p1.idsList = List.of(1, 2, 3)


            PatternMatchSingleResult p2 = new PatternMatchSingleResult();
            p2.docName = "doc1"
            p2.relationType = "location"
            p2.sentenceNumber = 1;
            p2.idsList = List.of(3, 2, 1)
        expect:
            p1.isTheSameAs(p2)
    }

    def "IsTheSameAs should be false when tree set is different"() {
        given:
            PatternMatchSingleResult p1 = new PatternMatchSingleResult();
            p1.docName = "doc1"
            p1.relationType = "location"
            p1.sentenceNumber = 1;
            p1.idsList = List.of(1, 2)


            PatternMatchSingleResult p2 = new PatternMatchSingleResult();
            p2.docName = "doc1"
            p2.relationType = "location"
            p2.sentenceNumber = 1;
            p2.idsList = List.of(3, 2, 1)
        expect:
            !p1.isTheSameAs(p2)
    }

    /*
    def "IsTheSameAs for RelationDesc"() {
        given:
            PatternMatchSingleResult p1 = new PatternMatchSingleResult();
            p1.docName = "doc1"
            p1.sentenceNumber = 1;
            p1.tree = List.of(1, 2, 3)

            RelationDesc rd = RelationDesc.from("location:2:nam_fac_goe:4:nam_loc_gpe_city")
            Sentence sentence = new Sentence();
            rd.setSentence(sentence);

        expect:
            p1.isTheSameAs(rd)
    }

    def "IsTheSameAs for RelationDesc v.2"() {
        given:
            PatternMatchSingleResult p1 = new PatternMatchSingleResult();
            p1.docName = "doc1"
            p1.sentenceNumber = 1;
            p1.tree = List.of(1, 2, 3)

            RelationDesc rd = RelationDesc.from("location:1:nam_fac_goe:3:nam_loc_gpe_city")

        expect:
            !p1.isTheSameAs(rd)
    }

     */

    def "concatenateWith "() {
        given:
            PatternMatchSingleResult p1 = new PatternMatchSingleResult();
            p1.docName = "doc1"
            p1.sentenceNumber = 1;
            p1.idsList = List.of(1, 2, 3)
            p1.patternMatchExtraInfo = new HashMap<>()
            p1.patternMatchExtraInfo.putRole("Opole", "nam_loc_goe", null);


            PatternMatchSingleResult p2 = new PatternMatchSingleResult();
            p2.docName = "doc1"
            p2.sentenceNumber = 1;
            p2.idsList = List.of(3, 4, 5)
            p2.patternMatchExtraInfo = new HashMap<>()
            p2.patternMatchExtraInfo.putRole("Wrocław", "nam_loc_goe", null);

            p1.concatenateWith(p2)

        expect:
            p1.idsList.contains(1)
            p1.idsList.contains(2)
            p1.idsList.contains(3)
            p1.idsList.contains(4)
            p1.idsList.contains(5)
            p1.patternMatchExtraInfo.getRoleMap().size() == 2


    }


}
