package g419.corpus.structure

import spock.lang.Shared
import spock.lang.Specification

class RelationSetTest extends  Specification {
    @Shared
            sampleAnnotation1,
            sampleAnnotation2,
            sampleAnnotation3,
            sampleSentence,
            attrIndex

    def setup() {
        attrIndex = new TokenAttributeIndex()
        attrIndex.addAttribute("orth")
        sampleSentence = new Sentence(attrIndex)
        sampleSentence.addToken(new Token("Ala", new Tag("Ala", "subst:sg:nom:f", true), attrIndex))
        sampleSentence.addToken(new Token("ma", new Tag("mieć", "fin:sg:ter:imperf", true), attrIndex))
        Token tok = new Token("kota", new Tag("kot", "subst:sg:gen:m2", true), attrIndex)
        tok.setNoSpaceAfter(true)
        sampleSentence.addToken(tok)
        sampleSentence.addToken(new Token(".", new Tag(".", "interp", true), attrIndex))

        sampleAnnotation1 = new Annotation(0, 2,"annotation_test_type_1", sampleSentence)
        sampleAnnotation2 = new Annotation(1, 3,"annotation_test_type_2", sampleSentence)
        sampleAnnotation3 = new Annotation(0, "annotation_test_type_3", sampleSentence)
    }

    def "Should create RelationSet"() {
        given:
        RelationSet relationSet = new RelationSet()

        expect:
        relationSet.relations == (HashSet) []
        relationSet.incomingRelations == [:]
        relationSet.outgoingRelations == [:]
    }

    def "Should add relation"() {
        given:
        RelationSet relationSet = new RelationSet()
        final Relation relation12 = new Relation(sampleAnnotation1, sampleAnnotation2, "relation_test_type")
        final Relation relation23 = new Relation(sampleAnnotation2, sampleAnnotation3, "relation_test_type")
        final Relation relation13 = new Relation(sampleAnnotation1, sampleAnnotation3, "relation_test_type")

        when:
        relationSet.addRelation(relation12)

        def outgoingMap = [:]
        outgoingMap.put(sampleAnnotation1, [relation12].toSet())
        def incomingMap = [:]
        incomingMap.put(sampleAnnotation2, [relation12].toSet())

        then:
        relationSet.relations == (HashSet) [relation12]
        relationSet.outgoingRelations == outgoingMap
        relationSet.incomingRelations == incomingMap

        when:
        relationSet.addRelation(relation23)

        outgoingMap.put(sampleAnnotation2, [relation23].toSet())
        incomingMap.put(sampleAnnotation3, [relation23].toSet())

        then:
        relationSet.relations == (HashSet) [relation12, relation23]
        relationSet.outgoingRelations == outgoingMap
        relationSet.incomingRelations == incomingMap

        when:
        relationSet.addRelation(relation13)

        outgoingMap[sampleAnnotation1].add(relation13)
        incomingMap[sampleAnnotation3].add(relation13)

        then:
        relationSet.relations == (HashSet) [relation12, relation23, relation13]
        relationSet.outgoingRelations == outgoingMap
        relationSet.incomingRelations == incomingMap
    }

    def "Should get relations"() {
        given:
        RelationSet relationSet = new RelationSet()
        final Relation relation12 = new Relation(sampleAnnotation1, sampleAnnotation2, "relation_test_type")
        final Relation relation23 = new Relation(sampleAnnotation2, sampleAnnotation3, "relation_test_type")
        final Relation relation13 = new Relation(sampleAnnotation1, sampleAnnotation3, "relation_test_type")

        expect:
        relationSet.relations == [].toSet()

        when:
        relationSet.addRelation(relation12)

        then:
        relationSet.getRelations() == [relation12].toSet()

        when:
        relationSet.addRelation(relation13)

        then:
        relationSet.getRelations() == [relation12, relation13].toSet()

        when:
        relationSet.addRelation(relation23)

        then:
        relationSet.getRelations() == [relation12, relation13, relation23].toSet()
    }

    def 'Should get incoming relations'() {
        given:
        RelationSet relationSet = new RelationSet()
        final Relation relation12 = new Relation(sampleAnnotation1, sampleAnnotation2, "relation_test_type")
        final Relation relation23 = new Relation(sampleAnnotation2, sampleAnnotation3, "relation_test_type")
        final Relation relation13 = new Relation(sampleAnnotation1, sampleAnnotation3, "relation_test_type")
        def incomingMap = [:]

        expect:
        relationSet.getIncomingRelations() == [:]

        when:
        relationSet.addRelation(relation12)
        incomingMap.put(sampleAnnotation2, [relation12].toSet())

        then:
        relationSet.getIncomingRelations() == incomingMap
        relationSet.getIncomingRelations(sampleAnnotation2) == [relation12].toSet()

        when:
        relationSet.addRelation(relation23)
        incomingMap.put(sampleAnnotation3, [relation23].toSet())

        then:
        relationSet.getIncomingRelations() == incomingMap
        relationSet.getIncomingRelations(sampleAnnotation3) == [relation23].toSet()

        when:
        relationSet.addRelation(relation13)
        incomingMap[sampleAnnotation3].add(relation13)

        then:
        relationSet.getIncomingRelations() == incomingMap
        relationSet.getIncomingRelations(sampleAnnotation3) == [relation23, relation13].toSet()
        relationSet.getIncomingRelations(sampleAnnotation1) == [].toSet()
    }

    def "Shout get outgoing relations"() {
        given:
        RelationSet relationSet = new RelationSet()
        final Relation relation12 = new Relation(sampleAnnotation1, sampleAnnotation2, "relation_test_type")
        final Relation relation23 = new Relation(sampleAnnotation2, sampleAnnotation3, "relation_test_type")
        final Relation relation13 = new Relation(sampleAnnotation1, sampleAnnotation3, "relation_test_type")
        def outgoingMap = [:]

        expect:
        relationSet.getOutgoingRelations() == [:]

        when:
        relationSet.addRelation(relation12)
        outgoingMap.put(sampleAnnotation1, [relation12].toSet())

        then:
        relationSet.getOutgoingRelations() == outgoingMap
        relationSet.getOutgoingRelations(sampleAnnotation1) == [relation12].toSet()

        when:
        relationSet.addRelation(relation23)
        outgoingMap.put(sampleAnnotation2, [relation23].toSet())

        then:
        relationSet.getOutgoingRelations() == outgoingMap
        relationSet.getOutgoingRelations(sampleAnnotation2) == [relation23].toSet()

        when:
        relationSet.addRelation(relation13)
        outgoingMap[sampleAnnotation1].add(relation13)

        then:
        relationSet.getOutgoingRelations() == outgoingMap
        relationSet.getOutgoingRelations(sampleAnnotation1) == [relation12, relation13].toSet()
        relationSet.getOutgoingRelations(sampleAnnotation3) == [].toSet()
    }
}

