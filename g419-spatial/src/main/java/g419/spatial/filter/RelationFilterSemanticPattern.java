package g419.spatial.filter;

import g419.spatial.io.PlainSpatialPatternParser;
import g419.spatial.structure.SpatialRelation;
import g419.spatial.structure.SpatialRelationPattern;
import g419.spatial.structure.SpatialRelationPatternMatcher;
import g419.toolbox.sumo.NamToSumo;
import g419.toolbox.sumo.Sumo;
import g419.toolbox.sumo.WordnetToSumo;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.zip.DataFormatException;

public class RelationFilterSemanticPattern implements IRelationFilter {

	WordnetToSumo wts = null;
	Sumo sumo = new Sumo(false);
	SpatialRelationPatternMatcher patternMatcher = null;
	NamToSumo namToSumo = new NamToSumo();
		
	public RelationFilterSemanticPattern() throws IOException{

        try {
			this.wts = new WordnetToSumo();
	        this.patternMatcher = this.getPatternMatcher();
		} catch (DataFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
     
	}
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	private SpatialRelationPatternMatcher getPatternMatcher() throws IOException{
		String location = "/g419/spatial/resources/spatial_relation_patterns.txt";
		InputStream resource = this.getClass().getResourceAsStream(location);

        if (resource == null) {
            throw new MissingResourceException("Resource not found: " + location,
                    this.getClass().getName(), location);
        }
        
        PlainSpatialPatternParser parser = new PlainSpatialPatternParser(
        		new InputStreamReader( resource ), this.sumo);
        return parser.parse();        
	}
		
	@Override
	public boolean pass(SpatialRelation relation) {		
		List<SpatialRelationPattern> matching = this.match(relation);
		return matching.size() > 0;
	}
	
	public List<SpatialRelationPattern> match(SpatialRelation relation){
		String landmark = relation.getLandmark().getSentence().getTokens().get(relation.getLandmark().getHead()).getDisambTag().getBase();
		String trajector = relation.getTrajector().getSentence().getTokens().get(relation.getTrajector().getHead()).getDisambTag().getBase();
		Set<String> landmarkConcepts = this.wts.getConcept(landmark);
		Set<String> landmarkTypeConcepts = this.namToSumo.getConcept(relation.getLandmark().getType());
		Set<String> trajetorConcepts = this.wts.getConcept(trajector);
		Set<String> trajetorTypeConcepts = this.namToSumo.getConcept(relation.getTrajector().getType());
		
		if ( landmarkConcepts != null ){ 
			relation.getLandmarkConcepts().addAll(landmarkConcepts);
		}
		if ( landmarkTypeConcepts != null ){
			relation.getLandmarkConcepts().addAll(landmarkTypeConcepts);
		}
		
		if ( trajetorConcepts != null ){
			relation.getTrajectorConcepts().addAll(trajetorConcepts);
		}
		if ( trajetorTypeConcepts != null ){
			relation.getTrajectorConcepts().addAll(trajetorTypeConcepts);
		}
		
		List<SpatialRelationPattern> matching = this.patternMatcher.matchAll(relation);
				
		return matching;		
	}
	
}
