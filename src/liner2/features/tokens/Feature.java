package liner2.features.tokens;

public abstract class Feature {
	
	String name;
	
	public Feature(String name){
		this.name = name;
	}
	
	public String getName(){
		return this.name;
	}

}
