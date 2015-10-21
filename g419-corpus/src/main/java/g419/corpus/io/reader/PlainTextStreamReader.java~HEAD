package g419.corpus.io.reader;

import g419.corpus.io.DataFormatException;
import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Tag;
import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;

import java.io.*;


public class PlainTextStreamReader extends AbstractDocumentReader {

	private Document document;
	private String docName;

	public PlainTextStreamReader(String docName, InputStream is, String analyzer) {
		this.docName = docName;
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
			Token token = new Token(ai);
			String[] tokenAttrs = tokenStr.split(" ");
			for (int i = 0; i < tokenAttrs.length; i++)   {
				token.setAttributeValue(i, tokenAttrs[i]); }
            Tag tag = new Tag(ai.getAttributeValue(token, "base"), ai.getAttributeValue(token, "ctag"), false);
            token.addTag(tag);
			sentence.addToken(token);
		}
		Paragraph paragraph = new Paragraph(null);
		paragraph.setAttributeIndex(ai);
        sentence.setId("sent" + paragraph.numSentences() + 1);
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

		try {
			File tager_input = File.createTempFile("wcrft_input", ".txt");
			BufferedWriter tager_writer = new BufferedWriter(
					new FileWriter(tager_input));

			tager_writer.write(cSeq, 0, cSeq.length());
			tager_writer.close();
			String cmd = "";
			if (analyzer.equals("wcrft")){
				cmd = "wcrft-app nkjp_e2.ini -i text -o ccl " + tager_input.getAbsolutePath();
			}
			else if(analyzer.equals("maca")){
				cmd =  "maca-analyse -qs morfeusz-nkjp-official -o ccl";
			}
			else{
				throw new Exception("Unrecognized analyzer: " + analyzer);
			}
			Process tager = Runtime.getRuntime().exec(cmd);
			InputStream tager_in = tager.getInputStream();
			AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(docName, tager_in, "ccl");
			return reader.nextDocument();
			
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}				
	}
	
}
