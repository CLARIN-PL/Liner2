package g419.tools.maltfeature;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.DataFormatException;

import org.maltparser.core.exception.MaltChainedException;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.liner2.core.tools.parser.MaltParser;
import g419.liner2.core.tools.parser.MaltSentence;
import g419.toolbox.sumo.Sumo;
import g419.toolbox.sumo.WordnetToSumo;
import g419.tools.utils.SparseMatrixCounter;

public class MaltFeatureGenerator {

    WordnetToSumo serdel = null;
    Sumo sumo = null;
    SparseMatrixCounter counter = null;
    boolean verbose = false;
    private MaltParser malt = null;
    List<MaltPattern> patterns = null;
    
		
	public MaltFeatureGenerator(String maltModelPath, List<MaltPattern> patterns) throws IOException, DataFormatException{
        this.serdel = new WordnetToSumo();
        this.sumo = new Sumo();
        this.counter = new SparseMatrixCounter();		
        this.malt = new MaltParser(maltModelPath);
        this.patterns = patterns;
	}
	
	public SparseMatrixCounter getMatrixCounter(){
		return this.counter;
	}
	
	public List<String> extractFeatures(Annotation ann, MaltSentence maltSent){
		List<String> features = new ArrayList<String>();
        for (MaltPattern pattern : this.patterns) {
        	List<DependencyPath> paths = pattern.match(maltSent, ann.getHead() );
        	for ( DependencyPath path : paths ){
        		features.add(this.pathToPattern(maltSent.getSentence(), pattern, path));
        		for  ( String patternStr : this.pathToPattern(maltSent.getSentence(), pattern, path, serdel, sumo) ){
        			features.add(patternStr);                    			
        		}
        	}                 
        }		
        return features;
	}
	
	public void process(Document document) throws MaltChainedException{
        for(Sentence sent: document.getSentences()){
            MaltSentence maltSent = new MaltSentence(sent, sent.getChunks());
            this.malt.parse(maltSent);
            //maltSent.wrapConjunctions();
            
            if ( maltSent.getAnnotations().size() > 0 && verbose ){
                for (String str : maltSent.getMaltData()){
                	System.out.println(str);
                }                	
            }
            
            for (Annotation ann : maltSent.getAnnotations()) {
        		for ( String feature : this.extractFeatures(ann, maltSent) ){
        			counter.addItem(feature, ann.getType());
        		}
            }
        }
	}

    /**
     * 
     * @param sentence
     * @param pattern
     * @param path
     * @return
     */
    public String pathToPattern(Sentence sentence, MaltPattern pattern, DependencyPath path){
    	StringBuilder sb = new StringBuilder();
    	for ( int i=0; i<pattern.getNodes().size(); i++ ){
    		MaltPatternNode node = pattern.getNodes().get(i);
    		if ( "name".equals(node.getLabel()) ){
    			sb.append("name");
    		}
    		else if ( node.getLabel() == null ) {
    			sb.append(node.toString());
    		}
    		else{
    			int index = path.getMatchedNodes().get(node);
    			sb.append("base:" + sentence.getTokens().get(index).getDisambTag().getBase());
    		}
    		if ( i < pattern.getEdges().size() ){
    			sb.append(pattern.getEdges().get(i));
    		}
    	}
    	return sb.toString();
    }

    /**
     * 
     * @param sentence
     * @param pattern
     * @param path
     * @return
     */
    @SuppressWarnings("serial")
	public List<String> pathToPattern(Sentence sentence, MaltPattern pattern, DependencyPath path, 
			WordnetToSumo serdel, Sumo sumo){
    	StringBuilder sb = new StringBuilder();
    	List<Set<String>> parts = new ArrayList<Set<String>>();
    	for ( int i=0; i<pattern.getNodes().size(); i++ ){
    		MaltPatternNode node = pattern.getNodes().get(i);
    		if ( "name".equals(node.getLabel()) ){
    			sb.append("name");
    			parts.add(new HashSet<String>(){{add("name");}});
    		}
    		else if ( node.getLabel() == null ) {
    			sb.append(node.toString());
    			parts.add(new HashSet<String>(){{add(node.toString());}});
    		}
    		else{
    			int index = path.getMatchedNodes().get(node);
    			String base = sentence.getTokens().get(index).getDisambTag().getBase();
    			Set<String> concepts = serdel.getLemmaConcepts(base);
    			if ( concepts != null ){
    				Set<String> elements = new HashSet<String>();
    				for ( String concept : concepts ){
    					elements.add("sumo:" + concept);
    					for ( String c : sumo.getSuperclasses(concept)){
    						elements.add("sumo:" + c);
    					}
    				}
    				parts.add(elements);
    				sb.append("sumo:" + concepts.iterator().next());
    			}
    			else{
    				sb.append("sumo:??" );    				
    				parts.add(new HashSet<String>(){{add("base:"+base);}});
    			}
    		}
    		if ( i < pattern.getEdges().size() ){
    			sb.append(pattern.getEdges().get(i));
    			Set<String> values = new HashSet<String>();
    			values.add(pattern.getEdges().get(i).toString());
    			parts.add(values);
    		}
    	}
    	return this.generateCombinations(parts);
    }

    /**
     * Generuje wszystkie możliwe kombinacje wartości dla sekwencji list
     * <code>
     *   [ ("mały "), ("biały ", "czarny "), ("kot", "pies") }
     * </code>
     * Wygeneruje następujące napisy:
     * <code>
     *   mały biały kot
     *   mały biały pies
     *   mały czarny kot
     *   mały czarny pies
     * </code>     
     * @param names
     * @param values
     * @return
     */
    private List<String> generateCombinations(List<Set<String>> parts){
    	@SuppressWarnings("serial")
		List<String> lastList = new ArrayList<String>(){{add("");}};
    	for ( Set<String> values : parts ){
    		List<String> newList = new ArrayList<String>();
    		for ( String str : lastList ){
    			for ( String value : values ){
    				newList.add(str + value);
    			}
    		}
    		lastList = newList;
    	}
    	return lastList;
    }    
}
