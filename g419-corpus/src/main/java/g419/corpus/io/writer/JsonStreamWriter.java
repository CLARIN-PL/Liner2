package g419.corpus.io.writer;

import com.google.gson.Gson;
import g419.corpus.structure.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.*;

/**
 * Zapisuje strukturę dokumentu (podział na akapity, zdania, tokeny i anotacje) do formatu json o strukturze:
 * 
 * name: String
 * chunks:
 * 		id: String
 * 		sentences:
 * 			id: String
 * 			tokens:
 * 				id: String
 * 				orth: String
 * 				ns: boolean
 * 				lexems:
 * 					base: String
 * 					ctag: String
 * 					disamb: boolean
 * annotations:
 * 	
 * 
 * @author 
 */
public class JsonStreamWriter extends AbstractDocumentWriter {
	private BufferedWriter ow = null;
	private OutputStream os = null;
	
	public static final String ATTR_ID = "id";
	public static final String ATTR_CHUNKS = "chunks";
	public static final String ATTR_SENTENCES = "sentences";
	public static final String ATTR_ANNOTATIONS = "annotations";
	public static final String ATTR_TOKENS = "tokens";
	public static final String ATTR_ORTH = "orth";
	public static final String ATTR_NS = "ns";
	public static final String ATTR_BASE = "base";
	public static final String ATTR_CTAG = "ctag";
	public static final String ATTR_DISAMB = "disamb";
	public static final String ATTR_LEXEMS = "lexems";
	
	public JsonStreamWriter(OutputStream os){
		this.os = os;
		this.ow = new BufferedWriter(new OutputStreamWriter(os));
	}

	@Override
	public void writeDocument(Document document) {
		
		Map<Token, String> tokenIds = this.generateTokenIds(document);
		Map<Annotation, String> annotationIds = this.generateAnnotationIds(document);
		
		Map<String, Collection<?>> doc = new HashMap<String, Collection<?>>();
		doc.put(JsonStreamWriter.ATTR_CHUNKS, this.getChunks(document, tokenIds));
		doc.put(JsonStreamWriter.ATTR_ANNOTATIONS, this.getAnnotations(document, tokenIds, annotationIds));
		try {
			Gson json = new Gson();
			this.ow.write(json.toJson(doc));
			this.ow.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Return a list of paragraphs as an nested set of maps.
	 * @param document
	 * @param tokenIds
	 * @return
	 */
	private List<Map<String, Object>> getChunks(Document document, Map<Token, String> tokenIds){
		List<Map<String, Object>> chunks = new ArrayList<Map<String,Object>>();
		for ( Paragraph paragraph : document.getParagraphs() ){			
			chunks.add(this.getChunk(paragraph, tokenIds));
		}
		return chunks;
	}

	/**
	 * 
	 * @param paragraph
	 * @param tokenIds
	 * @return
	 */
	private Map<String, Object> getChunk(Paragraph paragraph, Map<Token, String> tokenIds){
		Map<String, Object> paragraphMap = new HashMap<String, Object>();
		paragraphMap.put(JsonStreamWriter.ATTR_ID, null);
		paragraphMap.put(JsonStreamWriter.ATTR_SENTENCES, getSentences(paragraph, tokenIds));
		return paragraphMap;
	}
	
	/**
	 * 
	 * @param paragraph
	 * @param tokenIds
	 * @return
	 */
	private List<Map<String, Object>> getSentences(Paragraph paragraph, Map<Token, String> tokenIds){
		List<Map<String, Object>> sentences = new ArrayList<Map<String,Object>>();
		for ( Sentence sentence : paragraph.getSentences() ){
			Map<String, Object> sentenceMap = new HashMap<String, Object>();
			sentenceMap.put(JsonStreamWriter.ATTR_TOKENS, this.getTokens(sentence, tokenIds));
			sentences.add(sentenceMap);
		}
		return sentences;
	}
	
	/**
	 * 
	 * @param sentence
	 * @param tokenIds
	 * @return
	 */
	public Map<String, Object> getSentence(Sentence sentence, Map<Token, String> tokenIds){
		Map<String, Object> sentenceMap = new HashMap<String, Object>();
		sentenceMap.put(JsonStreamWriter.ATTR_ID, null);
		sentenceMap.put(JsonStreamWriter.ATTR_TOKENS, this.getTokens(sentence, tokenIds));
		return sentenceMap;
	}
	
	/**
	 * 
	 * @param document
	 * @param tokenIds
	 * @param annotationIds
	 * @return
	 */
	private List<Map<String, Object>> getAnnotations(Document document, Map<Token, String> tokenIds, Map<Annotation, String> annotationIds){
		List<Map<String, Object>> annotations = new ArrayList<Map<String,Object>>();
		for ( Annotation an : document.getAnnotations() ){
			Map<String, Object> f = new HashMap<String, Object>();
			f.put(JsonStreamWriter.ATTR_ID, annotationIds.get(an));
			f.put("text", an.getText());
			f.put("type", an.getType());
			f.put("category", an.getGroup());
			Set<String> tokens = new HashSet<String>();
			for ( Token token : an.getTokenTokens() ){
				tokens.add(tokenIds.get(token));
			}
			f.put("tokens", tokens);
			annotations.add(f);
		}
		return annotations;
	}

	/**
	 * 
	 * @param tokenIds
	 * @return
	 */
	public List<Map<String, Object>> getTokens(Sentence sentence, Map<Token, String> tokenIds){
		List<Map<String, Object>> tokens = new ArrayList<Map<String, Object>>();
		for ( Token t : sentence.getTokens() ){
			Map<String, Object> token = new HashMap<String, Object>();
			token.put(JsonStreamWriter.ATTR_ID, tokenIds.get(t));
			token.put(JsonStreamWriter.ATTR_ORTH, t.getOrth());
			token.put(JsonStreamWriter.ATTR_NS, t.getNoSpaceAfter());
			token.put(JsonStreamWriter.ATTR_LEXEMS, this.getLexems(t));
			tokens.add(token);
		}
		return tokens;
	}
	
	/**
	 * 
	 * @param token
	 * @return
	 */
	public List<Map<String, Object>> getLexems(Token token){
		List<Map<String, Object>> lexems = new ArrayList<Map<String, Object>>();		
		for ( Tag tag : token.getTags() ){
			lexems.add(this.getLexem(tag));
		}
		return lexems;
	}
	
	/**
	 * 
	 * @param tag
	 * @return
	 */
	public Map<String, Object> getLexem(Tag tag){
		Map<String, Object> tagMap = new HashMap<String, Object>();
		tagMap.put(JsonStreamWriter.ATTR_BASE, tag.getBase());
		tagMap.put(JsonStreamWriter.ATTR_CTAG, tag.getCtag());
		tagMap.put(JsonStreamWriter.ATTR_DISAMB, tag.getDisamb());
		return tagMap;
	}
	
	/**
	 * 
	 * @param document
	 * @return
	 */
	private Map<Token, String> generateTokenIds(Document document){
		Map<Token, String> ids = new HashMap<Token, String>();
		int id = 1;
		for ( Paragraph p : document.getParagraphs() ){
			for ( Sentence s : p.getSentences() ){
				for ( Token t : s.getTokens() ){
					ids.put(t, "t" + (id++));
				}
			}
		}
		return ids;
	}

	/**
	 * 
	 * @param document
	 * @return
	 */
	private Map<Annotation, String> generateAnnotationIds(Document document){
		Map<Annotation, String> ids = new HashMap<Annotation, String>();
		int id = 1;
		for ( Annotation an : document.getAnnotations() ){
			ids.put(an, "a" + (id++));
		}
		return ids;
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
			this.ow.close();
			this.os.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
