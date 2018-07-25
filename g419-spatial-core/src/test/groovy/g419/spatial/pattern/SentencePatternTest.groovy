package g419.spatial.pattern

import g419.corpus.structure.Annotation
import g419.corpus.structure.Frame
import g419.corpus.structure.Sentence
import g419.corpus.structure.SentenceAnnotationIndexTypePos
import g419.test.TestSampleSentence
import spock.lang.Specification

class SentencePatternTest extends Specification implements TestSampleSentence {

    Sentence sentence
    SentenceAnnotationIndexTypePos annotations

    def setup() {
        sentence = getSampleSentenceWithAnnotations()
        annotations = new SentenceAnnotationIndexTypePos(sentence)
    }

    def "match for a rule with a single SentencePatternMatchTokenPos condition should return a valid list of frames"() {
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

    def "should match a sequence of prep, adj and subst"() {
        given:
            SentencePatternMatchSequence matcher = new SentencePatternMatchSequence()
                    .append(new SentencePatternMatchTokenPos("prep"))
                    .append(new SentencePatternMatchTokenPos("adj"))
                    .append(new SentencePatternMatchTokenPos("subst"))
            def pattern = new SentencePattern(type, [matcher])

        when:
            def frames = pattern.match(annotations)

        then:
            frames.size() == 1
        and:
            frames.get(0).getType() == type

        where:
            type = "spatial"
    }

    def "should match a sequence of prep, adj, subst and return frame with two slots"() {
        given:
            SentencePatternMatchSequence matcher = new SentencePatternMatchSequence()
                    .append(new SentencePatternMatchTokenPos("prep").withLabel(label1))
                    .append(new SentencePatternMatchTokenPos("adj"))
                    .append(new SentencePatternMatchTokenPos("subst").withLabel(label2))
            def pattern = new SentencePattern(type, [matcher])

        when:
            List<Frame<Annotation>> frames = pattern.match(annotations)

        then:
            frames.size() == 1
        and:
            with(frames.get(0)) {
                it.getType() == type
                it.getSlots().size() == 2
                it.getSlot(label1).getText() == "Na"
                it.getSlot(label1).getType() == label1
                it.getSlot(label2).getText() == "łódce"
                it.getSlot(label2).getType() == label2
            }

        where:
            type = "spatial"
            label1 = "label1"
            label2 = "label2"
    }


    def "should not match any sequence of prep, prep"() {
        given:
            SentencePatternMatchSequence matcher = new SentencePatternMatchSequence()
                    .append(new SentencePatternMatchTokenPos("prep"))
                    .append(new SentencePatternMatchTokenPos("prep"))
            def pattern = new SentencePattern(type, [matcher])

        when:
            def frames = pattern.match(annotations)

        then:
            frames.size() == 0

        where:
            type = "spatial"
    }

    def "should match single annotation of type artifact"() {
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

    def "should match artifact followed by action"() {
        given:
            SentencePatternMatchSequence matcher = new SentencePatternMatchSequence()
                    .append(new SentencePatternMatchAnnotationType(annotationType1))
                    .append(new SentencePatternMatchAnnotationType(annotationType2));
            def pattern = new SentencePattern(type, [matcher])

        when:
            def frames = pattern.match(annotations)

        then:
            frames.size() == 2
        and:
            frames.get(0).getType() == type

        where:
            type = "spatial"
            annotationType1 = "artifact"
            annotationType2 = "action"
    }

    def "should match a artifact followed by action and return two frames, each with two slots"() {
        given:
            SentencePatternMatchSequence matcher = new SentencePatternMatchSequence()
                    .append(new SentencePatternMatchAnnotationType(annotationType1).withLabel(label1))
                    .append(new SentencePatternMatchAnnotationType(annotationType2).withLabel(label2))
            def pattern = new SentencePattern(type, [matcher])

        when:
            List<Frame<Annotation>> frames = pattern.match(annotations)

        then:
            frames.size() == 2
        and:
            with(frames.get(0)) {
                it.getType() == type
                it.getSlots().size() == 2
                it.getSlot(label1).getId() == "an1"
                it.getSlot(label2).getId() == "an5"
            }
        and:
            with(frames.get(1)) {
                it.getType() == type
                it.getSlots().size() == 2
                it.getSlot(label1).getId() == "an2"
                it.getSlot(label2).getId() == "an5"
            }

        where:
            type = "spatial"
            label1 = "label1"
            label2 = "label2"
            annotationType1 = "artifact"
            annotationType2 = "action"
    }

    def "should match a preposition followed by an annotation of type artifact"() {
        given:
            SentencePatternMatchSequence matcher = new SentencePatternMatchSequence()
                    .append(new SentencePatternMatchTokenPos(pos))
                    .append(new SentencePatternMatchAnnotationType(annotationType));
            def pattern = new SentencePattern(type, [matcher])

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
