package g419.tools.maltfeature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import g419.corpus.structure.Sentence;

/**
 * 
 * @author czuk
 *
 */
public class DependencyPath{
	
	private Map<MaltPatternNode, Integer> matchedNodes = new HashMap<MaltPatternNode, Integer>();
	private List<MaltPatternNode> matches = new ArrayList<MaltPatternNode>();
	
	public Map<MaltPatternNode, Integer> getMatchedNodes(){
		return this.matchedNodes;
	}
	
	public void addMatchedNode(MaltPatternNode node, Integer index){
		this.matchedNodes.put(node, index);
		this.matches.add(node);
	}
	
	public Integer getLastIndex(){
		return this.matchedNodes.get(this.matches.get(this.matches.size()-1));
	}
	
	public DependencyPath clone(){
		DependencyPath path = new DependencyPath();
		for ( MaltPatternNode node : this.matchedNodes.keySet() ){
			path.addMatchedNode(node, this.matchedNodes.get(node));
		}
		return path;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		for ( MaltPatternNode node : this.matches ){
			sb.append(node.getLabel() + ":" + this.matchedNodes.get(node) + "; ");
		}
		return sb.toString();
	}
	
	public String toString(Sentence sentence){
		StringBuilder sb = new StringBuilder();
		for ( MaltPatternNode node : this.matches ){
			int index = this.matchedNodes.get(node);
			sb.append(node.getLabel() + ":[" + index + "]" + sentence.getTokens().get(index).getOrth() + "; ");
		}
		return sb.toString();
	}
	
	public String generalize(Sentence sentence){
		StringBuilder sb = new StringBuilder();
		for ( MaltPatternNode node : this.matches ){
			int index = this.matchedNodes.get(node);
			sb.append(node.getLabel() + "=" + sentence.getTokens().get(index).getDisambTag().getBase() + "; ");
		}
		return sb.toString().trim();		
	}
}