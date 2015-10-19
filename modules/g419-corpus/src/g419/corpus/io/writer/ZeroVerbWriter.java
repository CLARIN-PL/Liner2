package g419.corpus.io.writer;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Tag;
import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;



//import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Pattern;

public class ZeroVerbWriter extends AbstractDocumentWriter{

	private static final String AGLT_CLASS = "aglt";
	private static final String QUB_CLASS = "qub";
	
	private OutputStream os;
	private static List<String> verbPos =  Arrays.asList(new String[]{"fin", "praet", "winien", "bedzie", "aglt"});
	private static int zeroVerbCount = 0;
	private static int verbCount = 0;
	private static int undelZeroAglt = 0;
	
	private static boolean delAglt = false;
	private static boolean delQub = false;
	
	private static String docid = ""; 
	
	public ZeroVerbWriter(OutputStream os){
		this.os = os;
	}
	
	public ZeroVerbWriter(){
		this.os = new ByteArrayOutputStream();
	}
	
//	public static String[] convertSentence(Sentence s){
//		int index = 0;
//		String[] sentenceConll = new String[s.getTokenNumber()];
//		TokenAttributeIndex ai = s.getAttributeIndex();
//		for(Token t : s.getTokens()){
//			sentenceConll[index] = convertToken(t, ai, ++index);
//		}
//		return sentenceConll;
//	}
	
	private void writeSentence(Sentence s) throws IOException{
		int index = 0;
		TokenAttributeIndex ai = s.getAttributeIndex();
//		System.out.println(s.annotationsToString());
		LinkedHashSet<Annotation> zeroAnnotations = s.getAnnotations(Arrays.asList(new Pattern[]{
				Pattern.compile("anafora_verb_null.*"), 
				Pattern.compile("anafora_wyznacznik")
		}));
		TreeSet<Integer> zeroTokens = new TreeSet<>();
		for(Annotation zeroAnnotation : zeroAnnotations){
			boolean onlyVerbs = true;
			for(Integer zeroToken: zeroAnnotation.getTokens()){
				if(!verbPos.contains(ai.getAttributeValue(s.getTokens().get(zeroToken), "ctag").split(":")[0])){
					onlyVerbs = false;
					break;
				}
			}
			if(onlyVerbs){
				zeroTokens.addAll(zeroAnnotation.getTokens());
			}
		}
//		System.out.println(zeroTokens);
		for(Token t : s.getTokens()){
			boolean zeroVerb = zeroTokens.contains(s.getTokens().indexOf(t));
			boolean zero1 = zeroTokens.contains(s.getTokens().indexOf(t) + 1);
			boolean zero2 = zeroTokens.contains(s.getTokens().indexOf(t) + 2);
			this.os.write(convertToken(t, ai, ++index, zeroVerb, zero1, zero2,  s).getBytes());
			//writeToken(t, ai, ++index);
		}
	};
	
	public static String convertToken(Token t, TokenAttributeIndex ai, int tokenIndex, boolean zeroVerb, boolean zero1, boolean zero2, Sentence sentence){
		String orth = t.getOrth();
		String base = ai.getAttributeValue(t, "base");
		String posext = ai.getAttributeValue(t, "class");
		String pos = ai.getAttributeValue(t, "pos");
		String ctag = "";
		String cpos = null;
		String label = "N";
		
		
		Tag disambTag = t.getTags().get(0);
		for(Tag iterTag : t.getTags())
			if(iterTag.getDisamb()) 
				disambTag = iterTag;
		
				
//		for(Tag tag : t.getTags()){
//			if(tag.getDisamb() || t.getTags().size() == 1){
				Tag tag = disambTag;
				int firstSep = Math.max(0, tag.getCtag().indexOf(":"));
				if(firstSep > 0) cpos = tag.getCtag().substring(0, firstSep);
				ctag = tag.getCtag().substring(tag.getCtag().indexOf(":") + 1).replace(":", "|");
				if(ctag.equals(pos) || ctag.equals(posext)) ctag = "_";
				if(pos == null && posext == null){
					if(cpos != null){
						pos = cpos;
						posext = cpos;
					}
					else{
						pos = ctag;
						posext = ctag;
						ctag = "_";
					}
				}
		
		if(pos.startsWith(AGLT_CLASS) && !delAglt){
			zeroVerbCount++;
			String prec = "", foll = "";
			boolean precQub = false, follVb = false, fem = false, sg = false, pri = false, sec = false, ter = true;
			
			pri = ctag.contains("|pri|") || ctag.startsWith("pri|") || ctag.endsWith("|pri");
			sec = ctag.contains("|sec|") || ctag.startsWith("sec|") || ctag.endsWith("|sec");
			ter = !pri && !sec;
			
			try{
				prec = sentence.getTokens().get(tokenIndex - 2).getOrth();
				precQub = QUB_CLASS.equalsIgnoreCase(ai.getAttributeValue(sentence.getTokens().get(tokenIndex - 2), "ctag").split(":")[0]);
			}catch(Exception e){}
			
			for(int i = tokenIndex; i < sentence.getTokenNumber(); i++){
				Token vst = sentence.getTokens().get(i);
				follVb = verbPos.contains(ai.getAttributeValue(vst, "ctag").split(":")[0]);
				if(follVb){
//					System.out.println(ctag);
//					System.out.println(ai.getAttributeValue(vst, "ctag"));
					fem = ai.getAttributeValue(vst, "ctag").contains(":f:") || ai.getAttributeValue(vst, "ctag").startsWith("f:") || ai.getAttributeValue(vst, "ctag").endsWith(":f");
					sg = ai.getAttributeValue(vst, "ctag").contains(":sg:") || ai.getAttributeValue(vst, "ctag").startsWith("sg:") || ai.getAttributeValue(vst, "ctag").endsWith(":sg");
//					System.out.println(ai.getAttributeValue(vst, "ctag") + ":" + (pri?"pri":(sec?"sec":"ter")));
					foll = vst.getOrth();
					break;
				}
			}
			
//			if(fem) System.out.println("FEM");
//			if(sg) System.out.println("SG");
//			System.out.print();
			if(precQub && follVb) System.out.println(prec + " " + orth + " " + foll +  "  --->  " + foll + prec + orth);
			else if (follVb) System.out.println(prec + " " + orth + " " + foll +  "  --->  " + prec + " " + foll + (fem||!sg?"":"e") + orth);
			else System.out.println(docid + " SHIT: " + prec + " " + orth + " " + foll);
			
		}
				
		if(pos.startsWith(AGLT_CLASS) && delAglt){
			delAglt = false;
			return "";
		}
		
		if(pos.startsWith(QUB_CLASS) && delQub){
			delQub = false;
			return "";
		}
		
		
//			}
//		}
		//TODO: ctag dla interp conj, etc.
		//TODO: iż -> dlaczego nie ma pos?
		
		if(verbPos.contains(pos) && !pos.equalsIgnoreCase(AGLT_CLASS)){
			// 1. verb + aglt
			if(tokenIndex + 1 < sentence.getTokens().size()){
				Token agltCandidate = sentence.getTokens().get(tokenIndex);
				if(AGLT_CLASS.equalsIgnoreCase(ai.getAttributeValue(agltCandidate, "ctag").split(":")[0])){
					String person = ai.getAttributeValue(agltCandidate, "person");
					pos = pos +  "|" + person;
					orth = orth + ai.getAttributeValue(agltCandidate, "orth");
					delAglt = true;
					zeroVerb = zeroVerb || zero1;
				}
			}
			
			// 2. verb + qub + aglt
			if(tokenIndex + 2 < sentence.getTokens().size()){
				Token qubCandidate = sentence.getTokens().get(tokenIndex);
				Token agltCandidate = sentence.getTokens().get(tokenIndex + 1);
				if(AGLT_CLASS.equalsIgnoreCase(ai.getAttributeValue(agltCandidate, "ctag").split(":")[0]) && QUB_CLASS.equalsIgnoreCase(ai.getAttributeValue(qubCandidate, "ctag").split(":")[0])){
					String person =  ai.getAttributeValue(agltCandidate, "person");
					pos = pos +  "|" + person;
					orth = orth + ai.getAttributeValue(qubCandidate, "orth") + ai.getAttributeValue(agltCandidate, "orth");
					delAglt = true;
					delQub = true;
					zeroVerb = zeroVerb || zero2;
				}
			}
			
			
			
			
			verbCount++;
			label = "V";
			if(zeroVerb){
				label = "Z";
				zeroVerbCount ++;
//				System.out.println("ZERO");
//				System.out.println(zeroVerbCount);
//				System.out.println(orth);
			}
		}
				
		
		return String.format("%d\t%s\t%s\t%s\t%s\t%s\n", tokenIndex, orth, base, pos, ctag, label);
//		return String.format("%d\t%s\t%s\t%s\t%s\t%s\t_\t_\t_\t_\n", tokenIndex, orth, base, pos, posext, ctag);
	};
	
	public String getStreamAsString(){
		return this.os.toString();
	}
	
	private void writeNewLine() throws IOException{
		this.os.write("\n".getBytes());
	}
	
	@Override
	public void writeDocument(Document document) {
		docid = document.getName();
		for(Sentence s: document.getSentences()){
			try {
				writeSentence(s);
				writeNewLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		docid = "";
//		System.out.println(document.getName());
//		System.out.println("VERBS IN TOTAL: "+verbCount);
//		System.out.println("ZERO VERBS IN TOTAL: "+ zeroVerbCount);
//		System.out.println("ZERO VERBS IN TOTAL (W. AGLT): "+ (zeroVerbCount + undelZeroAglt));
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
