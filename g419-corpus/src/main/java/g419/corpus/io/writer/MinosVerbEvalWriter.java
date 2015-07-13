package g419.corpus.io.writer;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class MinosVerbEvalWriter extends AbstractDocumentWriter{
	private BufferedWriter writer;
	
	public MinosVerbEvalWriter(OutputStream os){
		this.writer = new BufferedWriter(new OutputStreamWriter(os));
	}
	
	@Override
	public void writeDocument(Document document) {
		for(Sentence sentence : document.getSentences())
			for(Annotation annotation : sentence.getChunks())
				writeAnnotation(annotation);
	}

	public void writeAnnotation(Annotation annotation){
		if("wyznacznik_null_verb".equals(annotation.getType())){
			try {
				this.writer.write("1\t" + annotation.getText() + "\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else if ("wyznacznik_notnull_verb".equals(annotation.getType())){
			try {
				this.writer.write("0\t" + annotation.getText() + "\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
		

	@Override
	public void flush() {
		try {
			this.writer.flush();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void close() {
		try {
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
