package g419.liner2.core.tools;

// ToDo: Rename to ConfusionMatrix
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

	public void addTruePositive(int n){
		truePositives+=n;
	}

	public void addTrueNegative(){
		trueNegatives++;
	}

	public void addTrueNegative(int n){
		trueNegatives+=n;
	}

	public void addFalsePositive(){
		falsePositives++;
	}

	public void addFalsePositive(int n){
		falsePositives+=n;
	}

	public void addFalseNegative(){
		falseNegatives++;
	}

	public void addFalseNegative(int n){
		falseNegatives+=n;
	}

	public int getTruePositiveCount(){
		return this.truePositives;
	}
	
	public int getFalsePositiveCount(){
		return this.falsePositives;
	}

	public int getFalseNegativesCount(){
		return falseNegatives;
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
	
	public void printTotal(){
		print(precision(), recall(), f(), truePositives, falsePositives, falseNegatives);
	}
	
	public void print(float precision, float recall, float f, int tp, int fp, int fn){
		System.out.println(
			String.format(
				"Score: Precision=%.2f (%d/%d), Recall=%.2f (%d/%d), F=%.2f",
				precision * 100, tp, tp + fp, recall * 100, tp, tp + fn, f * 100
			)
		);
	}
}
