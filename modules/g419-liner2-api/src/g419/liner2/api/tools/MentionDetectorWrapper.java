package g419.liner2.api.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import pl.waw.ipipan.zil.core.md.detection.Detector;
import pl.waw.ipipan.zil.core.md.detection.zero.ZeroSubjectDetector;
import pl.waw.ipipan.zil.core.md.entities.Interpretation;
import pl.waw.ipipan.zil.core.md.entities.Text;
import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Tag;
import g419.corpus.structure.Token;
import g419.liner2.api.Liner2;

public class MentionDetectorWrapper {

	ZeroSubjectDetector zeroSubjectModel = null;
	
	public MentionDetectorWrapper() throws FileNotFoundException{
		// Todo
		String zeroSubjectModelPath = Liner2.class.getProtectionDomain().getCodeSource().getLocation().getPath().replace("g419-liner2-api.jar","") + "/../resources/zero_subject_model.bin";
		InputStream zeroSubjectDetectionModelStream = null;
		zeroSubjectDetectionModelStream = new FileInputStream(new File(zeroSubjectModelPath));
		zeroSubjectModel = new ZeroSubjectDetector(zeroSubjectDetectionModelStream);		
	}
	
	public void process(Document document){
		System.out.println("convert");
		Text text = MentionDetectorWrapper.convert(document);
		Detector.findMentionsInText(text, this.zeroSubjectModel);
		for ( pl.waw.ipipan.zil.core.md.entities.Paragraph p : text ){
			for ( pl.waw.ipipan.zil.core.md.entities.Sentence s : p ){
				for ( pl.waw.ipipan.zil.core.md.entities.Mention m : s.getMentions()){					
					System.out.println(m.toString());
					for (pl.waw.ipipan.zil.core.md.entities.Token t : m.getHeadSegments() ){
						System.out.println(" head: " + t.toString());
					}
				}
			}
		}
	}
	
	private static Text convert(Document document){
		Text text = new Text("d1");
		for ( Paragraph p : document.getParagraphs() ){
			text.add(MentionDetectorWrapper.convert(p));
		}
		return text;
	}
	
	private static pl.waw.ipipan.zil.core.md.entities.Paragraph convert(Paragraph paragraph){
		pl.waw.ipipan.zil.core.md.entities.Paragraph p = new pl.waw.ipipan.zil.core.md.entities.Paragraph();
		for ( Sentence sentence : paragraph.getSentences() ){
			p.add(MentionDetectorWrapper.convert(sentence));
		}
		return p;
	}
	
	private static pl.waw.ipipan.zil.core.md.entities.Sentence convert(Sentence sentence){
		pl.waw.ipipan.zil.core.md.entities.Sentence s = new pl.waw.ipipan.zil.core.md.entities.Sentence();
		for ( Token token : sentence.getTokens() ){
			s.add(MentionDetectorWrapper.convert(token));
		}
		return s;
	}
	
	private static pl.waw.ipipan.zil.core.md.entities.Token convert(Token token){
		pl.waw.ipipan.zil.core.md.entities.Token t = new pl.waw.ipipan.zil.core.md.entities.Token();
		t.setOrth(token.getOrth());
		t.setChosenInterpretation(MentionDetectorWrapper.convert(token.getDisambTag()));
		return t;
	}
	
	private static Interpretation convert(Tag tag){
		String[] c_tag = tag.getCtag().split(":", 2);
		Interpretation interpretation = new Interpretation(c_tag[0], c_tag.length>1 ? c_tag[1] : "", tag.getBase());		
		return interpretation;
	}
	
}
