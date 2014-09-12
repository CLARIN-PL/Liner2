package g419.corpus.io.writer;

import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Tag;
import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;

//import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class ConllStreamWriter extends AbstractDocumentWriter{

	private OutputStream os;
	
	public ConllStreamWriter(){
		this.os = new ByteArrayOutputStream();
	}
	
	public static String[] convertSentence(Sentence s){
		int index = 0;
		String[] sentenceConll = new String[s.getTokenNumber()];
		TokenAttributeIndex ai = s.getAttributeIndex();
		for(Token t : s.getTokens()){
			sentenceConll[index] = convertToken(t, ai, ++index);
		}
		return sentenceConll;
	}
	
	private void writeSentence(Sentence s) throws IOException{
		int index = 0;
		TokenAttributeIndex ai = s.getAttributeIndex();
		for(Token t : s.getTokens()){
			this.os.write(convertToken(t, ai, ++index).getBytes());
			//writeToken(t, ai, ++index);
		}
	};
	
	public static String convertToken(Token t, TokenAttributeIndex ai, int tokenIndex){
		String orth = t.getOrth();
		String base = ai.getAttributeValue(t, "base");
		String posext = ai.getAttributeValue(t, "class");
		String pos = ai.getAttributeValue(t, "pos");
		String ctag = "";
		
		for(Tag tag : t.getTags()){
			if(tag.getDisamb()){
				ctag = tag.getCtag().substring(tag.getCtag().indexOf(":") + 1).replace(":", "|");
			}
		}
		
		return String.format("%d\t%s\t%s\t%s\t%s\t%s\t_\t_\t_\t_\n", tokenIndex, orth, base, pos, posext, ctag);
	};
	
	public String getStreamAsString(){
		return this.os.toString();
	}
	
	@Override
	public void writeDocument(Document document) {
		for(Sentence s: document.getSentences()){
			try {
				writeSentence(s);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	@Override
	public void flush() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

}
