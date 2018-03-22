package g419.spatial.formatter

import spock.lang.Specification

class SpatialExpressionFormatterFactoryTest extends Specification {

    def "create should return valid formatter"(){
        expect:
            SpatialExpressionFormatterFactory.create(type).getClass().toString() == cl

        where:
            type   || cl
            "tsv"  || new SpatialExpressionFormatterTsv().getClass().toString()
            "tree" || new SpatialExpressionFormatterTree().getClass().toString()
    }

    def "create should return exception of unknown formatter name"(){
        when:
            SpatialExpressionFormatterFactory.create("unknown")

        then:
            RuntimeException ex1 = thrown()
            ex1.getMessage() == "Unknown spatial expressions formatter: unknown"

        when:
            SpatialExpressionFormatterFactory.create(null)
        then:
            RuntimeException ex2 = thrown()
            ex2.getMessage() == "Name of spatial expressions formatter cannot be null"
    }
}
