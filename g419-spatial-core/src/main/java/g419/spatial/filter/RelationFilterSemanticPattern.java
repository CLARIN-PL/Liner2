package g419.spatial.filter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.zip.DataFormatException;

import g419.corpus.structure.Tag;
import g419.spatial.io.CsvSpatialSchemeParser;
import g419.spatial.structure.SpatialExpression;
import g419.spatial.structure.SpatialRelationSchema;
import g419.spatial.structure.SpatialRelationSchemaMatcher;
import g419.toolbox.sumo.NamToSumo;
import g419.toolbox.sumo.Sumo;
import g419.toolbox.sumo.WordnetToSumo;

public class RelationFilterSemanticPattern implements IRelationFilter {

	WordnetToSumo wts = null;
	Sumo sumo = new Sumo(false);
	SpatialRelationSchemaMatcher patternMatcher = null;
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
	private SpatialRelationSchemaMatcher getPatternMatcher() throws IOException{
		String location = "/g419/spatial/resources/spatial_schemes.csv";
		InputStream resource = this.getClass().getResourceAsStream(location);
		boolean general = true;

        if (resource == null) {
            throw new MissingResourceException("Resource not found: " + location,
                    this.getClass().getName(), location);
        }
        
        return (new CsvSpatialSchemeParser(new InputStreamReader( resource ), new Sumo(false), general)).parse();
	}
		
	@Override
	public boolean pass(SpatialExpression relation) {		
		List<SpatialRelationSchema> matching = this.match(relation);
		return matching.size() > 0;
	}
	
	public List<SpatialRelationSchema> match(SpatialExpression relation){
		Set<String> landmarkConcepts = new HashSet<String>();
		for ( Tag tag : relation.getLandmark().getSentence().getTokens().get(relation.getLandmark().getHead()).getTags() ){
			Set<String> concepts = this.wts.getConcept(tag.getBase());
			if ( concepts != null ){
				landmarkConcepts.addAll( concepts );
			}
		}
		Set<String> landmarkTypeConcepts = this.namToSumo.getConcept(relation.getLandmark().getType());
		
		Set<String> trajetorConcepts = new HashSet<String>();
		for ( Tag tag : relation.getTrajector().getSentence().getTokens().get(relation.getTrajector().getHead()).getTags() ){
			Set<String> concepts = this.wts.getConcept(tag.getBase());
			if ( concepts != null ){
				trajetorConcepts.addAll( concepts );
			}
		}
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
		
		List<SpatialRelationSchema> matching = this.patternMatcher.matchAll(relation);
		relation.getSchemas().addAll(matching);
				
		return matching;		
	}
	
	public Sumo getSumo(){
		return this.sumo;
	}
}
