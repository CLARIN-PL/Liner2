package g419.spatial.structure

import com.google.common.collect.Maps
import g419.corpus.structure.Annotation
import g419.corpus.structure.Sentence
import g419.test.TestSampleSentence
import spock.lang.Specification

import java.util.stream.Collectors

class SpatialExpressionTest extends Specification implements TestSampleSentence{

    Sentence sentence
    Map<String,Annotation> ids

    def setup(){
        sentence = getSampleSentenceWithAnnotations()
        ids = Maps.newHashMap()
        sentence.getChunks().stream().forEach{a->ids.put(a.getId(),a)}
    }

    def "getters should return values set by the constructor"(){
        when:
            SpatialExpression se = new SpatialExpression("type", ids["an1"], ids["an2"], ids["an3"])

        then:
            se.getType() == "type"
            se.getTrajector().getSpatialObject() == ids["an1"]
            se.getTrajector().getRegion() == null
            se.getSpatialIndicator() == ids["an2"]
            se.getLandmark().getSpatialObject() == ids["an3"]
            se.getLandmark().getRegion() == null
            se.getMotionIndicator() == null
            se.getDirections().size() == 0
            se.getDistances().size() == 0
            se.getPathsIndicators().size() == 0
    }

    def "getters should return values set by the setters"(){
        given:
            SpatialExpression se = new SpatialExpression()

        when:
            se.setLandmark(ids["an1"])
            se.getLandmark().setRegion(ids["an2"])
            se.setTrajector(ids["an3"])
            se.getTrajector().setRegion(ids["an4"])
            se.setSpatialIndicator(ids["an5"])
            se.setMotionIndicator(ids["an6"])

        then:
            se.getType() == null
            se.getLandmark().getSpatialObject() == ids["an1"]
            se.getLandmark().getRegion() == ids["an2"]
            se.getTrajector().getSpatialObject() == ids["an3"]
            se.getTrajector().getRegion() == ids["an4"]
            se.getSpatialIndicator() == ids["an5"]
            se.getMotionIndicator() == ids["an6"]
            se.getDirections().size() == 0
            se.getDistances().size() == 0
            se.getPathsIndicators().size() == 0
    }

    def "getAnnotations should return valid set of annotations"(){
        given:
            SpatialExpression se = new SpatialExpression()
            se.setLandmark(ids["an1"])
            se.getLandmark().setRegion(ids["an2"])
            se.setTrajector(ids["an3"])
            se.getTrajector().setRegion(ids["an4"])
            se.setSpatialIndicator(ids["an5"])
            se.setMotionIndicator(ids["an6"])

        when:
            Set<Annotation> list = se.getAnnotations()

        then:
            list.size() == 6
            list.stream().map{a->a.getId()}.collect(Collectors.toList()).sort() == ["an1","an2","an3","an4","an5","an6"]
    }

    def "toString should return valid value"(){
        given:
            SpatialExpression se = new SpatialExpression("type", ids["an3"], ids["an7"], ids["an1"])

        when:
            def str = se.toString()

        then:
            str == "type: TR:[{rybak} z wędką:person] ... SI:[{Na}:preposition] LM:[zielonej {łódce}:artifact]"
    }

}
