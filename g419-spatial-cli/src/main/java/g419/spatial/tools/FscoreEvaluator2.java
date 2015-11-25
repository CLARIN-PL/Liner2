package g419.spatial.tools;

import java.util.HashMap;
import java.util.Map;

import g419.liner2.api.tools.FscoreEvaluator;
import g419.spatial.structure.SpatialRelation;

public class FscoreEvaluator2 {

	Map<String, SpatialRelation> gold = new HashMap<String, SpatialRelation>();
	Map<String, SpatialRelation> decision = new HashMap<String, SpatialRelation>();
	
	public void addGold(SpatialRelation relation){
		this.gold.put(relation.getKey(), relation);
	}

	public void addDecision(SpatialRelation relation){
		this.decision.put(relation.getKey(), relation);
	}

	public boolean containsAsGold(SpatialRelation relation){
		return this.gold.containsKey(relation.getKey());
	}

	public boolean containsAsDecision(SpatialRelation relation){
		return this.decision.containsKey(relation.getKey());
	}
	
	public void evaluate(){
		FscoreEvaluator eval = new FscoreEvaluator();
		for ( String key : this.decision.keySet() ){
			if ( this.gold.containsKey(key) ){
				eval.addTruePositive();
			}
			else{
				eval.addFalsePositive();
			}
		}
		for ( String key : gold.keySet() ){
			if ( !this.decision.containsKey(key) ){
				eval.addFalseNegative();
			}
		}
		eval.printTotal();
	}

}
