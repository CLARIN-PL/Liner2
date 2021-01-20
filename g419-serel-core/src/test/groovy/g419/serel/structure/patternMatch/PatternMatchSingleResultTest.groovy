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

}
