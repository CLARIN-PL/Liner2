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
import g419.corpus.structure.Token;
import g419.lib.cli.CommonOptions;
import g419.lib.cli.action.Action;
import g419.liner2.api.Liner2;
import g419.liner2.api.chunker.IobberChunker;
import g419.liner2.api.features.tokens.ClassFeature;
import g419.liner2.api.tools.FscoreEvaluator;
import g419.liner2.api.tools.parser.MaltParser;
import g419.liner2.api.tools.parser.MaltSentence;
import g419.liner2.api.tools.parser.MaltSentenceLink;
import g419.spatial.filter.IRelationFilter;
import g419.spatial.filter.RelationFilterPronoun;
import g419.spatial.filter.RelationFilterSemanticPattern;
import g419.spatial.filter.RelationFilterSpatialIndicator;
import g419.spatial.structure.SpatialRelation;
import g419.spatial.structure.SpatialRelationPattern;
import g419.spatial.tools.FscoreEvaluator2;

public class ActionSpatial extends Action {
	
	private final static String OPTION_FILENAME_LONG = "filename";
	private final static String OPTION_FILENAME = "f";
	
	private List<Pattern> annotationsPrep = new LinkedList<Pattern>();
	private List<Pattern> annotationsNg = new LinkedList<Pattern>();
	
	private Pattern patternAnnotationNam = Pattern.compile("^nam(_.*|$)");
	
	private String filename = null;
	private String inputFormat = null;
	
	private Logger logger = Logger.getLogger("ActionSpatial");
	
	/* Parametry, które będzie trzeba wyciągnąć do pliku ini. */
	private String config_liner2_model = "/home/czuk/nlp/eclipse/workspace_liner2/models-released/liner2.5/liner25-model-pack-ibl/config-n82.ini";
	private String config_iobber_model = "model-kpwr11-H";
	private String config_iobber_config = "kpwr.ini";

	public ActionSpatial() {
		super("spatial");
		this.setDescription("recognize spatial relations");
		this.options.addOption(this.getOptionInputFilename());		
		this.options.addOption(CommonOptions.getInputFileFormatOption());
		
		this.annotationsPrep.add(Pattern.compile("^PrepNG.*"));
		this.annotationsNg.add(Pattern.compile("^NG.*"));
	}
	
	/**
	 * Create Option object for input file name.
	 * @return Object for input file name parameter.
	 */
	private Option getOptionInputFilename(){
		return Option.builder(ActionSpatial.OPTION_FILENAME).hasArg().argName("FILENAME").required()
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
        this.filename = line.getOptionValue(ActionSpatial.OPTION_FILENAME);
        this.inputFormat = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT);
    }

	@Override
	public void run() throws Exception {
		AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(this.filename, this.inputFormat);
		Document document = null;
		
		RelationFilterSemanticPattern semanticFilter = new RelationFilterSemanticPattern(); 
		
		List<IRelationFilter> filters = new LinkedList<IRelationFilter>();
		//filters.add(new RelationFilterSpatialIndicator());
		filters.add(new RelationFilterPronoun());
		filters.add(semanticFilter);
		
		IobberChunker iobber = new IobberChunker("", this.config_iobber_model, this.config_iobber_config);		
		Liner2 liner2 = new Liner2(this.config_liner2_model);
		
		MaltParser malt = new MaltParser("/nlp/resources/maltparser/skladnica_liblinear_stackeager_final.mco");
		FscoreEvaluator2 evalTotal = new FscoreEvaluator2();
		FscoreEvaluator2 evalNoSeedTotal = new FscoreEvaluator2();
		Map<String, FscoreEvaluator> evalByTypeTotal = new HashMap<String, FscoreEvaluator>();
				
		while ( ( document = reader.nextDocument() ) != null ){					
			System.out.println("=======================================");
			//Logger.getLogger(this.getClass()).info("\nDocument: " + document.getName());
			System.out.println("Document: " + document.getName());
			System.out.println("=======================================");
			
			List<SpatialRelation> gold = this.getSpatialRelations(document);
			
			if ( gold.size() == 0 ){
				continue;
			}
						
			//liner2.chunkInPlace(document);
			//iobber.chunkInPlace(document);			
						
			for ( SpatialRelation relation : gold ){
				evalTotal.addGold(relation);
				evalNoSeedTotal.addGold(relation);
			}
			
			for (Paragraph paragraph : document.getParagraphs()){
				for (Sentence sentence : paragraph.getSentences()){
					
					this.splitPrepNg(sentence);
					
					MaltSentence maltSentence = new MaltSentence(sentence);
					malt.parse(maltSentence);

					/* Zaindeksuj frazy np */
					Map<Integer, Annotation> chunkNpTokens = new HashMap<Integer, Annotation>();
					Map<Integer, Annotation> chunkVerbfinTokens = new HashMap<Integer, Annotation>();
					Map<Integer, Annotation> chunkPrepTokens = new HashMap<Integer, Annotation>();
					Map<Integer, Annotation> chunkPpasTokens = new HashMap<Integer, Annotation>();
					Map<Integer, Annotation> chunkPactTokens = new HashMap<Integer, Annotation>();
					for ( Annotation an : sentence.getChunks() ){
						if ( an.getType().equals("chunk_np") ){
							for ( Integer n = an.getBegin(); n <= an.getEnd(); n++){
								chunkNpTokens.put(n, an);
							}
						}
						else if ( an.getType().equals("Prep") ){
							for ( Integer n = an.getBegin(); n <= an.getEnd(); n++){
								chunkPrepTokens.put(n, an);					
							}
						}
						else if ( an.getType().equals("Pact") ){
							for ( Integer n = an.getBegin(); n <= an.getEnd(); n++){
								chunkPactTokens.put(n, an);					
							}
						}
						else if ( an.getType().equals("Ppas") ){
							for ( Integer n = an.getBegin(); n <= an.getEnd(); n++){
								chunkPpasTokens.put(n, an);					
							}
						}
						else if ( an.getType().equals("Verbfin") ){
							for ( Integer n = an.getBegin(); n <= an.getEnd(); n++){
								chunkVerbfinTokens.put(n, an);					
							}
						}
					}
					
					/* Zaindeksuj pierwsze tokeny anotacji NG* */
					Map<Integer, List<Annotation>> mapTokenIdToAnnotations = new HashMap<Integer, List<Annotation>>();
					for ( Annotation an : sentence.getAnnotations(this.annotationsNg) ){
						for ( Integer n = an.getBegin(); n <= an.getEnd(); n++){
							if ( !mapTokenIdToAnnotations.containsKey(n) ){
								mapTokenIdToAnnotations.put(n, new LinkedList<Annotation>());
							}
							mapTokenIdToAnnotations.get(n).add(an);
						}
					}
					
					
					List<SpatialRelation> relations = new LinkedList<SpatialRelation>();

					// Second Iteration only
					
					relations.addAll( this.findCandidatesFirstNgAnyPrepNg(sentence, mapTokenIdToAnnotations, chunkNpTokens, chunkPrepTokens) );
					relations.addAll( this.findCandidatesByMalt(sentence, maltSentence));
					

					relations.addAll( this.findCandidatesNgPpasPrepNg(
							sentence, mapTokenIdToAnnotations, chunkNpTokens, chunkPpasTokens, chunkPrepTokens));
					relations.addAll( this.findCandidatesNgPactPrepNg(
							sentence, mapTokenIdToAnnotations, chunkNpTokens, chunkPactTokens, chunkPrepTokens));
					/*
					relations.addAll( this.findCandidatesNgPrepNgPpasPrepNg(
							sentence, mapTokenIdToAnnotations, chunkVerbfinTokens, chunkPrepTokens, chunkPpasTokens) );
					relations.addAll( this.findCandidatesNgPrepNgCommaPrepNg(
							sentence, mapTokenIdToAnnotations, chunkNpTokens, chunkPrepTokens) );
					relations.addAll( this.findCandidatesNgPrepNgPrepNg(
							sentence, mapTokenIdToAnnotations, chunkNpTokens, chunkPrepTokens) );
					relations.addAll( this.findCandidatesNgNgPrepNg(
							sentence, mapTokenIdToAnnotations, chunkNpTokens, chunkPrepTokens) );

					relations.addAll( this.findCandidatesNgPrepNgNoNp(
							sentence, mapTokenIdToAnnotations, chunkNpTokens, chunkPrepTokens) );
					relations.addAll( this.findCandidatesNgPrepNgDiffNp(
							sentence, mapTokenIdToAnnotations, chunkNpTokens, chunkPrepTokens) );
					relations.addAll( this.findCandidatesPrepNgNgDiffNp(
							sentence, mapTokenIdToAnnotations, chunkNpTokens, chunkPrepTokens) );
					relations.addAll( this.findCandidatesNgAnyPrepNg(
							sentence, mapTokenIdToAnnotations, chunkNpTokens, chunkPrepTokens) );
					relations.addAll( this.findCandidatesPrepNgNg(
							sentence, mapTokenIdToAnnotations, chunkNpTokens, chunkPrepTokens) );
					relations.addAll( this.findCandidatesNgPrepNg(
							sentence, mapTokenIdToAnnotations, chunkNpTokens, chunkPrepTokens) );

					relations.addAll( this.findCandidatesNgVerbfinPrepNg(
							sentence, mapTokenIdToAnnotations, chunkVerbfinTokens, chunkPrepTokens) );
					relations.addAll( this.findCandidatesPrepNgVerbfinNg(
							sentence, mapTokenIdToAnnotations, chunkVerbfinTokens, chunkPrepTokens) );
					*/


					this.replaceNgWithNames(sentence, relations);
					
					if ( relations.size() > 0 ){
						System.out.println();
						System.out.println(sentence);
						System.out.println();
						
						for ( SpatialRelation relation : gold ){
							if ( relation.getLandmark().getSentence() == sentence) {
								System.out.println(relation.toString() + " " + relation.getKey());
							}
						}
						
						for ( SpatialRelation rel : relations ){
							String status = "OK    ";
							String filterName = "";
							String eval = "";
							String duplicate = "";
							
							for ( IRelationFilter filter : filters ){
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
							for ( SpatialRelationPattern p : semanticFilter.match(rel) ){
								if ( sb.length() > 0 ){
									sb.append(" & ");
								}
								sb.append(p.toString());
							}
							
							System.out.println(String.format("  - %s\t %-80s\t%s %s %s %s",status, rel.toString(), filterName, eval, rel.getKey(), sb.toString()));
							
							System.out.println("\t\t\tTrajector = " + rel.getTrajector() + " => " + String.join(", ", rel.getTrajectorConcepts()));
							System.out.println("\t\t\tLandmark  = " + rel.getLandmark() + " => " + String.join(", ", rel.getLandmarkConcepts()));
							
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
	 * Dla fraz NG, które pokrywają się z nazwą własną, zmienia NG na nazwę.
	 * @param sentence
	 * @param relations
	 */
	private void replaceNgWithNames(Sentence sentence, List<SpatialRelation> relations){
		Map<String, Annotation> names = new HashMap<String, Annotation>();
		for ( Annotation an : sentence.getAnnotations(this.patternAnnotationNam) ){
			String key = String.format("%d:%d", an.getBegin(), an.getEnd());
			if ( names.containsKey(key) ){
				Logger.getLogger(this.getClass()).warn(String.format("Name for key '%s' already exists: %s", key, an));				
			}
			else{
				names.put(key, an);
			}
		}
		for ( SpatialRelation relation : relations ){
			// Sprawdź landmark
			String landmarkKey = String.format("%d:%d",relation.getLandmark().getBegin(), relation.getLandmark().getEnd());
			Annotation landmarkName = names.get(landmarkKey);
			if ( landmarkName != null ){
				Logger.getLogger(this.getClass()).info(String.format("Replace %s (%s) with nam (%s)", relation.getLandmark().getType(), relation.getLandmark(), landmarkName));
				landmarkName.setHead(relation.getLandmark().getHead());
				relation.setLandmark(landmarkName);
			}
			// Sprawdź trajector
			String trajectorKey = String.format("%d:%d",relation.getTrajector().getBegin(), relation.getTrajector().getEnd());
			Annotation trajectorName = names.get(trajectorKey);
			if ( trajectorName != null ){
				Logger.getLogger(this.getClass()).info(String.format("Replace %s (%s) with nam (%s)", relation.getTrajector().getType(), relation.getTrajector(), trajectorName));
				relation.setTrajector(trajectorName);
			}
		}
	}
	
	/**
	 * Wydziela z anotacji PrepNG* anotacje zagnieżdżone poprzez odcięcie przymika.
	 * @param sentence
	 */
	public void splitPrepNg(Sentence sentence){
		/* Zaindeksuj pierwsze tokeny anotacji NG* */
		Map<Integer, List<Annotation>> mapTokenIdToAnnotations = new HashMap<Integer, List<Annotation>>();
		for ( Annotation an : sentence.getAnnotations(this.annotationsNg) ){
			if ( !mapTokenIdToAnnotations.containsKey(an.getBegin()) ){
				mapTokenIdToAnnotations.put(an.getBegin(), new LinkedList<Annotation>());
			}
			mapTokenIdToAnnotations.get(an.getBegin()).add(an);
		}
		
		for ( Annotation an : sentence.getAnnotations(this.annotationsPrep) ){
			if ( !mapTokenIdToAnnotations.containsKey(an.getBegin()+1) ){
				Annotation ani = new Annotation(an.getBegin()+1, an.getEnd(), an.getType().substring(4), an.getSentence());
				ani.setHead(an.getHead());
				sentence.addChunk(ani);
			}
		}
	}

	/**
	 * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
	 * @param sentence
	 */
	public List<SpatialRelation> findCandidatesNgAnyPrepNg(Sentence sentence, 
			Map<Integer, List<Annotation>> mapTokenIdToAnnotations, 
			Map<Integer, Annotation> chunkNpTokens, 
			Map<Integer, Annotation> chunkPrepTokens){				
		List<SpatialRelation> relations = new LinkedList<SpatialRelation>();
		/* Szukaj wzorców NG* prep NG* */
		for ( Annotation an : sentence.getAnnotations(this.annotationsPrep) ){						
			Annotation preposition = chunkPrepTokens.get(an.getBegin());
			if ( preposition == null ){
				this.logger.warn("Prep annotation for PrepNG not found: " + an.toString());
				continue;
			}
						
			Integer landmarkId = an.getBegin()+preposition.getTokens().size();
			Integer trajectorPrevId = an.getBegin()-1;
			
			boolean breakSearchPrev = false;
			while ( !breakSearchPrev && trajectorPrevId >=0 && mapTokenIdToAnnotations.get(trajectorPrevId) == null ){
				/* Przecinek i nawias zamykający przerywają poszykiwanie */
				String orth = sentence.getTokens().get(trajectorPrevId).getOrth(); 
				if ( orth.equals(",") || orth.equals(")") ){
					breakSearchPrev = true;
				}
				trajectorPrevId--;
			}
			Integer trajectorId = trajectorPrevId;
						
			if ( chunkNpTokens.get(landmarkId) != null && !breakSearchPrev && chunkNpTokens.get(trajectorPrevId) == chunkNpTokens.get(landmarkId) ){
				String type = "";
				if ( trajectorPrevId+1 == preposition.getBegin() ){
					type = "<NG|PrepNG>";
				}
				else{
					type = "<NG|...|PrepNG>";						
				}
				List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
				List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
				relations.addAll(this.generateAllCombinations(type, trajectors, landmarks, preposition));
			}
		}
		return relations;
	}
	
	/**
	 * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
	 * @param sentence
	 */
	public List<SpatialRelation> findCandidatesNgPrepNgNoNp(Sentence sentence, 
			Map<Integer, List<Annotation>> mapTokenIdToAnnotations, 
			Map<Integer, Annotation> chunkNpTokens, 
			Map<Integer, Annotation> chunkPrepTokens){				
		List<SpatialRelation> relations = new LinkedList<SpatialRelation>();
		/* Szukaj wzorców NG* prep NG* */
		for ( Annotation an : sentence.getAnnotations(this.annotationsPrep) ){						
			Annotation preposition = chunkPrepTokens.get(an.getBegin());
			if ( preposition == null ){
				this.logger.warn("Prep annotation for PrepNG not found: " + an.toString());
				continue;
			}
						
			Integer landmarkId = an.getBegin()+preposition.getTokens().size();
			Integer trajectorId = an.getBegin()-1;
						
			if ( chunkNpTokens.get(landmarkId) == null
					&& chunkNpTokens.get(trajectorId) == null ){
				String type = "NG|PrepNG";
				List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
				List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
				if ( trajectors != null ){
					relations.addAll(this.generateAllCombinations(type, trajectors, landmarks, preposition));
				}
			}
		}
		return relations;
	}	

	/**
	 * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
	 * @param sentence
	 */
	public List<SpatialRelation> findCandidatesNgPrepNgDiffNp(Sentence sentence, 
			Map<Integer, List<Annotation>> mapTokenIdToAnnotations, 
			Map<Integer, Annotation> chunkNpTokens, 
			Map<Integer, Annotation> chunkPrepTokens){				
		List<SpatialRelation> relations = new LinkedList<SpatialRelation>();
		/* Szukaj wzorców NG* prep NG* */
		for ( Annotation an : sentence.getAnnotations(this.annotationsPrep) ){						
			Annotation preposition = chunkPrepTokens.get(an.getBegin());
			if ( preposition == null ){
				this.logger.warn("Prep annotation for PrepNG not found: " + an.toString());
				continue;
			}
						
			Integer landmarkId = an.getBegin()+preposition.getTokens().size();
			Integer trajectorId = an.getBegin()-1;
						
			if ( chunkNpTokens.get(landmarkId) != null
					&& chunkNpTokens.get(trajectorId) != null
					&& chunkNpTokens.get(trajectorId) != chunkNpTokens.get(landmarkId) ){
				String type = "<NG><PrepNG>";
				List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
				List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
				if ( trajectors != null ){
					relations.addAll(this.generateAllCombinations(type, trajectors, landmarks, preposition));
				}
			}
		}
		return relations;
	}

	/**
	 * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
	 * @param sentence
	 */
	public List<SpatialRelation> findCandidatesPrepNgNgDiffNp(Sentence sentence, 
			Map<Integer, List<Annotation>> mapTokenIdToAnnotations, 
			Map<Integer, Annotation> chunkNpTokens, 
			Map<Integer, Annotation> chunkPrepTokens){				
		List<SpatialRelation> relations = new LinkedList<SpatialRelation>();
		/* Szukaj wzorców NG* prep NG* */
		for ( Annotation an : sentence.getAnnotations(this.annotationsPrep) ){						
			Annotation preposition = chunkPrepTokens.get(an.getBegin());
			if ( preposition == null ){
				this.logger.warn("Prep annotation for PrepNG not found: " + an.toString());
				continue;
			}
						
			Integer landmarkId = an.getBegin() + preposition.getTokens().size();
			Integer trajectorId = an.getEnd() + 1;
						
			if ( chunkNpTokens.get(landmarkId) != null
					&& chunkNpTokens.get(trajectorId) != null
					&& chunkNpTokens.get(trajectorId) != chunkNpTokens.get(landmarkId) ){
				String type = "<PrepNG><NG>";
				List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
				List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
				relations.addAll(this.generateAllCombinations(type, trajectors, landmarks, preposition));
			}
		}
		return relations;
	}

	/**
	 * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
	 * @param sentence
	 */
	public List<SpatialRelation> findCandidatesNgPrepNgPpasPrepNg(Sentence sentence, 
			Map<Integer, List<Annotation>> mapTokenIdToAnnotations, 
			Map<Integer, Annotation> chunkNpTokens, 
			Map<Integer, Annotation> chunkPrepTokens,
			Map<Integer, Annotation> chunkPpasTokens){				
		List<SpatialRelation> relations = new LinkedList<SpatialRelation>();
		/* Szukaj wzorców NG* prep NG* */
		for ( Annotation an : sentence.getAnnotations(this.annotationsPrep) ){						
			Annotation preposition = chunkPrepTokens.get(an.getBegin());
			if ( preposition == null ){
				this.logger.warn("Prep annotation for PrepNG not found: " + an.toString());
				continue;
			}
						
			Integer landmarkId = an.getBegin()+preposition.getTokens().size();
			if ( an.getBegin()-1 <= 0 || !sentence.getTokens().get(an.getBegin()-1).getOrth().equals(",") ){
				continue;
			}
			
			Annotation ppas = chunkPpasTokens.get(an.getBegin()-1);
			
			if ( ppas == null ){
				continue;
			}
			
			List<Annotation> ngs = mapTokenIdToAnnotations.get(ppas.getBegin()-1);
			if ( ngs == null ){
				continue;
			}
			
			Annotation prep = chunkPrepTokens.get(ngs.get(0).getBegin()-1);
			if ( prep == null ){
				continue;
			}
			
			Integer trajectorId = prep.getBegin()-1;
						
			if ( chunkNpTokens.get(landmarkId) != null && chunkNpTokens.get(trajectorId) == chunkNpTokens.get(landmarkId) ){
				String type = "<NG|PrepNG|Ppas|PrepNG>";
				List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
				List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
				if ( trajectors != null ){
					relations.addAll(this.generateAllCombinations(type, trajectors, landmarks, preposition));
				}
			}
		}
		return relations;
	}	

	/**
	 * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
	 * @param sentence
	 */
	public List<SpatialRelation> findCandidatesNgPrepNgCommaPrepNg(Sentence sentence, 
			Map<Integer, List<Annotation>> mapTokenIdToAnnotations, 
			Map<Integer, Annotation> chunkNpTokens, 
			Map<Integer, Annotation> chunkPrepTokens){				
		List<SpatialRelation> relations = new LinkedList<SpatialRelation>();
		/* Szukaj wzorców NG* prep NG* */
		for ( Annotation an : sentence.getAnnotations(this.annotationsPrep) ){						
			Annotation preposition = chunkPrepTokens.get(an.getBegin());
			if ( preposition == null ){
				this.logger.warn("Prep annotation for PrepNG not found: " + an.toString());
				continue;
			}
						
			Integer landmarkId = an.getBegin()+preposition.getTokens().size();
			Integer commaId = preposition.getBegin()-1;
			
			if ( commaId <= 0 || !sentence.getTokens().get(commaId).getOrth().equals(",") ){
				continue;
			}			
						
			Integer prepId = null;
			List<Annotation> ngs = mapTokenIdToAnnotations.get(commaId-1);
			if ( ngs == null ){
				continue;
			}
			else{
				for ( Annotation a : ngs ){
					if ( prepId == null ){
						prepId = a.getBegin()-1;
					}
					else{
						prepId = Math.min(prepId, a.getBegin()-1);
					}
				}
			}
			
			Annotation prep = chunkPrepTokens.get(prepId);
			if ( prep == null ){
				continue;
			}
			
			Integer trajectorId = prep.getBegin()-1;
						
			if ( chunkNpTokens.get(landmarkId) != null 
					&& chunkNpTokens.get(trajectorId) == chunkNpTokens.get(landmarkId) ){
				String type = "<NG|PrepNG|Comma|PrepNG>";
				List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
				List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
				if ( trajectors != null ){
					relations.addAll(this.generateAllCombinations(type, trajectors, landmarks, preposition));
				}
			}
		}
		return relations;
	}	
	
	/**
	 * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
	 * @param sentence
	 */
	public List<SpatialRelation> findCandidatesNgPrepNgPrepNg(Sentence sentence, 
			Map<Integer, List<Annotation>> mapTokenIdToAnnotations, 
			Map<Integer, Annotation> chunkNpTokens, 
			Map<Integer, Annotation> chunkPrepTokens){				
		List<SpatialRelation> relations = new LinkedList<SpatialRelation>();
		/* Szukaj wzorców NG* prep NG* */
		for ( Annotation an : sentence.getAnnotations(this.annotationsPrep) ){						
			Annotation preposition = chunkPrepTokens.get(an.getBegin());
			if ( preposition == null ){
				this.logger.warn("Prep annotation for PrepNG not found: " + an.toString());
				continue;
			}
						
			Integer landmarkId = an.getBegin()+preposition.getTokens().size();

			List<Annotation> ngs = mapTokenIdToAnnotations.get(an.getBegin()-1);
			Integer prepId = null;
			if ( ngs == null ){
				continue;
			}
			else{
				for ( Annotation a : ngs ){
					if ( prepId == null ){
						prepId = a.getBegin()-1;
					}
					else{
						prepId = Math.min(prepId, a.getBegin()-1);
					}
				}
			}
			
			Annotation prep = chunkPrepTokens.get(prepId);
			if ( prep == null ){
				continue;
			}
			
			Integer trajectorId = prep.getBegin()-1;
						
			if ( chunkNpTokens.get(landmarkId) != null 
					&& chunkNpTokens.get(trajectorId) == chunkNpTokens.get(landmarkId) ){
				String type = "<NG|PrepNG|PrepNG>";
				List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
				List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
				relations.addAll(this.generateAllCombinations(type, trajectors, landmarks, preposition));
			}
		}
		return relations;
	}			
	
	/**
	 * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
	 * @param sentence
	 */
	public List<SpatialRelation> findCandidatesNgNgPrepNg(Sentence sentence, 
			Map<Integer, List<Annotation>> mapTokenIdToAnnotations, 
			Map<Integer, Annotation> chunkNpTokens, 
			Map<Integer, Annotation> chunkPrepTokens){				
		List<SpatialRelation> relations = new LinkedList<SpatialRelation>();
		/* Szukaj wzorców NG* prep NG* */
		for ( Annotation an : sentence.getAnnotations(this.annotationsPrep) ){						
			Annotation preposition = chunkPrepTokens.get(an.getBegin());
			if ( preposition == null ){
				this.logger.warn("Prep annotation for PrepNG not found: " + an.toString());
				continue;
			}
						
			Integer landmarkId = an.getBegin()+preposition.getTokens().size();
						
			List<Annotation> ngs = mapTokenIdToAnnotations.get(preposition.getBegin()-1);
			Integer trajectorId = null;
			if ( ngs == null ){
				continue;
			}
			else{
				for ( Annotation a : ngs ){
					if ( trajectorId == null ){
						trajectorId = a.getBegin()-1;
					}
					else{
						trajectorId = Math.min(trajectorId, a.getBegin()-1);
					}
				}
			}
			
			if ( chunkNpTokens.get(landmarkId) != null && chunkNpTokens.get(trajectorId) == chunkNpTokens.get(landmarkId) ){
				String type = "<NG|NG|PrepNG>";
				List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
				List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
				if ( trajectors != null ){
					relations.addAll(this.generateAllCombinations(type, trajectors, landmarks, preposition));
				}
			}
		}
		return relations;
	}			

	/**
	 * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
	 * @param sentence
	 */
	public List<SpatialRelation> findCandidatesFirstNgAnyPrepNg(Sentence sentence, 
			Map<Integer, List<Annotation>> mapTokenIdToAnnotations, 
			Map<Integer, Annotation> chunkNpTokens, 
			Map<Integer, Annotation> chunkPrepTokens){				
		List<SpatialRelation> relations = new LinkedList<SpatialRelation>();
		/* Szukaj wzorców NG* prep NG* */
		for ( Annotation an : sentence.getAnnotations(this.annotationsPrep) ){						
			Annotation preposition = chunkPrepTokens.get(an.getBegin());
			if ( preposition == null ){
				this.logger.warn("Prep annotation for PrepNG not found: " + an.toString());
				continue;
			}
						
			Integer landmarkId = an.getBegin()+preposition.getTokens().size();
			Integer trajectorId = an.getBegin()-1;
			
			boolean breakSearchPrev = false;
			while ( !breakSearchPrev && trajectorId >=0 && mapTokenIdToAnnotations.get(trajectorId) == null ){
				/* Przecinek i nawias zamykający przerywają poszykiwanie */
				String orth = sentence.getTokens().get(trajectorId).getOrth(); 
				if ( orth.equals(",") || orth.equals(")") ){
					breakSearchPrev = true;
				}
				trajectorId--;
			}			
						
			if ( chunkNpTokens.get(landmarkId) != null && !breakSearchPrev && chunkNpTokens.get(trajectorId) == chunkNpTokens.get(landmarkId) ){
				
				while ( trajectorId > 0
						&& mapTokenIdToAnnotations.get(trajectorId-1) != null
						&& chunkNpTokens.get(trajectorId-1) == chunkNpTokens.get(landmarkId)
						){
					trajectorId = mapTokenIdToAnnotations.get(trajectorId-1).get(0).getBegin();
				}
				
				String type = "";
				if ( trajectorId+1 == preposition.getBegin() ){
					type = "<FirstNG|PrepNG>";
				}
				else{
					type = "<FirstNG|...|PrepNG>";						
				}
									
				List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
				List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
				relations.addAll(this.generateAllCombinations(type, trajectors, landmarks, preposition));
			}
		}
		return relations;
	}		
	
	/**
	 * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
	 * @param sentence
	 */
	public List<SpatialRelation> findCandidatesPrepNgNg(Sentence sentence, 
			Map<Integer, List<Annotation>> mapTokenIdToAnnotations, 
			Map<Integer, Annotation> chunkNpTokens, 
			Map<Integer, Annotation> chunkPrepTokens){				
		List<SpatialRelation> relations = new LinkedList<SpatialRelation>();
		/* Szukaj wzorców prep NG* NG* */
		for ( Annotation an : sentence.getAnnotations(this.annotationsPrep) ){						
			Annotation preposition = chunkPrepTokens.get(an.getBegin());
			if ( preposition == null ){
				this.logger.warn("Prep annotation for PrepNG not found: " + an.toString());
				continue;
			}
						
			Integer landmarkId = an.getBegin()+preposition.getTokens().size();
			Integer trajectorId = an.getEnd()+1;
			
			if ( chunkNpTokens.get(landmarkId) != null
					&& chunkNpTokens.get(landmarkId) == chunkNpTokens.get(trajectorId) ){
				String type = "<PrepNG|NG>";
				List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
				List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
				relations.addAll(this.generateAllCombinations(type, trajectors, landmarks, preposition));
			}
		}
		return relations;
	}	

	/**
	 * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
	 * @param sentence
	 */
	public List<SpatialRelation> findCandidatesNgPrepNg(Sentence sentence, 
			Map<Integer, List<Annotation>> mapTokenIdToAnnotations, 
			Map<Integer, Annotation> chunkNpTokens, 
			Map<Integer, Annotation> chunkPrepTokens){				
		List<SpatialRelation> relations = new LinkedList<SpatialRelation>();
		/* Szukaj wzorców prep NG* NG* */
		for ( Annotation landmark : sentence.getAnnotations(this.annotationsNg) ){
			
			Integer prepId = landmark.getBegin()-1;
			if ( prepId <= 0 
					|| !sentence.getTokens().get(prepId).getDisambTag().equals("prep")
					|| chunkPrepTokens.get(prepId) != null ){
				continue;
			}
			
			Integer landmarkId = landmark.getBegin();
			Integer trajectorId = prepId-1;
			
			if ( chunkNpTokens.get(landmarkId) != null
					&& chunkNpTokens.get(landmarkId) == chunkNpTokens.get(trajectorId) ){
				String type = "<NG|prep|NG>";
				List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
				List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
				relations.addAll(this.generateAllCombinations(type, trajectors, landmarks, 
						new Annotation(prepId, "Prep", sentence)));
			}
		}
		return relations;
	}	

	/**
	 * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
	 * @param sentence
	 */
	public List<SpatialRelation> findCandidatesPrepNgVerbfinNg(Sentence sentence, 
			Map<Integer, List<Annotation>> mapTokenIdToAnnotations, 
			Map<Integer, Annotation> chunkVerbfinTokens, 
			Map<Integer, Annotation> chunkPrepTokens){				
		List<SpatialRelation> relations = new LinkedList<SpatialRelation>();
		/* Szukaj wzorców prep NG* NG* */
		for ( Annotation an : sentence.getAnnotations(this.annotationsPrep) ){						
			Annotation preposition = chunkPrepTokens.get(an.getBegin());
			if ( preposition == null ){
				this.logger.warn("Prep annotation for PrepNG not found: " + an.toString());
				continue;
			}
						
			Integer landmarkId = an.getBegin()+preposition.getTokens().size();
			Integer verbfinId = an.getEnd()+1;
			Annotation verbfin = chunkVerbfinTokens.get(verbfinId);
			
			if ( verbfin != null ){
				Integer trajectorId = verbfin.getEnd()+1;			
				String type = "PrepNG|Verbfin|NG";
				List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
				List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
				relations.addAll(this.generateAllCombinations(type, trajectors, landmarks, preposition));
			}
		}
		return relations;
	}		
	
	/**
	 * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
	 * @param sentence
	 */
	public List<SpatialRelation> findCandidatesNgVerbfinPrepNg(Sentence sentence, 
			Map<Integer, List<Annotation>> mapTokenIdToAnnotations, 
			Map<Integer, Annotation> chunkVerbfinTokens, 
			Map<Integer, Annotation> chunkPrepTokens){				
		List<SpatialRelation> relations = new LinkedList<SpatialRelation>();
		/* Szukaj wzorców prep NG* NG* */
		for ( Annotation an : sentence.getAnnotations(this.annotationsPrep) ){						
			Annotation preposition = chunkPrepTokens.get(an.getBegin());
			if ( preposition == null ){
				this.logger.warn("Prep annotation for PrepNG not found: " + an.toString());
				continue;
			}
						
			Integer landmarkId = an.getBegin()+preposition.getTokens().size();
			Integer verbfinId = preposition.getBegin()-1;
			Annotation verbfin = chunkVerbfinTokens.get(verbfinId);
			
			if ( verbfin != null ){
				Integer trajectorId = verbfin.getBegin()-1;			
				String type = "NG|Verbfin|PrepNG";
				List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
				List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
				if ( trajectors != null ){
					relations.addAll(this.generateAllCombinations(type, trajectors, landmarks, preposition));
				}
			}
		}
		return relations;
	}		

	/**
	 * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
	 * @param sentence
	 */
	public List<SpatialRelation> findCandidatesNgPpasPrepNg(Sentence sentence, 
			Map<Integer, List<Annotation>> mapTokenIdToAnnotations,
			Map<Integer, Annotation> chunkNpTokens,
			Map<Integer, Annotation> chunkPpasTokens, 
			Map<Integer, Annotation> chunkPrepTokens){				
		List<SpatialRelation> relations = new LinkedList<SpatialRelation>();
		/* Szukaj wzorców prep NG* NG* */
		for ( Annotation an : sentence.getAnnotations(this.annotationsPrep) ){						
			Annotation preposition = chunkPrepTokens.get(an.getBegin());
			if ( preposition == null ){
				this.logger.warn("Prep annotation for PrepNG not found: " + an.toString());
				continue;
			}
						
			Integer landmarkId = an.getBegin()+preposition.getTokens().size();
			Integer verbfinId = preposition.getBegin()-1;
			Annotation ppas = chunkPpasTokens.get(verbfinId);
			if ( ppas == null ){
				continue;
			}
			Integer trajectorId = ppas.getBegin()-1;			
			
			if ( ppas != null && chunkNpTokens.get(landmarkId) != null 
					&& chunkNpTokens.get(landmarkId) == chunkNpTokens.get(trajectorId) ){
				String type = "<NG|Ppas|PrepNG>";
				List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
				List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
				if ( trajectors != null ){
					relations.addAll(this.generateAllCombinations(type, trajectors, landmarks, preposition));
				}
			}
		}
		return relations;
	}		
	
	/**
	 * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
	 * @param sentence
	 */
	public List<SpatialRelation> findCandidatesNgPactPrepNg(Sentence sentence, 
			Map<Integer, List<Annotation>> mapTokenIdToAnnotations,
			Map<Integer, Annotation> chunkNpTokens,
			Map<Integer, Annotation> chunkPactTokens, 
			Map<Integer, Annotation> chunkPrepTokens){				
		List<SpatialRelation> relations = new LinkedList<SpatialRelation>();
		/* Szukaj wzorców prep NG* NG* */
		for ( Annotation an : sentence.getAnnotations(this.annotationsPrep) ){						
			Annotation preposition = chunkPrepTokens.get(an.getBegin());
			if ( preposition == null ){
				this.logger.warn("Prep annotation for PrepNG not found: " + an.toString());
				continue;
			}
						
			Integer landmarkId = an.getBegin()+preposition.getTokens().size();
			Integer verbfinId = preposition.getBegin()-1;
			Annotation pact = chunkPactTokens.get(verbfinId);
			if ( pact == null ){
				continue;
			}
			Integer trajectorId = pact.getBegin()-1;			
			
			if ( pact != null && chunkNpTokens.get(landmarkId) != null 
					&& chunkNpTokens.get(landmarkId) == chunkNpTokens.get(trajectorId) ){
				String type = "<NG|Pact|PrepNG>";
				List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
				List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
				if ( trajectors != null ){
					relations.addAll(this.generateAllCombinations(type, trajectors, landmarks, preposition));
				}
			}
		}
		return relations;
	}		

	/**
	 * 
	 * @param sentence
	 * @param maltSentence
	 * @return
	 */
	public List<SpatialRelation> findCandidatesByMalt(Sentence sentence, MaltSentence maltSentence){
		List<SpatialRelation> srs = new ArrayList<SpatialRelation>();
		for (int i=0; i< sentence.getTokens().size(); i++){
			Token token = sentence.getTokens().get(i);
			List<Integer> landmarks = new ArrayList<Integer>();
			List<Integer> trajectors = new ArrayList<Integer>();
			Integer indicator = null;
			String type = "MALT";
			String typeLM = "";
			String typeTR = "";
			if ( ClassFeature.BROAD_CLASSES.get("verb").contains(token.getDisambTag().getPos()) ){
				List<MaltSentenceLink> links = maltSentence.getLinksByTargetIndex(i);
				for ( MaltSentenceLink link : links ){
					Token tokenChild = sentence.getTokens().get(link.getSourceIndex());
					if ( link.getRelationType().equals("subj") ){
						if ( tokenChild.getDisambTag().getBase().equals("i")
								|| tokenChild.getDisambTag().getBase().equals("oraz")){
							typeTR = "_TRconj";
							for ( MaltSentenceLink trLink : maltSentence.getLinksByTargetIndex(link.getSourceIndex())){
								landmarks.add(trLink.getSourceIndex());
							}										
						}
						else if ( !tokenChild.getDisambTag().getPos().equals("interp") ){
							trajectors.add(link.getSourceIndex());
						}
					}
					else if ( tokenChild.getDisambTag().getPos().equals("prep") ){
						indicator = link.getSourceIndex();
						for ( MaltSentenceLink prepLink : maltSentence.getLinksByTargetIndex(link.getSourceIndex())){
							Token lm = sentence.getTokens().get(prepLink.getSourceIndex());
							if ( lm.getOrth().equals(",")){
								typeLM = "_LMconj";
								for ( MaltSentenceLink prepLinkComma : maltSentence.getLinksByTargetIndex(prepLink.getSourceIndex())){
									landmarks.add(prepLinkComma.getSourceIndex());
								}
							}
							else if (!lm.getDisambTag().getPos().equals("interp")){
								landmarks.add(prepLink.getSourceIndex());
							}
						}
					}
				}
			}
			
			if ( landmarks.size() > 0 && trajectors.size() > 0 && indicator != null ){
				for ( Integer landmark : landmarks ){
					for ( Integer trajector : trajectors ){
						SpatialRelation sr = new SpatialRelation(
								type + typeLM + typeTR, 
								new Annotation(trajector, "TR", sentence), 
								new Annotation(indicator, "SI", sentence), 
								new Annotation(landmark, "LM", sentence));
						srs.add(sr);
					}
				}
			}
		}
		return srs;
	}
	
	/**
	 * 
	 * @param type
	 * @param trajectors
	 * @param landmarks
	 * @param preposition
	 * @return
	 */
	private List<SpatialRelation> generateAllCombinations(String type, List<Annotation> trajectors, List<Annotation> landmarks, Annotation preposition){
		List<SpatialRelation> relations = new ArrayList<SpatialRelation>();
		if ( trajectors != null && landmarks != null ){
			for ( Annotation trajector : trajectors ){
				for ( Annotation landmark : landmarks ){
					SpatialRelation sr = new SpatialRelation(type, trajector, preposition, landmark);
					relations.add(sr);						
				}
			}
		}	
		return relations;
	}
		
	/**
	 * 
	 * @param document
	 * @return
	 */
	private List<SpatialRelation> getSpatialRelations(Document document){
		Set<String> objectPos = new HashSet<String>();
		objectPos.add("subst");
		objectPos.add("ign");
		
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
			if ( (landmark != null || region != null) && trajector != null ){
				if ( landmark == null || (landmark != null && region != null && region.getBegin() < landmark.getBegin() ) ){
					landmark = region;
				}
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
