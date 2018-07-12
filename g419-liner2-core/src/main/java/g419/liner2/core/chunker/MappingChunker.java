package g419.liner2.core.chunker;

import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.liner2.core.converter.AnnotationMappingConverter;
import g419.liner2.core.converter.Converter;
import io.vavr.control.Option;

import java.util.HashMap;

public class MappingChunker extends Chunker {

    private final Converter converter;

    public MappingChunker(final String mappingFile) {
        converter = new AnnotationMappingConverter(mappingFile);
    }

    @Override
    public HashMap<Sentence, AnnotationSet> chunk(final Document document) {
        return Option.of(document)
                .peek(converter::apply)
                .map(Document::getChunkings).get();
    }
}
