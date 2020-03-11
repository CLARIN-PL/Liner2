package g419.spatial.tools

import g419.corpus.schema.kpwr.KpwrSpatial
import g419.corpus.structure.Annotation
import g419.corpus.structure.Sentence
import g419.corpus.structure.Token
import g419.corpus.structure.TokenAttributeIndex
import g419.spatial.structure.SpatialExpression
import spock.lang.Specification

class SpatialExpressionToFrameTest extends Specification {

    def "convert should a simple spatial expression into a valid frame"() {
        given:
            Sentence s = getSampleSentence()
            def se = new SpatialExpression()
            se.getTrajector().setSpatialObject(new Annotation(0, "SO", s))
            se.setSpatialIndicator(new Annotation(1, "SI", s))
            se.getLandmark().setSpatialObject(new Annotation(2, "SO", s))

        when:
            def frame = SpatialExpressionToFrame.convert(se)

        then:
            frame.getSlot(KpwrSpatial.SPATIAL_TRAJECTOR).getText() == "Książkas"
    }

    def getSampleSentence() {
        def index = new TokenAttributeIndex().with("orth")
        Sentence s = new Sentence(index)
        s.addToken(new Token("Książka", index))
        s.addToken(new Token("na", index))
        s.addToken(new Token("stole", index))
        return s
    }
}
