package g419.liner2.api.tools;

import java.util.HashMap;

public class TrieDictNode {

	private HashMap<String,TrieDictNode> children = new HashMap<String,TrieDictNode>();
	private boolean terminal;

	public TrieDictNode(boolean terminal){
		this.terminal = terminal;
	}

	public void addChild(String value, boolean terminal){
		if (!hasChild(value)) 
			children.put(value, new TrieDictNode(terminal));
		else if (terminal == true){
			TrieDictNode existing = getChild(value);
			if (!existing.isTerminal()){
				existing.setTerminal(true);
				children.put(value, existing);
			}
		}
	}
	
	public boolean hasChild(String value){
		return children.containsKey(value);
	}
	
	public TrieDictNode getChild(String value){
		return children.get(value);
	}
	
	public boolean isTerminal() {
		return terminal;
	}

	public void setTerminal(boolean terminal) {
		this.terminal = terminal;
	}
		
	public String toString(){
		String out = this.children.toString();
		for(TrieDictNode c: this.children.values())
			out += c.toString();
		return this.children.toString();
	}
	public void size(){
		System.out.println(children.size());
	}

}
