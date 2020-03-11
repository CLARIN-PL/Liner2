package g419.toolbox.sumo

import spock.lang.Specification

class SumoTest extends Specification {

    def "default constructor should load a valid number of concepts"() {
        when:
            def sumo = new Sumo()

        then:
            sumo.getConcepts().size() == 3570

        and:
            sumo.containsClass("CleaningDevice")

        and:
            sumo.containsClass("cleaningdevice")
    }

    def "default constructor should load a valid number of concepts (case sensitive)"() {
        when:
            def sumo = new Sumo(true)

        then:
            sumo.getConcepts().size() == 3570

        and:
            sumo.containsClass("CleaningDevice")

        and:
            !sumo.containsClass("cleaningdevice")
    }

    def "getSuperclasses should return a valid list of concepts"() {
        given:
            def sumo = new Sumo()

        when:
            def concepts = sumo.getSuperclasses("device")

        then:
            concepts == ["artifact", "physical", "entity", "object"] as Set
    }

    def "getSubclasses should return a valid list of concepts"() {
        given:
            def sumo = new Sumo()

        when:
            def concepts = sumo.getSubclasses("cargoship")

        then:
            concepts.size() == 25

        and:
            concepts.contains("rollonrolloffcargoship")
    }

    def "isSubclassOf(#subclass, #upperclass) should return #result"() {
        given:
            def sumo = new Sumo()

        expect:
            sumo.isSubclassOf(subclass, upperclass) == result

        where:
            subclass                 | upperclass  || result
            "cargoship"              | "ship"      || true
            "rollonrolloffcargoship" | "cargoship" || true
            "ship"                   | "animal"    || false
    }
}
