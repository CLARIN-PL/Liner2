package g419.corpus.io.writer

import com.cedarsoftware.util.DeepEquals
import g419.corpus.io.reader.AbstractDocumentReader
import g419.corpus.io.reader.ReaderFactory
import g419.corpus.structure.*
import spock.lang.Specification

class CclStreamWriterTest extends Specification {

    // ToDo: This could go to higher level test package
    def "read/write test"() {
        given:
            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("sample-props.xml")
            AbstractDocumentReader reader = ReaderFactory.get().getStreamReader("sample-props.xml", inputStream, "ccl")
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
            CclStreamWriter writer = new CclStreamWriter(outputStream)

        when:
            Document original = reader.nextDocument()
            writer.writeDocument(original)
            ByteArrayInputStream buffer = new ByteArrayInputStream(outputStream.toByteArray())
            reader.close()
            def reloaded = ReaderFactory.get().getStreamReader("sample-props.xml", buffer, "ccl").nextDocument()

        then:
            DeepEquals.deepEquals(original, reloaded)

        cleanup:
            inputStream.close()
            reader.close()
    }

    def "write a document containing an annotation with a lemma"() {
        given:
            Document document = getSampleDocument()
            ByteArrayOutputStream os = new ByteArrayOutputStream()
            CclStreamWriter writer = new CclStreamWriter(new PrintStream(os))

        when:
            writer.writeDocument(document)
            writer.close()
            String ccl = new String(os.toByteArray(), "UTF-8")

        then:
            ccl.contains("<prop key=\"loc:lemma\">Zielona Góra</prop>")

    }

    Document getSampleDocument() {
        TokenAttributeIndex index = new TokenAttributeIndex().with("orth").with("base").with("ctag")

        Sentence sentence = new Sentence(index)
        sentence.addToken(new Token("Spotkał", new Tag("spotkać", "praet:sg:m1:perf", true), index))
        sentence.addToken(new Token("em", new Tag("być", "aglt:sg:pri:imperf:wok", true), index))
        sentence.addToken(new Token("Karola", new Tag("Karol", "subst:sg:acc:m1", true), index))
        sentence.addToken(new Token("w", new Tag("w", "prep:loc:nwok", true), index))
        sentence.addToken(new Token("Zielonej", new Tag("zielony", "adj:sg:loc:f:pos", true), index))
        sentence.addToken(new Token("Górze", new Tag("góra", "subst:sg:loc:f", true), index).withNoSpaceAfter(true))
        sentence.addToken(new Token(".", new Tag(".", "interp", true), index))

        Annotation an = new Annotation(4, 5, "loc", sentence).withLemma("Zielona Góra")
        sentence.addChunk(an)

        Paragraph paragraph = new Paragraph("p1", index)
        paragraph.addSentence(sentence)

        Document document = new Document("sample", index)
        document.addParagraph(paragraph)

        return document
    }
}
