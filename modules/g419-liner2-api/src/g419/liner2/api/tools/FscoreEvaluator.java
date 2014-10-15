package g419.liner2.api.tools;

public class FscoreEvaluator {

	protected int truePositives;
	protected int trueNegatives;
	protected int falsePositives;
	protected int falseNegatives;
	
	public FscoreEvaluator(){
		reset();
	}
	
	public void reset(){
		truePositives = 0;
		trueNegatives = 0;
		falsePositives = 0;
		falseNegatives = 0;
	}
	
	
	public void addTruePositive(){
		truePositives++;
	}
	
	public void addTrueNegative(){
		trueNegatives++;
	}
	
	public void addFalsePositive(){
		falsePositives++;
	}
	
	public void addFalseNegative(){
		falseNegatives++;
	}
	
	protected float safeDiv(float numerator, float denominator, float defaultValue){
		if(denominator == 0) return defaultValue;
		return numerator / denominator;
	}
	
	public float precision(){
		return safeDiv(truePositives, truePositives + falsePositives, 0.0f);
	}
	
	public float recall(){
		return safeDiv(truePositives, truePositives + falseNegatives, 0.0f);
	}
	
	public float f(){
		float precision = precision();
		float recall = recall();
		
		return safeDiv(2 * precision * recall, precision + recall, 0.0f); 
	}
	
}
