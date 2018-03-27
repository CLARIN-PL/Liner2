package g419.spatial.structure

import com.google.common.collect.Maps
import g419.corpus.structure.Annotation
import g419.corpus.structure.Sentence
import g419.corpus.structure.Tag
import g419.corpus.structure.Token
import g419.corpus.structure.TokenAttributeIndex
import spock.lang.Specification

import java.util.stream.Collectors

class SpatialExpressionTest extends Specification {

    def "getters should return values set by the constructor"(){
        given:
            Sentence sentence = getSampleSentenceWithAnnotations()
            Map<String,Annotation> ids = Maps.newHashMap()
            sentence.getChunks().stream().forEach{a->ids.put(a.getId(),a)}
            SpatialExpression se = new SpatialExpression("type", ids["an1"], ids["an2"], ids["an3"])

        expect:
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
            Sentence sentence = getSampleSentenceWithAnnotations()
            Map<String,Annotation> ids = Maps.newHashMap()
            sentence.getChunks().stream().forEach{a->ids.put(a.getId(),a)}
            SpatialExpression se = new SpatialExpression()
            se.setLandmark(ids["an1"])
            se.getLandmark().setRegion(ids["an2"])
            se.setTrajector(ids["an3"])
            se.getTrajector().setRegion(ids["an4"])
            se.setSpatialIndicator(ids["an5"])
            se.setMotionIndicator(ids["an6"])

        expect:
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
            Sentence sentence = getSampleSentenceWithAnnotations()
            Map<String,Annotation> ids = Maps.newHashMap()
            sentence.getChunks().stream().forEach{a->ids.put(a.getId(),a)}
            SpatialExpression se = new SpatialExpression()
            se.setLandmark(ids["an1"])
            se.getLandmark().setRegion(ids["an2"])
            se.setTrajector(ids["an3"])
            se.getTrajector().setRegion(ids["an4"])
            se.setSpatialIndicator(ids["an5"])
            se.setMotionIndicator(ids["an6"])
            Set<Annotation> list = se.getAnnotations()

        expect:
            list.size() == 6
            list.stream().map{a->a.getId()}.collect(Collectors.toList()).sort() == ["an1","an2","an3","an4","an5","an6"]
    }


    /**
     *  Na zielonej łódce stoi rybak z wędką
     *  0- 1------- 2---- 3--- 4---- 5 6----
     *     ┗━an1━━━━━━━━┛      ┗━an3━━━━━━━┛
     *              ┗an2┛      ┗an4┛
     *                    ┗━an5━━━━┛
     */
    def getSampleSentenceWithAnnotations() {
        Sentence sentence = getSampleSentence()
        sentence.addChunk(new Annotation("an1", 1, 2, "artifact", sentence))
        sentence.addChunk(new Annotation("an2", 2, 2, "artifact", sentence))
        sentence.addChunk(new Annotation("an3", 4, 6, "person", sentence))
        sentence.addChunk(new Annotation("an4", 4, 4, "person", sentence))
        sentence.addChunk(new Annotation("an5", 3, 4, "action", sentence))
        sentence.addChunk(new Annotation("an6", 3, 4, "artifact", sentence))
        return sentence
    }

    /**
     *  Na zielonej łódce stoi rybak z wędką
     *  0- 1------- 2---- 3--- 4---- 5 6----
     */
    def getSampleSentence() {
        TokenAttributeIndex attrIndex = new TokenAttributeIndex()
        attrIndex.addAttribute("orth")
        Sentence sentence = new Sentence()
        sentence.addToken(new Token("Na", new Tag("na", "tag", true), attrIndex))
        sentence.addToken(new Token("zielonej", new Tag("zielony", "tag", true), attrIndex))
        sentence.addToken(new Token("łódce", new Tag("łódka", "tag", true), attrIndex))
        sentence.addToken(new Token("stoi", new Tag("stać", "tag", true), attrIndex))
        sentence.addToken(new Token("rybak", new Tag("rybak", "tag", true), attrIndex))
        sentence.addToken(new Token("z", new Tag("z", "tag", true), attrIndex))
        sentence.addToken(new Token("wędką", new Tag("wędka", "tag", true), attrIndex))
        return sentence
    }
}
