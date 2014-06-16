package liner2.chunker;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedHashSet;

import liner2.reader.ReaderFactory;
import liner2.reader.AbstractDocumentReader;

import liner2.structure.TokenAttributeIndex;
import liner2.structure.Annotation;
import liner2.structure.AnnotationSet;
import liner2.structure.Paragraph;
import liner2.structure.Document;
import liner2.structure.Sentence;

import liner2.writer.AbstractDocumentWriter;
import liner2.writer.WriterFactory;

import liner2.Main;

/*
 * @author Maciej Janicki
 */

public class WcclChunker extends Chunker {
	
	private String wcclFile = null;
	
	public WcclChunker()	{}
	
	public void setWcclFile(String filename) {
        this.wcclFile = filename;
	}
	
	private AnnotationSet chunkSentence(Sentence sentence) {
		AnnotationSet chunking = new AnnotationSet(sentence);
		String cmd = "wccl-rules -q -t nkjp -i ccl -I - -C " + this.wcclFile;
		Process p = null;
		try {
			p = Runtime.getRuntime().exec(cmd);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        InputStream in = p.getInputStream();
        OutputStream out = p.getOutputStream();
        BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()));


        // zapamiętaj AttributeIndex, żeby nie stracić go przy addSentence()
        TokenAttributeIndex ai = sentence.getAttributeIndex();
        Document document = new Document("wccl chunker", ai);
        Paragraph paragraph = new Paragraph(null);
        LinkedHashSet<Annotation> sentenceAnns = sentence.getChunks();
        sentence.setAnnotations(new AnnotationSet(sentence));
        paragraph.addSentence(sentence);
        document.addParagraph(paragraph);
        document.setAttributeIndex(ai);


        try {
            AbstractDocumentWriter writer = WriterFactory.get().getStreamWriter(out, "ccl");
            writer.writeDocument(document);
            writer.close();

            AbstractDocumentReader reader = ReaderFactory.get().getStreamReader("wccl chunker", in, "ccl");
            document = reader.nextDocument();
            reader.close();
            String error = err.readLine();
            if(error != null){
                throw new Exception(error);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        sentence.setAnnotations(new AnnotationSet(sentence, sentenceAnns));
        Sentence resultSentence = document.getSentences().get(0);
        for (Annotation chunk : resultSentence.getChunks())
			if (!chunking.contains(chunk)) {
                chunking.addChunk(chunk);
            }
        return chunking;
	}	
	
	@Override
	public HashMap<Sentence, AnnotationSet> chunk(Document ps) {
		HashMap<Sentence, AnnotationSet> chunkings = new HashMap<Sentence, AnnotationSet>();
		for ( Paragraph paragraph : ps.getParagraphs() )
			for (Sentence sentence : paragraph.getSentences())
				chunkings.put(sentence, this.chunkSentence(sentence));
		return chunkings;
	}
	
}
