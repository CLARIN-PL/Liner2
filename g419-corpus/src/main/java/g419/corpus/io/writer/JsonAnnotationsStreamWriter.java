package g419.corpus.io.writer;

import g419.corpus.structure.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Map;


/*
 * Drukowanie wyników w postaci obiektu JSON.
 * @author Jan Kocoń
 */
public class JsonAnnotationsStreamWriter extends AbstractDocumentWriter {
	private BufferedWriter ow;
	private int sentenceOffset = 0;

	public JsonAnnotationsStreamWriter(OutputStream os){
		this.ow = new BufferedWriter(new OutputStreamWriter(os));
	}

	private JSONObject getChunk(Annotation c, Sentence s) throws IOException {
		int begin = this.sentenceOffset;
		int end = this.sentenceOffset;
		JSONObject o = new JSONObject();

		ArrayList<Token> tokens = s.getTokens();
		for (int i = 0; i < c.getBegin(); i++)
			begin += tokens.get(i).getOrth().length();
		end = begin;
		for (int i = c.getBegin(); i <= c.getEnd(); i++)
			end += tokens.get(i).getOrth().length();
		o.put("from", begin);
		o.put("to", (end-1));
		o.put("type", c.getType());
		o.put("text", c.getText());

		//this.ow.write("(" + begin + "," + (end-1) + "," + c.getType() + ",\"" + c.getText() + "\")");
		if (!c.getMetadata().isEmpty()){
			JSONObject o1 = new JSONObject();
			for (Map.Entry<String, String> entry: c.getMetadata().entrySet()){
				o1.put(entry.getKey(), entry.getValue());
			}
			o.put("metadata", o1);
		}
		//this.ow.write(o.toString());
		return o;
	}

	@Override
	public void writeDocument(Document document) {
		JSONArray a = new JSONArray();
		this.sentenceOffset = 0;
		for (Paragraph paragraph : document.getParagraphs())
			for (Sentence s : paragraph.getSentences())
				try {
					Annotation[] chunks = Annotation.sortChunks(s.getChunks());
					for (Annotation c : chunks)
						a.put(getChunk(c, s));

					for (Token t : s.getTokens())
						this.sentenceOffset += t.getOrth().length();

				} catch (IOException ex) {
					ex.printStackTrace();
				}
		try {
			this.ow.write(a.toString());
			this.ow.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void flush() {
		try {
			ow.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() {
		try {
			this.ow.flush();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
