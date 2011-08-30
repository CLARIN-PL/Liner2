package liner2.chunker;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;

import liner2.reader.ReaderFactory;
import liner2.reader.StreamReader;

import liner2.structure.Chunk;
import liner2.structure.Chunking;
import liner2.structure.Paragraph;
import liner2.structure.ParagraphSet;
import liner2.structure.Sentence;

import liner2.writer.StreamWriter;
import liner2.writer.WriterFactory;

/*
 * @author Maciej Janicki
 */

public class WcclChunker extends Chunker {
	
	private String wcclFile = null;
	
	public WcclChunker()	{}
	
	public void setWcclFile(String filename) {
		this.wcclFile = filename;
	}
	
	@Override
	public Chunking chunkSentence(Sentence sentence) {
		Chunking chunking = new Chunking(sentence);
		String cmd = "wccl-rules -q -i ccl -I - " + this.wcclFile;
		Process p = null;
		
		try {
			p = Runtime.getRuntime().exec(cmd);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
		InputStream err = p.getErrorStream();
		InputStream in = p.getInputStream();
		OutputStream out = p.getOutputStream();
		
		ParagraphSet paragraphSet = new ParagraphSet();
		Paragraph paragraph = new Paragraph(null);
		paragraph.addSentence(sentence);
		paragraphSet.addParagraph(paragraph);
		
		try {
			StreamReader reader = ReaderFactory.get().getStreamReader(in, "ccl");
			StreamWriter writer = WriterFactory.get().getStreamWriter(out, "ccl");
			writer.writeParagraphSet(paragraphSet);
			writer.close();
			paragraph = reader.readParagraph();
			reader.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		Sentence resultSentence = paragraph.getSentences().get(0);
		for (Chunk chunk : resultSentence.getChunks())
			chunking.addChunk(chunk);
		
		return chunking;
	}	
	
}
