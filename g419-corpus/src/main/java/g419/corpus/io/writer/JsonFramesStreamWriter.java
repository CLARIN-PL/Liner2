package g419.corpus.io.writer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Frame;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;


/**
 * 
 * @author 
 */
public class JsonFramesStreamWriter extends AbstractDocumentWriter {
	private BufferedWriter ow = null;
	private OutputStream os = null;
	
	public JsonFramesStreamWriter(OutputStream os){
		this.os = os;
		this.ow = new BufferedWriter(new OutputStreamWriter(os));
	}

	@Override
	public void writeDocument(Document document) {
		
		Map<Token, String> tokenIds = this.generateTokenIds(document);
		Map<Annotation, String> annotationIds = this.generateAnnotationIds(document);
		
		Map<String, Collection<?>> doc = new HashMap<String, Collection<?>>();
		doc.put("tokens", this.getTokens(document, tokenIds));
		doc.put("frames", this.getFrames(document, annotationIds));
		doc.put("annotations", this.getAnnotations(document, tokenIds, annotationIds));
		try {
			Gson json = new Gson();
			this.ow.write(json.toJson(doc));
			this.ow.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private List<Map<String, Object>> getFrames(Document document, Map<Annotation, String> annotationIds){
		List<Map<String, Object>> frames = new ArrayList<Map<String,Object>>();
		for ( Frame frame : document.getFrames() ){
			Map<String, Object> f = new HashMap<String, Object>();
			Map<String, Object> slots = new HashMap<String, Object>();
			
			Set<String> slotNames = new HashSet<String>();
			slotNames.addAll(frame.getSlots().keySet());
			slotNames.addAll(frame.getSlotAttributes().keySet());
			
			for ( String slotName : slotNames ){
				Annotation an = frame.getSlot(slotName);
				Map<String, Object> annotation = new HashMap<String, Object>();
				if ( an != null ){
					annotation.put("id", annotationIds.get(an));
				}
									
				Map<String, String> annotationAttributes = frame.getSlotAttributes(slotName);
				if ( annotationAttributes != null ){
					annotation.put("attributes", annotationAttributes);
				}
				
				slots.put(slotName, annotation);
			}
			f.put("id", "x");
			f.put("type", frame.getType());
			f.put("slots", slots);
			frames.add(f);
		}
		return frames;
	}

	private List<Map<String, Object>> getAnnotations(Document document, Map<Token, String> tokenIds, Map<Annotation, String> annotationIds){
		List<Map<String, Object>> annotations = new ArrayList<Map<String,Object>>();
		for ( Annotation an : document.getAnnotations() ){
			Map<String, Object> f = new HashMap<String, Object>();
			f.put("id", annotationIds.get(an));
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

	private List<List<String>> getTokens(Document document, Map<Token, String> tokenIds){
		List<List<String>> tokens = new ArrayList<List<String>>();
		for ( Paragraph p : document.getParagraphs() ){
			for ( Sentence s : p.getSentences() ){
				for ( Token t : s.getTokens() ){
					List<String> token = new ArrayList<String>();
					token.add(tokenIds.get(t));
					token.add(t.getOrth());
					token.add(t.getDisambTag().getBase());
					token.add(t.getDisambTag().getCtag());
					token.add(t.getNoSpaceAfter()?"1":"0");
					tokens.add(token);
				}
			}
		}
		return tokens;
	}
	
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
			//((GZIPOutputStream)this.os).finish();
			this.os.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
