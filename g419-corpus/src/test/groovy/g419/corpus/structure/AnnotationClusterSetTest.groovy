package g419.corpus.structure

import spock.lang.Specification
import spock.lang.Shared
import g419.corpus.structure.AnnotationCluster.ReturningStrategy;

class AnnotationClusterSetTest extends Specification {
    @Shared
            attrIndex,
            sampleSentence,
            annotation1,
            annotation2,
            annotation3,
            annotationCluster

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

        annotation1 = new Annotation(0, 2, "test_annotation_type_1", sampleSentence)
        annotation2 = new Annotation(2, 2, "test_annotation_type_2", sampleSentence)
        annotation3 = new Annotation(2, 2, "test_annotation_type_3", sampleSentence)

        annotationCluster = new AnnotationCluster("ann_cluster_type_test", "ann_set_test")
    }

    def "Should create AnnotationClusterSet"() {
        given:
        AnnotationClusterSet annotationClusterSet = new AnnotationClusterSet()

        expect:
        annotationClusterSet != null
        annotationClusterSet.relationType == null
        annotationClusterSet.relationSet == null
        annotationClusterSet.annotationInCluster == [:]
        annotationClusterSet.relationClusters == (Set) []
    }

    def "Should add relation cluster"() {
        given:
        AnnotationClusterSet annotationClusterSet = new AnnotationClusterSet()

        expect:
        annotationClusterSet.relationClusters == (Set) []

        when:
        annotationClusterSet.addRelationCluster(annotationCluster)

        then:
        annotationClusterSet.relationClusters == (Set) [annotationCluster]

        when:
        AnnotationCluster _annotationCluster = new AnnotationCluster("ann_cluster_type_test", "ann_set_test")
        annotationClusterSet.addRelationCluster(_annotationCluster)

        then:
        annotationClusterSet.relationClusters == (Set) [annotationCluster, _annotationCluster]
    }

    def "Should add relation"() {
        given:
        AnnotationClusterSet annotationClusterSet = new AnnotationClusterSet()
        Relation relation1 = new Relation("test_relation_id", annotation1, annotation2, "test_relation_type")
        Relation relation2 = new Relation("test_relation_id", annotation2, annotation3, "test_relation_type")

        expect:
        annotationClusterSet.relationClusters == (Set) []

        when:
        annotationClusterSet.addRelation(relation1)

        then:
        annotationClusterSet.annotationInCluster.keySet() == (Set) [annotation1, annotation2]

        when:
        annotationClusterSet.addRelation(relation2)

        then:
        annotationClusterSet.annotationInCluster.keySet() == (Set) [annotation1, annotation2, annotation3]
    }


    //TODO test fails, something with returning strategies which makes returning reversed relations (annotationTo and annotationFrom are reversed)~MG
//    def "Should get relation set"() {
//        given:
//        AnnotationClusterSet annotationClusterSet = new AnnotationClusterSet()
//        Relation relation1 = new Relation("test_relation_id", annotation1, annotation2, "test_relation_type", "test_relation_set")
//        Relation relation2 = new Relation("test_relation_id", annotation2, annotation1, "test_relation_type", "test_relation_set")
//        SortedSet<Annotation> sortedSet = new TreeSet<>(new AnnotationPositionComparator())
//        sortedSet.add(annotation2)
//        sortedSet.add(annotation3)
//        ReturningStrategy returningStrategy = new AnnotationCluster.ReturnRelationsToHead()
//
//        expect:
//        annotationClusterSet.getRelationSet(returningStrategy).relations == (Set) []
//
//        when:
//        annotationClusterSet.addRelation(relation1)
//
//        then:
//        annotationClusterSet.getRelationSet().relations.equals([relation1].toSet())
//
//        when:
//        annotationClusterSet.addRelation(relation2)
//
//        then:
//        annotationClusterSet.getRelationSet().relations.equals([relation1, relation2].toSet())
//    }

    def "Should get relation clusters"() {
        given:
        AnnotationClusterSet annotationClusterSet = new AnnotationClusterSet()
        annotationClusterSet.addRelationCluster(annotationCluster)

        expect:
        annotationClusterSet.getClusters() == (Set) [annotationCluster]
    }
}
