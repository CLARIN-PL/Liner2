package g419.crete.core.classifier.serialization;

public abstract class Serializer<T> {
	
	protected T model;
	
	public Serializer(T model){
		this.model = model;
	}
	
	public T getModel(){
		return this.model;
	}
	
	// ----------------------- ABSTRACT METHODS ----------------------------
	public abstract void persist(String path);
	public abstract void load(String path);
}
