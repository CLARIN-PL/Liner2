package liner2.features.tokens;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import liner2.structure.Tag;
import liner2.structure.Token;
import liner2.structure.TokenAttributeIndex;
import pl.wroc.pwr.ci.plwordnet.plugins.princetonadapter.da.PrincetonDataLemmaRaw;
import pl.wroc.pwr.ci.plwordnet.plugins.princetonadapter.da.PrincetonDataRaw;

public class TopHyperFeature extends TokenFeature{
	
	WordnetLoader database;
	private int number;
	
	public TopHyperFeature(String name, WordnetLoader database, int number){
		super(name);
		this.database = database;
		this.number = number;
	}
	
	public ArrayList<String> getRoots(PrincetonDataRaw synset){
		ArrayList<String> roots = new ArrayList<String>();
		ArrayList<PrincetonDataRaw> hypernyms = database.getHypernyms(synset);
		if (hypernyms.size() == 0)
			for (PrincetonDataLemmaRaw lemmaRaw : synset.lemmas)
				roots.add(lemmaRaw.lemma);	
		else 
			for (PrincetonDataRaw hypernym: hypernyms)
				roots.addAll(getRoots(hypernym));		
		return roots;
	}
	
	public <K extends Comparable<? super K>, V extends Comparable<? super V>> Map<K, V> sortByValue(
			Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(
				map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				int comp = -(o1.getValue()).compareTo(o2.getValue());
				if (comp == 0)
					comp = (o1.getKey()).compareTo(o2.getKey());
				return comp;
			}
		});
		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
	
	public String generate(Token token, TokenAttributeIndex index){
		String base = token.getAttributeValue(index.getIndex("base"));
		ArrayList<PrincetonDataRaw> thisDistSynsets =  database.getSynsets(base);
		ArrayList<String> roots = new ArrayList<String>();
		for(PrincetonDataRaw synset: thisDistSynsets)
			roots.addAll(getRoots(synset));
		LinkedHashMap<String, Integer> rootMap = new LinkedHashMap<String, Integer>();
		for (String s : roots){
			if (rootMap.containsKey(s))
				rootMap.put(s, rootMap.get(s) + 1);
			else
				rootMap.put(s, 1);
		}
		List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(
				rootMap.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
				int comp = -(o1.getValue()).compareTo(o2.getValue());
				if (comp == 0)
					comp = (o1.getKey()).compareTo(o2.getKey());
				return comp;
			}
		});		
		/*for (Map.Entry<String, Integer> cursor : list)
			System.out.println(cursor.getKey() + " : " + cursor.getValue());*/	
		if (this.number > 0 && this.number <= list.size())
			return list.get(this.number - 1).getKey();		
		else if (this.number == 0 && list.size() > 0){
			Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
				public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
					return (o1.getKey()).compareTo(o2.getKey());
				}
			});		
			return list.get(0).getKey();
		}
		return null;
	}
	
	/*public static void main(String args[]){
		WordnetLoader database = new WordnetLoader("/home/kotu/projects/timex-recognition/liner2-models-fat-pack/data/plwordnet_2_0_2_pwn_format");
		TopHyperFeature s = new TopHyperFeature("top4hyper2",database,0);
		TokenAttributeIndex attributeIndex = new TokenAttributeIndex();
	    attributeIndex.addAttribute("orth");
	    attributeIndex.addAttribute("base");
	    attributeIndex.addAttribute("ctag");
	    Tag tag = new Tag("przedwczoraj", "subst:sg:nom:m2", true);
		Token token = new Token("w", tag, attributeIndex);
		System.out.println("|"+s.generate(token, attributeIndex)+"|");
	}	*/
}
