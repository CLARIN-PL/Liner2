package g419.spatial.tools;

import java.util.HashMap;
import java.util.Map;

import g419.liner2.api.tools.FscoreEvaluator;
import g419.spatial.structure.SpatialExpression;

public class FscoreEvaluator2 {

	Map<String, SpatialExpression> gold = new HashMap<String, SpatialExpression>();
	Map<String, SpatialExpression> decision = new HashMap<String, SpatialExpression>();
	
	public void addGold(SpatialExpression relation){
		this.gold.put(relation.getKey(), relation);
	}

	public void addDecision(SpatialExpression relation){
		this.decision.put(relation.getKey(), relation);
	}

	public boolean containsAsGold(SpatialExpression relation){
		return this.gold.containsKey(relation.getKey());
	}

	public boolean containsAsDecision(SpatialExpression relation){
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
