package g419.toolbox.wordnet.struct;

import java.util.*;

public class WordnetPl {

	Map<String, LexicalUnit> units = new HashMap<String, LexicalUnit>();
	Map<String, List<LexicalRelation>> unitRelations = new HashMap<String, List<LexicalRelation>>();
	
	public void addLexicalUnit(LexicalUnit unit) {
		this.units.put(unit.getId(), unit);		
	}

	public Collection<LexicalUnit> getLexicalUnits(){
		return this.units.values();
	}
	
	public LexicalUnit getLexicalUnit(String id){
		return this.units.get(id);
	}
	
	public void addLexicalRelation(LexicalRelation relation){
		List<LexicalRelation> relations = this.unitRelations.get(relation.getRelation());
		if ( relations == null ){
			relations = new ArrayList<LexicalRelation>();
			this.unitRelations.put(relation.getRelation(), relations);
		}
		relations.add(relation);
	}
	
	public List<LexicalRelation> getLexicalRelations(String relation){
		return this.unitRelations.get(relation);
	}
}
