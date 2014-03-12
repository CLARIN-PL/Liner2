package liner2.reader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import liner2.structure.Document;
import liner2.structure.Paragraph;
import liner2.structure.Sentence;
import liner2.structure.Tag;
import liner2.structure.Token;
import liner2.structure.TokenAttributeIndex;
import liner2.tools.DataFormatException;

public class PlainTextStreamReader extends AbstractDocumentReader {

	private Document document;

	public PlainTextStreamReader(InputStream is, String analyzer) {
		this.read(is, analyzer);		
	}

	private void read(InputStream is, String analyzer){
		BufferedReader input_reader = new BufferedReader(new InputStreamReader(is));
		
		StringBuilder sb = new StringBuilder();
		String line = null;
		try {
			while (( line = input_reader.readLine()) != null )
				sb.append(line + "\n");
			input_reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
					
		if (analyzer.equals("none"))
			this.document = analyzePlain(sb.toString().trim());
		else
			this.document = analyze(sb.toString().trim(), analyzer);
		
	}
	
	@Override
	public void close() throws DataFormatException {
	}

	@Override
	protected TokenAttributeIndex getAttributeIndex() {
		if ( this.document != null )
			return this.document.getAttributeIndex();
		else
			return null;
	}

	@Override
	public Document nextDocument() {
		if ( this.document != null ){
			Document document = this.document;
			this.document = null;
			return document;
		}
		else{			
			return null;
		}
	}
	
	/**
	 * Analyze text provided with morphological information.
	 * @param cSeq
	 * @return
	 */
	private Document analyzePlain(String cSeq) {
		Sentence sentence = new Sentence();
		TokenAttributeIndex ai = new TokenAttributeIndex();
		ai.addAttribute("orth");
		ai.addAttribute("base");
		ai.addAttribute("ctag");
		sentence.setAttributeIndex(ai);

		String[] tokens = cSeq.trim().split("  ");
		for (String tokenStr : tokens) {
			Token token = new Token();
			String[] tokenAttrs = tokenStr.split(" ");
			for (int i = 0; i < tokenAttrs.length; i++)   {
				token.setAttributeValue(i, tokenAttrs[i]); }
            Tag tag = new Tag(token.getAttributeValue(1), token.getAttributeValue(2), false);
            token.addTag(tag);
			sentence.addToken(token);
		}
		Paragraph paragraph = new Paragraph(null);
		paragraph.setAttributeIndex(ai);
		paragraph.addSentence(sentence);
		Document document = new Document("terminal input", ai);
		document.addParagraph(paragraph);
		return document;
	}
	
	/**
	 * Analyze plain text using maca analyzer or wcrft tagger.
	 * @param cSeq
	 * @param analyzer
	 * @return
	 */
	private Document analyze(String cSeq, String analyzer) {
		// prepare maca command
		String cmd = "maca-analyse -qs morfeusz-nkjp-official -o ccl";

		if ( analyzer.equals("wcrft") )
			cmd = "wcrft nkjp_s2.ini -d " + "/nlp/resources/model_nkjp10_wcrft_s2" + " -i text -o ccl - ";
				
		Process tager = null;
		try {
			tager = Runtime.getRuntime().exec(cmd);
			InputStream tager_in = tager.getInputStream();
			OutputStream tager_out = tager.getOutputStream();
			
			BufferedWriter tager_writer = new BufferedWriter(
				new OutputStreamWriter(tager_out));
				
			tager_writer.write(cSeq, 0, cSeq.length());
			tager_writer.close();
			
			AbstractDocumentReader reader = ReaderFactory.get().getStreamReader("terminal input", tager_in, "ccl");
			return reader.nextDocument();
			
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}				
	}
	
}
