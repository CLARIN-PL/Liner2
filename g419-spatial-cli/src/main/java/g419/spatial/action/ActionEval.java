package g419.spatial.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.log4j.Logger;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Relation;
import g419.corpus.structure.Sentence;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.liner2.api.tools.FscoreEvaluator;
import g419.spatial.filter.IRelationFilter;
import g419.spatial.structure.SpatialRelation;
import g419.spatial.structure.SpatialRelationSchema;
import g419.spatial.tools.FscoreEvaluator2;
import g419.spatial.tools.SpatialRelationRecognizer;
import g419.spatial.tools.SpatialResources;

public class ActionEval extends Action {
	
	private final static String OPTION_FILENAME_LONG = "filename";
	private final static String OPTION_FILENAME = "f";
	
	private List<Pattern> annotationsPrep = new LinkedList<Pattern>();
	private List<Pattern> annotationsNg = new LinkedList<Pattern>();
	
	private String filename = null;
	private String inputFormat = null;
	
	private Set<String> objectPos = new HashSet<String>();
	
	/**
	 * 
	 */
	public ActionEval() {
		super("eval");
		this.setDescription("recognize spatial relations");
		this.options.addOption(this.getOptionInputFilename());		
		this.options.addOption(CommonOptions.getInputFileFormatOption());
		
		this.annotationsPrep.add(Pattern.compile("^PrepNG.*"));
		this.annotationsNg.add(Pattern.compile("^NG.*"));
		
		this.objectPos.add("subst");
		this.objectPos.add("ign");
		this.objectPos.add("brev");
	}
	
	/**
	 * Create Option object for input file name.
	 * @return Object for input file name parameter.
	 */
	private Option getOptionInputFilename(){
		return Option.builder(ActionEval.OPTION_FILENAME).hasArg().argName("FILENAME").required()
						.desc("path to the input file").longOpt(OPTION_FILENAME_LONG).build();			
	}

	/**
	 * Parse action options
	 * @param arg0 The array with command line parameters
	 */
	@Override
	public void parseOptions(String[] args) throws Exception {
        CommandLine line = new DefaultParser().parse(this.options, args);
        parseDefault(line);
        this.filename = line.getOptionValue(ActionEval.OPTION_FILENAME);
        this.inputFormat = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT);
    }

	@Override
	public void run() throws Exception {
		AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(this.filename, this.inputFormat);
		SpatialRelationRecognizer recognizer = new SpatialRelationRecognizer();
		Set<String> regions = SpatialResources.getRegions();
		
		FscoreEvaluator2 evalTotal = new FscoreEvaluator2();
		FscoreEvaluator2 evalNoSeedTotal = new FscoreEvaluator2();
		Map<String, FscoreEvaluator> evalByTypeTotal = new HashMap<String, FscoreEvaluator>();
				
		Document document = null;
		while ( ( document = reader.nextDocument() ) != null ){					
			System.out.println("=======================================");
			System.out.println("Document: " + document.getName());
			System.out.println("=======================================");
			
			List<SpatialRelation> gold = this.getSpatialRelations(document);
			
			if ( gold.size() == 0 ){
				continue;
			}
						
			for ( SpatialRelation relation : gold ){
				evalTotal.addGold(relation);
				evalNoSeedTotal.addGold(relation);
			}
			
			for (Paragraph paragraph : document.getParagraphs()){
				for (Sentence sentence : paragraph.getSentences()){
					
					List<SpatialRelation> relations = recognizer.findCandidates(sentence);
					
					if ( relations.size() > 0 ){
						System.out.println();
						System.out.println("Sentence: " + sentence);
						System.out.println("-----------------------------------------------------------------------------");
						System.out.println();
						
						for ( SpatialRelation relation : gold ){
							if ( relation.getLandmark().getSentence() == sentence) {
								System.out.println(relation.toString() + " " + relation.getKey());
							}
						}
						System.out.println();
						
						for ( SpatialRelation rel : relations ){
							String status = "OK    ";
							String filterName = "";
							String eval = "";
							String duplicate = "";
							
							for ( IRelationFilter filter : recognizer.getFilters() ){
								if ( !filter.pass(rel) ){
									status = "REMOVE";
									filterName = filter.getClass().getSimpleName();
									break;
								}								
							}

							if ( evalTotal.containsAsDecision(rel) ){
								duplicate = "-DUPLICATE";
							}
							else{
								duplicate = "-FIRST";
							}

							evalNoSeedTotal.addDecision(rel);
							if ( status.equals("REMOVE") ){
								if ( evalTotal.containsAsGold(rel) ){
									eval = "FalseNegative";
								}
							}
							else{
								FscoreEvaluator evalType = evalByTypeTotal.get(rel.getType());
								if ( evalType == null ){
									evalType = new FscoreEvaluator();
									evalByTypeTotal.put(rel.getType(), evalType);
								}
								
								if ( evalTotal.containsAsGold(rel) ){
									evalType.addTruePositive();
									eval = "TruePositive";
								}
								else{
									evalType.addFalsePositive();
									eval = "FalsePositive";
								}
								evalTotal.addDecision(rel);
							}
							
							eval += duplicate;
							
							StringBuilder sb = new StringBuilder();
							for ( SpatialRelationSchema p : recognizer.getSemanticFilter().match(rel) ){
								if ( sb.length() > 0 ){
									sb.append(" & ");
								}
								
								sb.append(p.getName());
								sb.append(" (TR subclass of:");								
								for ( String concept : p.getTrajectorConcepts() ){
									if ( recognizer.getSemanticFilter().getSumo().isClassOrSubclassOf(rel.getTrajectorConcepts(), concept)){
										sb.append(" " + concept);
									}
								}

								sb.append("; LM subclass of:");
								for ( String concept : p.getLandmarkConcepts() ){
									if ( recognizer.getSemanticFilter().getSumo().isClassOrSubclassOf(rel.getLandmarkConcepts(), concept)){
										sb.append(" " + concept);
									}
								}
								sb.append(")");
								
							}
							
							String info = String.format("  - %s\t %-80s\t%s %s; id=%s; schema=%s",status, rel.toString(), filterName, eval, rel.getKey(), sb.toString());
							if ( regions.contains(rel.getLandmark().getHeadToken().getDisambTag().getBase()) ){
								info += " REGION_AS_LANDMARK";
							}
							System.out.println(info);
							
							System.out.println("\t\t\tTrajector = " + rel.getTrajector() + " => " + String.join(", ", rel.getTrajectorConcepts()));
							System.out.println("\t\t\tLandmark  = " + rel.getLandmark() + " => " + String.join(", ", rel.getLandmarkConcepts()));
							System.out.println();
						}
							
					}
				}
			}		
		}
			
		reader.close();
		System.out.println();
		System.out.println("=============================");
		System.out.println("Z sitem semantycznym");
		System.out.println("=============================");
		evalTotal.evaluate();
		System.out.println("-----------------------------");
		
		for ( String type : evalByTypeTotal.keySet() ){
			FscoreEvaluator evalType = evalByTypeTotal.get(type);
			System.out.println(String.format("%-20s P=%5.2f TP=%d FP=%d", type, evalType.precision()*100, evalType.getTruePositiveCount(), evalType.getFalsePositiveCount()));
		}
		
		System.out.println();
		System.out.println("=============================");
		System.out.println("Bez sita semantycznego");
		System.out.println("=============================");
		evalNoSeedTotal.evaluate();
	}
	
		
	/**
	 * 
	 * @param document
	 * @return
	 */
	private List<SpatialRelation> getSpatialRelations(Document document){
		
		List<SpatialRelation> srs = new ArrayList<SpatialRelation>();
		Map<Annotation, Annotation> landmarks = new HashMap<Annotation, Annotation>();
		Map<Annotation, List<Annotation>> trajectors = new HashMap<Annotation, List<Annotation>>();
		Map<Annotation, Annotation> regions = new HashMap<Annotation, Annotation>();
		for ( Relation r : document.getRelations().getRelations() ){
			if ( r.getType().equals("landmark") ){
				landmarks.put(r.getAnnotationFrom(), r.getAnnotationTo() );			
			}			
			else if ( r.getType().equals("trajector") ){
				List<Annotation> annotations = trajectors.get(r.getAnnotationFrom());
				if ( annotations == null ){
					annotations = new ArrayList<Annotation>();
					trajectors.put(r.getAnnotationFrom(), annotations);
				}
				annotations.add( r.getAnnotationTo() );				
			}
			else if ( r.getType().equals("other") && r.getAnnotationTo().getType().equals("region") ){
				regions.put(r.getAnnotationFrom(), r.getAnnotationTo() );
			}
		}
		Set<Annotation> allIndicators = new HashSet<Annotation>();
		allIndicators.addAll(landmarks.keySet());
		allIndicators.addAll(trajectors.keySet());
		
		for ( Annotation indicator : allIndicators ){
			Annotation landmark = landmarks.get(indicator);
			List<Annotation> trajector = trajectors.get(indicator);
			Annotation region = regions.get(indicator);
			//if ( (landmark != null || region != null) && trajector != null ){
			if ( landmark != null && trajector != null ){
//				if ( landmark == null || (landmark != null && region != null && region.getBegin() < landmark.getBegin() ) ){
//					landmark = region;
//				}
				for ( Annotation tr : trajector ){
					// Zignoruje relacje, w któryj trajector lub landmark nie są substem lub ignem
					if ( objectPos.contains(tr.getHeadToken().getDisambTag().getPos()) 
							&& objectPos.contains(landmark.getHeadToken().getDisambTag().getPos()) ){					
						srs.add(new SpatialRelation("Gold", tr, indicator, landmark));
					}
				}
			}
			else{
				if ( landmark == null ){
					Logger.getLogger(this.getClass()).warn(String.format("Missing landmark for spatial indicator %s", indicator.toString()));
				}
				if ( trajector == null ){
					Logger.getLogger(this.getClass()).warn(String.format("Missing trajector for spatial indicator %s", indicator.toString()));
				}
				
			}
		}
		return srs;
	}
}
