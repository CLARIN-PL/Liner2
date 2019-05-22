package g419.corpus.structure

import spock.lang.Specification

class FrameTest extends Specification {

    def "getType should return value set by the constructor"(){
        when:
            def frame = new Frame<String>(type)

        then:
            frame.getType() == type

        where:
            type = "name"
    }

    def "set should store value for the last call"(){
        given:
            def frame = new Frame<String>(type)

        when:
            frame.set(slot, value1)
        then:
            frame.getSlot(slot) == value1

        when:
            frame.set(slot, value2)
        then:
            frame.getSlot(slot) == value2

        where:
            type = "name"
            slot = "slot"
            value1 = "v1"
            value2 = "v2"
    }

    def "setSlotAttribute should assign attribute to correct slots"(){
        given:
            def frame = new Frame<String>(type)
            frame.set(slot1, value1)
            frame.set(slot2, value2)

        expect:
            frame.getSlotAttributes(slot1).size() == 0
            frame.getSlotAttributes(slot2).size() == 0

        when:
            frame.setSlotAttribute(slot1, attribute, value)
        then:
            frame.getSlotAttributes(slot1).size() == 1
            frame.getSlotAttributes(slot2).size() == 0
        and:
            frame.getSlotAttributes(slot1).get(attribute) == value

        where:
            type = "name"
            slot1 = "slot1"
            value1 = "value1"
            slot2 = "slot2"
            value2 = "value2"
            attribute = "attribute"
            value = "value"
    }
}
