package g419.corpus.io.reader

import g419.corpus.structure.Document
import g419.corpus.structure.DocumentDescriptor
import spock.lang.Specification

class CclSAXStreamReaderTest extends Specification {

    def "should read metadata"() {
        given:
            final InputStream ccl = getClass().getClassLoader().getResourceAsStream("sample-props.xml")
            final InputStream ini = getClass().getClassLoader().getResourceAsStream("sample-props.ini")
            CclSAXStreamReader reader = new CclSAXStreamReader("sample", ccl, ini, null)

            final DocumentDescriptor expectedDescriptor = new DocumentDescriptor()
            expectedDescriptor.setDescription("id", "with_props")
            expectedDescriptor.setDescription("date", "some_date")
            expectedDescriptor.setDescription("title", "some_title")
            expectedDescriptor.setMetadata("subject", "some_subject")
            expectedDescriptor.setMetadata("keywords", "kw1, kw2, kw3")
            expectedDescriptor.setMetadata("licence", "any license")

        when:
            Document document = reader.nextDocument()

        then:
            expectedDescriptor == document.getDocumentDescriptor()
    }

    def "should read annotation lemma correctly"() {
        given:
            final InputStream ccl = getClass().getClassLoader().getResourceAsStream("sample-lemma.xml")
            CclSAXStreamReader reader = new CclSAXStreamReader("sample", ccl, null, null)

        when:
            Document document = reader.nextDocument()

        then:
            with(document.getAnnotations().get(0)) {
                getText() == "Zielonej Górze"
                getLemma() == "Zielona Góra"
            }
    }
}
