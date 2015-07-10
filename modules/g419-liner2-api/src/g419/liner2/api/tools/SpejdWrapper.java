package g419.liner2.api.tools;

import java.io.IOException;

import morfologik.stemmers.Stempel;
import morfologik.stemmers.Stempelator;
import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Tag;
import g419.corpus.structure.Token;
import ipipan.spejd.entities.Interpretation;
import ipipan.spejd.entities.Segment;
import ipipan.spejd.rules.LexDictionary;
import ipipan.spejd.rules.RuleSet;
import ipipan.spejd.util.Config;

public class SpejdWrapper {

	private Config config = null;
	private RuleSet rules = null;
	private Stempelator stemmer = null;
	private LexDictionary dict = null; 
	
	public SpejdWrapper() throws IOException{
		this.config = new Config("resources/gramatyka_Spejd_NKJP_1.0/config.ini", new String[0]);
		this.rules = new RuleSet(this.config);
        this.stemmer = new Stempelator();
        this.dict = new LexDictionary(this.config.lexDictionaries, config);
	}
	
	public void process(Document document) throws Exception{
		for ( Paragraph p : document.getParagraphs() ){
			for ( Sentence s : p.getSentences() ){
				this.process(s);
			}
		}
	}
	
	public void process(Sentence sentence) throws Exception{
		ipipan.spejd.rules.Sentence sentenceSpejd = this.convert(sentence);
		this.rules.applyTo(sentenceSpejd);
	}
	
	public ipipan.spejd.rules.Sentence convert(Sentence sentence){
		ipipan.spejd.rules.Sentence sentenceSpejd = 
				new ipipan.spejd.rules.Sentence(this.config, this.dict, this.stemmer);
		for ( Token token : sentence.getTokens() ){			
			sentenceSpejd.addEntity(this.convert(token));
		}
		return sentenceSpejd;
	}
	
	public Segment convert(Token token){
		Interpretation[] inters = new Interpretation[1] ;
		Tag tag = token.getDisambTag();
		inters[0] = new Interpretation(String.format("%s:%s", tag.getBase(), tag.getCtag()), this.config);
		Segment segment = new Segment(""+token.hashCode(), token.getOrth(), inters, this.config);
		return segment;
	}
}
