package g419.spatial.tools

import g419.corpus.structure.Annotation
import g419.corpus.structure.Sentence
import g419.spatial.structure.SpatialExpression
import g419.test.TestSampleSentence
import spock.lang.Specification

class SpatialExpressionKeyGeneratorSimpleTest extends Specification implements TestSampleSentence {

    Sentence sentence
    SpatialExpressionKeyGeneratorSimple keyGenerator

    def setup() {
        sentence = getSampleSentence()
        keyGenerator = new SpatialExpressionKeyGeneratorSimple()
    }


    def "generateKey should return valid key"() {
        given:
            Annotation an1 = new Annotation("an1", 0, 0, "spatial_indicator", sentence)
            Annotation an2 = new Annotation("an2", 1, 2, "landmark", sentence)
            an2.setHead(2)
            Annotation an3 = new Annotation("an3", 4, 6, "trajector", sentence)
            an3.setHead(4)

            SpatialExpression se = new SpatialExpression()
            se.setSpatialIndicator(an1)
            se.getLandmark().setSpatialObject(an2)
            se.getTrajector().setSpatialObject(an3)

        when:
            def key = keyGenerator.generateKey(se)

        then:
            key == "doc:_sent:s1_tr-so:4_si:0_lm-so:2"
    }
}
