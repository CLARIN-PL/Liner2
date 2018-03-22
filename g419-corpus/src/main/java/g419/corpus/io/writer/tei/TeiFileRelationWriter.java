package g419.corpus.io.writer.tei;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import g419.corpus.io.Tei;
import g419.corpus.structure.Document;
import g419.corpus.structure.Relation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TeiFileRelationWriter extends TeiFileWriter {

    final Logger logger = LoggerFactory.getLogger(getClass());

    public TeiFileRelationWriter(final OutputStream stream, final String filename, final TeiPointerManager pointers) throws XMLStreamException {
        super(stream, filename, pointers, ImmutableMap.of(Tei.TAG_LANG, "pl"));
        writelnStartElement(Tei.TAG_BODY);
    }

    @Override
    public void writeDocument(final Document document) throws XMLStreamException {
        List<Relation> relations = Lists.newArrayList(document.getRelations().getRelations());
        Collections.sort(relations, Comparator.comparing(Relation::getId));
        relations.stream().forEach(this::writeRelation);
    }

    private void writeRelation(final Relation relation) {
        try {
            String sourceRef = pointers.getPointer(relation.getAnnotationFrom());
            String targetRef = pointers.getPointer(relation.getAnnotationTo());
            writelnStartElement(Tei.TAG_SEGMENT, ImmutableMap.of("xml:id", relation.getId(), "type", relation.getType(), "set", relation.getSet()));
            writelnComment(relation.getAnnotationFrom().getText());
            writelnEmptyElement(Tei.TAG_POINTER, ImmutableMap.of("type", "source", "target", sourceRef));
            writelnComment(relation.getAnnotationTo().getText());
            writelnEmptyElement(Tei.TAG_POINTER, ImmutableMap.of("type", "target", "target", targetRef));
            writelnEndElement();
        } catch (XMLStreamException ex) {
            logger.error("Failed to write relation {} to TEI file", relation, ex);
        }
    }

}
