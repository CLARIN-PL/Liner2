package g419.corpus.io.reader

import g419.corpus.structure.Document
import spock.lang.Specification

import java.util.zip.GZIPInputStream

class TeiStreamReaderTest extends Specification {

    Document doc

    def setup(){
        doc = new TeiStreamReader("00121121",
                stream("metadata.xml.gz"),
                stream("ann_morphosyntax.xml.gz"),
                stream("ann_props.xml.gz"),
                stream("ann_segmentation.xml.gz"),
                stream("ann_named.xml.gz"),
                stream("ann_mentions.xml.gz"),
                stream("ann_chunks.xml.gz"),
                stream("ann_annotations.xml.gz"),
                stream("ann_coreference.xml.gz"),
                stream("ann_words.xml.gz"),
                stream("ann_groups.xml.gz"),
                stream("ann_relations.xml.gz"),
                "00121121").nextDocument()
    }

    def "nextDocument should return valid document"(){
        expect:
            doc.getSentences().size() == 6
        and:
            doc.getAnnotations().countBy {it.getGroup()} == ["word":77, "named":5, "group":27, "chunks":52, "mentions":6, "other":2]
        and:
            doc.getRelationsSet().countBy {it.getType()} == ["landmark":1, "trajector": 4]
    }

    def stream(String filename){
        return new GZIPInputStream(getClass().getResourceAsStream("/pst/00121121/" + filename))
    }
}
