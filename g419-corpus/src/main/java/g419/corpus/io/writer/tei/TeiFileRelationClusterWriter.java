package g419.corpus.io.writer.tei;

import com.google.common.collect.ImmutableMap;
import g419.corpus.io.Tei;
import g419.corpus.structure.*;

import javax.xml.stream.XMLStreamException;
import java.io.OutputStream;
import java.util.Map;
import java.util.stream.Collectors;

public class TeiFileRelationClusterWriter extends TeiFileWriter {

    final private String relationType;

    public TeiFileRelationClusterWriter(OutputStream stream, String filename, TeiPointerManager pointers, String relationType) throws XMLStreamException {
        super(stream, filename, pointers, ImmutableMap.of("xml:lang", "pl"));
        writelnStartElement(Tei.TAG_BODY);
        this.relationType = relationType;
    }

    @Override
    public void writeDocument(Document document) throws XMLStreamException {
        writeCoreferenceRelations(document.getRelations(relationType));
    }

    private void writeCoreferenceRelations(RelationSet relations) throws XMLStreamException {
        writelnStartElement(Tei.TAG_PARAGRAPH);
        int clusterId = 0;
        for ( final AnnotationCluster cluster : AnnotationClusterSet.fromRelationSet(relations).getClusters()) {
            writeRelationCluster(cluster, ++clusterId);
        }
        writelnEndElement();
    }

    private void writeRelationCluster(AnnotationCluster cluster, int clusterId) throws XMLStreamException {
        writelnComment(cluster.getAnnotations().stream().map(an -> an.getText()).collect(Collectors.joining(", ")));
        writelnStartElement(Tei.TAG_SEGMENT, ImmutableMap.of("xml:id", "coreference_" + clusterId));
        writelnStartElement(Tei.TAG_FEATURESET, ImmutableMap.of("type", "coreference"));
        writelnEmptyElement(Tei.TAG_FEATURESET, ImmutableMap.of("name", "type", "fVal", "ident"));
        writelnEndElement();
        for (Annotation ann : cluster.getAnnotations()) {
            writelnComment(ann.getText());
            writelnEmptyElement(Tei.TAG_POINTER, ImmutableMap.of("target", pointers.getPointer(ann)));
        }
        writelnEndElement();
    }


}
