package g419.spatial.pattern

import g419.corpus.structure.Sentence
import g419.test.TestSampleSentence
import g419.spatial.tools.SentenceAnnotationIndexTypePos
import spock.lang.Specification

class SentencePatternTestTest extends Specification implements TestSampleSentence{

    Sentence sentence
    SentenceAnnotationIndexTypePos annotations

    def setup(){
        sentence = getSampleSentenceWithAnnotations()
        annotations = new SentenceAnnotationIndexTypePos(sentence)
    }

    def "match for a rule with a single SentencePatternMatchTokenPos condition should return a valid list of frames"(){
        given:
            def matchers = [new SentencePatternMatchTokenPos(pos)]
            def pattern = new SentencePattern(type, matchers)

        when:
            def frames = pattern.match(annotations)

        then:
            frames.size() == 2
        and:
            frames.get(0).getType() == type

        where:
            pos = "prep"
            type = "spatial"
    }

    def "should match single annotation of type artifact"(){
        given:
            def matchers = [new SentencePatternMatchAnnotationType(annotationType)]
            def pattern = new SentencePattern(type, matchers)

        when:
            def frames = pattern.match(annotations)

        then:
            frames.size() == 3
        and:
            frames.get(0).getType() == type

        where:
            type = "spatial"
            annotationType = "artifact"
    }

    def "should match a sequence of two artifact annotations"(){
        given:
            def matchers = [new SentencePatternMatchAnnotationType(annotationType1), new SentencePatternMatchAnnotationType(annotationType2)]
            def pattern = new SentencePattern(type, matchers)

        when:
            def frames = pattern.match(annotations)

        then:
            frames.size() == 1
        and:
            frames.get(0).getType() == type

        where:
            type = "spatial"
            annotationType1 = "artifact"
            annotationType2 = "action"
    }

    def "should match a preposition followed by an annotation of type artifact"(){
        given:
            def matchers = [new SentencePatternMatchTokenPos(pos), new SentencePatternMatchAnnotationType(annotationType)]
            def pattern = new SentencePattern(type, matchers)

        when:
            def frames = pattern.match(annotations)

        then:
            frames.size() == 1
        and:
            frames.get(0).getType() == type

        where:
            type = "spatial"
            pos = "prep"
            annotationType = "artifact"
    }

}
