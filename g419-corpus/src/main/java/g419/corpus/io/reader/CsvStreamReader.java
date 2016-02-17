package g419.corpus.io.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import g419.corpus.io.DataFormatException;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Tag;
import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;


public class CsvStreamReader extends AbstractDocumentReader {
	private BufferedReader ir;

    static private Pattern annotationPattern = Pattern.compile("([IB])-([^#]*)");
    private TokenAttributeIndex attributeIndex = null;
	private String nextParagraphId = null;
	
	public CsvStreamReader(InputStream is) {
		this.ir = new BufferedReader(new InputStreamReader(is));
		this.attributeIndex = new TokenAttributeIndex();
		this.attributeIndex.addAttribute("orth");
	}
	
	@Override
	public void close() throws DataFormatException {
		try {
			ir.close();
		} catch (IOException ex) {
			throw new DataFormatException("Failed to close input stream.");
		}
	}

	
	@Override
	protected TokenAttributeIndex getAttributeIndex() {
		return this.attributeIndex;
	}

	@Override
	public Document nextDocument() throws DataFormatException {
		
		String line = null;
		TokenAttributeIndex index = this.attributeIndex.clone();

		Paragraph paragraph = new Paragraph(nextParagraphId);
		paragraph.setAttributeIndex(index);
		this.nextParagraphId = null;
		Sentence currentSentence = new Sentence();
        HashMap<String, Annotation> annsByType = new HashMap<String, Annotation>();

		try {
			while ( ( line = this.ir.readLine()) != null ){
				line = line.trim();
						
				if ( line.length() == 0 ) {
					if (currentSentence.getTokenNumber() > 0) {
			            currentSentence.setId("sent" + paragraph.numSentences() + 1);
						paragraph.addSentence(currentSentence);
						currentSentence = new Sentence();
			            annsByType = new HashMap<String, Annotation>();
					}
				}
				else {
					String[] columns = line.split(" ");
					currentSentence.addToken(new Token(columns[0], new Tag("base", "ctag", false), index));				
			        String label = columns[columns.length - 1];
			        if(label.equals("O")){
			            annsByType = new HashMap<String, Annotation>();
			        }
			        else{
			            Matcher m = annotationPattern.matcher(label);
			            int idx = currentSentence.getTokenNumber() - 1;
			            while(m.find()){
			                String annType = m.group(2);
			                if(m.group(1).equals("B")){
			                    Annotation newAnn = new Annotation(idx, annType, currentSentence);
			                    currentSentence.addChunk(newAnn);
			                    annsByType.put(annType, newAnn);
			                }
			                else if(m.group(1).equals("I")){
			                    if(annsByType.containsKey(annType))
			                        annsByType.get(annType).addToken(idx);
			                }
			            }
			        }
					
				}			
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if ( currentSentence.getTokenNumber() > 0 ) {
            currentSentence.setId("sent" + paragraph.numSentences() + 1);
			paragraph.addSentence(currentSentence);
		}
		
		if ( paragraph.getSentences().size() == 0 ){
			return null;
		}
		else{		
			List<Paragraph> paragraphs = new ArrayList<Paragraph>();
			paragraphs.add(paragraph);
			return new Document("csv", paragraphs, index);
		}
	}
	
}
