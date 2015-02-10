package g419.crete.api.classifier.model;

public abstract class Model<T> {
	
	protected T model;
	
	public Model(T model){
		this.model = model;
	}
	
	// ----------------------- ABSTRACT METHODS ----------------------------
	public abstract void persist(String path);
	public abstract T load(String path);
}
