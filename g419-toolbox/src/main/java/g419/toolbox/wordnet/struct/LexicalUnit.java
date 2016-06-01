package g419.toolbox.wordnet.struct;

public class LexicalUnit {

	private String id = null;
	private String name = null;
	
	public LexicalUnit(String id, String name){
		this.id = id;
		this.name = name;
	}
	
	public String getId(){
		return this.id;
	}
	
	public String getName(){
		return this.name;
	}
	
}
