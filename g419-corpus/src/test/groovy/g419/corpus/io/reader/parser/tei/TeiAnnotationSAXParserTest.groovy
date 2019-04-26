package g419.corpus.io.reader.parser.tei


import com.google.common.collect.Maps
import g419.corpus.structure.TokenAttributeIndex
import spock.lang.Specification

import java.util.zip.GZIPInputStream

class TeiAnnotationSAXParserTest extends Specification {

    def "should return a valud list of annotations"() {
        given:
            TokenAttributeIndex attributeIndex = new TokenAttributeIndex();
            attributeIndex.addAttribute("orth");
            attributeIndex.addAttribute("base");
            attributeIndex.addAttribute("ctag");

            InputStream annMorphosyntax = new GZIPInputStream(
                    getClass().getResourceAsStream("/pst/00121121/ann_morphosyntax.xml.gz"))
            InputStream annSegmentation = new GZIPInputStream(
                    getClass().getResourceAsStream("/pst/00121121/ann_segmentation.xml.gz"))
            InputStream annGroups = new GZIPInputStream(
                    getClass().getResourceAsStream("/pst/00121121/ann_groups.xml.gz"))
            final Map<String, List<String>> globalElementIndex = Maps.newHashMap()
            String docName = "00121121"

            final TeiMorphosyntaxSAXParser morphoParser = new TeiMorphosyntaxSAXParser(docName, annMorphosyntax, attributeIndex)
            final TeiSegmentationSAXParser segmentationParser = new TeiSegmentationSAXParser(annSegmentation, morphoParser.getParagraphs())


        when:
            new TeiAnnotationSAXParser(
                    "ann_groups.xml",
                    annGroups,
                    segmentationParser.getParagraphs(),
                    morphoParser.getTokenIdsMap(),
                    globalElementIndex,
                    "group")

        then:
            segmentationParser.getParagraphs().size() == 1

        and:
            segmentationParser.getParagraphs().get(0).getSentences().get(0).getChunks().size() == 4

    }

}
