package g419.crete.api.features;

public class FeatureValue<T> {
	private T value;
	
	public FeatureValue(T value){
		this.value = value;
	}
	
	public T getValue(){
		return value;
	}
}
