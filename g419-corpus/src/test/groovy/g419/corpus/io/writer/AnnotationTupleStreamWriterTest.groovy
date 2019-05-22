package g419.corpus.io.writer

import g419.corpus.io.reader.CclSAXStreamReader
import g419.corpus.structure.Document
import spock.lang.Specification

class AnnotationTupleStreamWriterTest extends Specification {

    def "writeDocument should write correct content to the file"(){
        given:
            Document document = new CclSAXStreamReader("0099883", getClass().getResourceAsStream("/00099883.xml"), null, null).nextDocument()
            String tuples = getClass().getResource("/00099883.annotation-tuple.txt").text
            ByteArrayOutputStream os = new ByteArrayOutputStream()
            AnnotationTupleStreamWriter writer = new AnnotationTupleStreamWriter(new PrintStream(os))
            writer.writeDocument(document)
            writer.close()
            os.flush()
            String stream = new String(os.toByteArray(),"UTF-8")

        expect:
            stream == tuples
    }

}
