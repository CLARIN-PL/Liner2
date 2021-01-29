package g419.serel.structure.patternMatch

import g419.corpus.structure.RelationDesc
import spock.lang.Specification

class PatternMatchSingleResultTest extends Specification {
    def "ConcatenateWith"() {
    }

    def "IsTheSameAs"() {
        given:
            PatternMatchSingleResult p1 = new PatternMatchSingleResult();
            p1.docName = "doc1"
            p1.sentenceNumber = 1;
            p1.tree = List.of(1, 2, 3)


            PatternMatchSingleResult p2 = new PatternMatchSingleResult();
            p2.docName = "doc1"
            p2.sentenceNumber = 1;
            p2.tree = List.of(3, 2, 1)
        expect:
            p1.isTheSameAs(p2)
    }

    def "IsTheSameAs should be false when tree set is different"() {
        given:
            PatternMatchSingleResult p1 = new PatternMatchSingleResult();
            p1.docName = "doc1"
            p1.sentenceNumber = 1;
            p1.tree = List.of(1, 2)


            PatternMatchSingleResult p2 = new PatternMatchSingleResult();
            p2.docName = "doc1"
            p2.sentenceNumber = 1;
            p2.tree = List.of(3, 2, 1)
        expect:
            !p1.isTheSameAs(p2)
    }

    def "IsTheSameAs for RelationDesc"() {
        given:
            PatternMatchSingleResult p1 = new PatternMatchSingleResult();
            p1.docName = "doc1"
            p1.sentenceNumber = 1;
            p1.tree = List.of(1, 2, 3)

            RelationDesc rd = RelationDesc.from("location:2:nam_fac_goe:4:nam_loc_gpe_city")

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

}
