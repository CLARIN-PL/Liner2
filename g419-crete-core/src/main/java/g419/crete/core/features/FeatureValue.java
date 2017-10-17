package g419.crete.core.features;

public class FeatureValue<T> {
	private T value;
	
	public FeatureValue(T value){
		this.value = value;
	}
	
	public T getValue(){
		return value;
	}
}
