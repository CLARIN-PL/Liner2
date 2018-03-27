package g419.spatial.tools

import g419.spatial.structure.SpatialExpression
import spock.lang.Specification

class NullerTest extends Specification {

    def "resolve should return valid Optional object"(){
        given:
            SpatialExpression se = new SpatialExpression()
            SpatialExpression seNull = null
            Nuller nuller = new Nuller()

        expect:
            Nuller.resolve{ -> se }.isPresent() == true
            Nuller.resolve{ -> se.getSpatialIndicator() }.isPresent() == false
            Nuller.resolve{ -> se.getLandmark() }.isPresent() == true
            Nuller.resolve{ -> se.getLandmark().getRegion() }.isPresent() == false
            Nuller.resolve{ -> seNull.getLandmark() }.isPresent() == false
    }
}
