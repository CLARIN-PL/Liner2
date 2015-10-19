package g419.liner2.api.chunker;

import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;

import java.util.HashMap;

public class NullChunker extends Chunker{

	@Override
	public HashMap<Sentence, AnnotationSet> chunk(Document ps) {
		return new HashMap<Sentence, AnnotationSet>();
	}

}
