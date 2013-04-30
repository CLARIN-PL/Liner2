package liner2.features.tokens;

import java.util.ArrayList;
import java.util.Collections;

import pl.wroc.pwr.ci.plwordnet.plugins.princetonadapter.da.PrincetonDataLemmaRaw;
import pl.wroc.pwr.ci.plwordnet.plugins.princetonadapter.da.PrincetonDataRaw;
import liner2.structure.Token;

public class SynonymFeature extends TokenFeature{
	
	WordnetLoader database;
	
	public SynonymFeature(String name, WordnetLoader database){
		super(name);
		this.database = database;
	}
	
	public String generate(Token token){
		String base = token.getAttributeValue(1);
		ArrayList<String> lemmas = new ArrayList<String>();
		ArrayList<PrincetonDataRaw> synsets =  database.getSynsets(base);
		if(synsets.isEmpty())
			return base;
		for(PrincetonDataLemmaRaw lr: synsets.get(0).lemmas)
			lemmas.add(lr.lemma);
		lemmas.remove(base);
		if(synsets.size() > 1){
			
			for(PrincetonDataRaw synset: synsets.subList(1, synsets.size())){
				ArrayList<String> common_lemmas = new ArrayList<String>();
				for(PrincetonDataLemmaRaw lr: synset.lemmas)
					if(lemmas.contains(lr.lemma))
						common_lemmas.add(lr.lemma);
				lemmas = common_lemmas;
			}
		}
		
		if(lemmas.isEmpty())
			return base;
		Collections.sort(lemmas);
		return lemmas.get(0);
			
	}
}