package g419.tools.maltfeature;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import g419.liner2.api.tools.parser.MaltSentence;

public class MaltPattern {

    private static Pattern relFrom = Pattern.compile("^--\\(([a-z_]+)\\)-->$");
    private static Pattern relTo = Pattern.compile("^<--\\(([a-z_]+)\\)--$");

    // Symboliczna nazwa wzorca
	String patternName;

	// Liczba wierzchołków jest o jeden większa od liczby krawędzi	
    List<MaltPatternNode> nodes = new ArrayList<MaltPatternNode>();
    
    // N-ta krawędź jest pomiędzy wierzchołkiem n i n+1.
    List<MaltPatternEdge> edges = new ArrayList<MaltPatternEdge>();
    
    public MaltPattern(String pattern){
        parsePattern(pattern);
    }
    
    public MaltPatternNode getFirstNode(){
    	return this.nodes.get(0);
    }
    
    public String getPatternString(){
    	return this.patternName;
    }
    
    public List<MaltPatternNode> getNodes(){
    	return this.nodes;
    }
    
    public List<MaltPatternEdge> getEdges(){
    	return this.edges;
    }


    /**
     * Parsuje wzorzec do postaci listy wierzchołków i krawędzi.
     * Przykład wzorca:
     * <code>
     * name --(subst)--> X
     * </code>
     * @param pattern
     */
    private void parsePattern(String pattern) {
        patternName = pattern;
        String[] elements = pattern.split("\\s+");
        
        MaltPatternNode node = this.parseNode(elements[0]);
        if ( node == null ){
        	return;
        }
        this.nodes.add(node);
        for (int i=1; i<elements.length; i+=2 ){
        	this.edges.add(this.parseEdge(elements[i]));
        	this.nodes.add(this.parseNode(elements[i+1]));
        }        
    }

    
    /**
     * Parsuje string reprezentujący wierzchołek ścieżki w drzewie zależnościowym.
     * @param data
     * @return
     */
    private MaltPatternNode parseNode(String data){
        String[] nodeData = data.split("=");
        String label = null;
        String cond = null;
        
        if ( nodeData.length == 1 ){
        	if ( nodeData[0].startsWith("#") ){
        		label = nodeData[0];
        	}
        	else{
        		cond = nodeData[0];
        	}
        }
        else{
    		label = nodeData[0];
    		cond = nodeData[1];        	
        }
        
        if ( cond != null ){
        	String[] condData = cond.split(":");
        	String type = condData[0].trim(); 
        	if ( type.equals("pos") ){
        		return new MaltPatternNodePos(label, condData[1].trim());
        	}
        	else if ( type.equals("base") ){
        		return new MaltPatternNodeBase(label, condData[1].trim());
        	}
        	else if ( type.equals("name") ){
        		return new MaltPatternNodeAny("name");
        	}
        	else{
        		Logger.getLogger(this.getClass()).error("Nierozpoznany warunek: " + cond);
        		return null;
        	}        		
        }
        else{
        	return new MaltPatternNodeAny(label);
        }        
    }


    /**
     * 
     * @param data
     * @return
     */
    private MaltPatternEdge parseEdge(String data){
    	Matcher m = relFrom.matcher(data);
    	MaltPatternEdge edge = null;
    	if ( m.matches() ){
    		return new MaltEdgeRightArrow(m.group(1));
    	}
    	else{
    		m = relTo.matcher(data);
    		if ( m.matches() ){
    			return new MaltEdgeLeftArrow(m.group(1));
    		}
    		else{
    			Logger.getLogger(this.getClass()).warn("Niepoprawny opis krawędzi: " + data);
    		}
    	}
        return edge;
    }


    /**
     * 
     * @param sentence
     * @param tokenIdx
     * @param node
     * @return
     */
    public List<DependencyPath> match(MaltSentence sentence, int tokenIdx){
    	List<DependencyPath> paths = new ArrayList<DependencyPath>();
    	DependencyPath initPath = new DependencyPath();
    	initPath.addMatchedNode(this.nodes.get(0), tokenIdx);
    	paths.add(initPath);

    	for ( int i=0; i<this.nodes.size(); i++ ){
    		List<DependencyPath> newPaths = new ArrayList<DependencyPath>();
    		for ( DependencyPath path : paths){
    			if ( this.nodes.get(i).check(sentence, path.getLastIndex()) ){
    				newPaths.add(path);
    			}
    		}
    		paths = newPaths;
    		if ( paths.size() == 0 ){
    			return new ArrayList<DependencyPath>();
    		}

    		if ( i < this.edges.size() ){
    			newPaths = new ArrayList<DependencyPath>();
    			for ( DependencyPath path : paths ){
    				Set<Integer> indices = this.edges.get(i).findNodes(sentence, path.getLastIndex());
    				if ( indices.size() == 1 ){
    					path.addMatchedNode(this.nodes.get(i+1), indices.iterator().next());
    					newPaths.add(path);
    				}
    				else{
	    				for ( Integer index : indices ){
	    					DependencyPath clonedPath = path.clone();
	    					clonedPath.addMatchedNode(this.nodes.get(i+1), index);
	    					newPaths.add(clonedPath);
	    				}
    				}
    			}
    			paths = newPaths;
    		}
    		if ( paths.size() == 0 ){
    			return new ArrayList<DependencyPath>();
    		}
    	}
    	return paths; 
    }
   
    
}


