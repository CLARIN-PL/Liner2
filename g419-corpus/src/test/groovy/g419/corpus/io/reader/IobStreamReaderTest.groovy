package g419.corpus.io.reader

import g419.corpus.io.DataFormatException
import spock.lang.AutoCleanup
import spock.lang.Specification

import java.nio.charset.StandardCharsets

class IobStreamReaderTest extends Specification {

    @AutoCleanup
    IobStreamReader reader

    def setup() {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("sample.iob")
        reader = new IobStreamReader(is)
    }


    def "should throw an exception for an empty stream"() {
        given:
            String exampleString = ""
            InputStream is = new ByteArrayInputStream(exampleString.getBytes(StandardCharsets.UTF_8))

        when:
            reader = new IobStreamReader(is)

        then:
            DataFormatException ex = thrown()
            ex.getMessage() == "Header not found. Empty file?"
    }

    def "should throw an exception for an invalid header"() {
        given:
            String exampleString = "This is not a IOB file"
            InputStream is = new ByteArrayInputStream(exampleString.getBytes(StandardCharsets.UTF_8))

        when:
            reader = new IobStreamReader(is)

        then:
            DataFormatException ex = thrown()
            ex.getMessage().startsWith("First line does not contain attributes definition")
    }

    def "should read file in IOB format and create valid structure of the documents"() {
        when:
            def d1 = reader.nextDocument()

        then:
            d1 != null
            d1.getSentences().size() == 1
            d1.getSentences().get(0).getTokenNumber() == 3

        when:
            def d2 = reader.nextDocument()

        then:
            d2 != null
            d2.getSentences().size() == 2
            d2.getSentences().get(0).getTokenNumber() == 17
            d2.getSentences().get(1).getTokenNumber() == 16

        when:
            def d3 = reader.nextDocument()

        then:
            d3 == null
    }

    def "hasNext should return valid values"() {
        expect:
            reader.hasNext() == true

        when:
            reader.nextDocument()

        then:
            reader.hasNext() == true

        when:
            reader.nextDocument()

        then:
            reader.hasNext() == false
    }

    def "parseFileHeader should return valid list"() {
        given:
            def header = "-DOCSTART CONFIG FEATURES f1 f2 f3"

        when:
            def features = reader.parseFileHeader(header)

        then:
            features == ["f1", "f2", "f3"]
    }

    def "goToNextFileBlock should return valid file identifiers"() {
        expect:
            reader.nextFileId == "file1"

        when:
            def id2 = reader.goToNextFileBlock()

        then:
            id2 == "file2"

        when:
            def id3 = reader.goToNextFileBlock()

        then:
            id3 == null
    }

    def "should read valid set of annotations"() {
        when:
            def d1 = reader.nextDocument()

        then:
            d1.getSentences().get(0).getChunks().countBy {
                it.getType()
            } == [chunk_app: 1, chunk_head_agp: 1, chunk_head_np: 1, chunk_agp: 1, chunk_np: 1, nam_loc_gpe_city: 1, nam_fac_goe: 1]

        when:
            def d2 = reader.nextDocument()

        then:
            d2.getSentences().get(0).getChunks().countBy {
                it.getType()
            } == [chunk_app: 2, chunk_head_agp: 2, chunk_head_np: 2, chunk_agp: 2, chunk_np: 2, nam_loc_gpe_city: 2, keyword: 2, nam_fac_goe: 1, nam_adj_country: 1, nam_loc_gpe_district: 1, spatial_object: 1, spatial_indicator_3: 1]
    }


}
