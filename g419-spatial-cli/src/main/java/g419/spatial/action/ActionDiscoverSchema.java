package g419.spatial.action;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.schema.tagset.MappingNkjpToConllPos;
import g419.corpus.structure.*;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.liner2.core.Liner2;
import g419.liner2.core.chunker.IobberChunker;
import g419.liner2.core.features.tokens.ClassFeature;
import g419.liner2.core.tools.parser.MaltParser;
import g419.liner2.core.tools.parser.MaltSentence;
import g419.liner2.core.tools.parser.MaltSentenceLink;
import g419.spatial.filter.*;
import g419.spatial.structure.SpatialExpression;
import g419.spatial.structure.SpatialRelationSchema;
import g419.toolbox.sumo.Sumo;
import g419.toolbox.wordnet.NamToWordnet;
import g419.toolbox.wordnet.Wordnet3;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.regex.Pattern;

public class ActionDiscoverSchema extends Action {
	
	private final static String OPTION_FILENAME_LONG = "filename";
	private final static String OPTION_FILENAME = "f";
	
	private List<Pattern> annotationsPrep = new LinkedList<Pattern>();
	private List<Pattern> annotationsNg = new LinkedList<Pattern>();
	
	private Pattern patternAnnotationNam = Pattern.compile("^nam(_.*|$)");
	
	private String filename = null;
	private String inputFormat = null;
	
	private Logger logger = Logger.getLogger("ActionSpatial");
	private Set<String> objectPos = new HashSet<String>();
	
	/* Parametry, które będzie trzeba wyciągnąć do pliku ini. */
	private String config_liner2_model = "/home/czuk/nlp/eclipse/workspace_liner2/models-released/liner2.5/liner25-model-pack-ibl/config-n82.ini";
	private String config_iobber_model = "model-kpwr11-H";
	private String config_iobber_config = "kpwr.ini";
	private String wordnet = "/nlp/resources/plwordnet/plwordnet_2_3_mod/plwordnet_2_3_pwn_format/";

	public ActionDiscoverSchema() {
		super("discover-schema");
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
		return Option.builder(ActionDiscoverSchema.OPTION_FILENAME).hasArg().argName("FILENAME").required()
						.desc("path to the input file").longOpt(OPTION_FILENAME_LONG).build();			
	}

	/**
	 * Parse action options
	 * @param args The array with command line parameters
	 */
	@Override
	public void parseOptions(final CommandLine line) throws Exception {
        this.filename = line.getOptionValue(ActionDiscoverSchema.OPTION_FILENAME);
        this.inputFormat = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT);
    }

	@Override
	public void run() throws Exception {
		AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(this.filename, this.inputFormat);
		Document document = null;
		
		Sumo sumo = new Sumo();
		Wordnet3 wordnet = new Wordnet3(this.wordnet);
		NamToWordnet nam2wordnet = new NamToWordnet(wordnet);
		
		RelationFilterSemanticPattern semanticFilter = new RelationFilterSemanticPattern(); 
		
		List<IRelationFilter> filters = new LinkedList<IRelationFilter>();
		filters.add(new RelationFilterSpatialIndicator());
		filters.add(new RelationFilterPronoun());
		filters.add(semanticFilter);
		filters.add(new RelationFilterPrepositionBeforeLandmark());
		filters.add(new RelationFilterLandmarkTrajectorException());
		filters.add(new RelationFilterHolonyms(wordnet, nam2wordnet));
		
		IobberChunker iobber = new IobberChunker("", this.config_iobber_model, this.config_iobber_config);		
		Liner2 liner2 = new Liner2(this.config_liner2_model);
		
		MaltParser malt = new MaltParser("/nlp/resources/maltparser/skladnica_liblinear_stackeager_final.mco");
				
		while ( ( document = reader.nextDocument() ) != null ){					
			System.out.println("=======================================");
			//Logger.getLogger(this.getClass()).info("\nDocument: " + document.getName());
			System.out.println("Document: " + document.getName());
			System.out.println("=======================================");
			
			//liner2.chunkInPlace(document);
			//iobber.chunkInPlace(document);			
						
			for (Paragraph paragraph : document.getParagraphs()){
				for (Sentence sentence : paragraph.getSentences()){
					
					this.splitPrepNg(sentence);
					
					MaltSentence maltSentence = new MaltSentence(sentence, MappingNkjpToConllPos.get());
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
					
					
					List<SpatialExpression> relations = new LinkedList<SpatialExpression>();

					// Second Iteration only
					
					relations.addAll( this.findCandidatesFirstNgAnyPrepNg(sentence, mapTokenIdToAnnotations, chunkNpTokens, chunkPrepTokens) );
					relations.addAll( this.findCandidatesByMalt(sentence, maltSentence));
					

					relations.addAll( this.findCandidatesNgPpasPrepNg(
							sentence, mapTokenIdToAnnotations, chunkNpTokens, chunkPpasTokens, chunkPrepTokens));
					relations.addAll( this.findCandidatesNgPactPrepNg(
							sentence, mapTokenIdToAnnotations, chunkNpTokens, chunkPactTokens, chunkPrepTokens));

					this.replaceNgWithNames(sentence, relations);
					
					if ( relations.size() > 0 ){
						for ( SpatialExpression rel : relations ){
							
							boolean pass = true;
							
							for ( IRelationFilter filter : filters ){
								if ( !filter.pass(rel) ){
									System.out.println("- " + rel.toString() + "\t" + filter.getClass().getSimpleName());
									if ( filter.getClass() == RelationFilterSemanticPattern.class ){
										Set<String> trajectorConcepts = rel.getTrajectorConcepts();
										Set<String> landmarkConcepts = rel.getLandmarkConcepts();
										System.out.println("\t\t\tTrajector = " + rel.getTrajector() + " => " + String.join(", ", trajectorConcepts));
										System.out.println("\t\t\tLandmark  = " + rel.getLandmark() + " => " + String.join(", ", landmarkConcepts));

										Set<String> trajectorConceptsSuper = new HashSet<String>();
										Set<String> landmarkConceptsSuper = new HashSet<String>();

										for ( String concept : trajectorConcepts ){
											trajectorConceptsSuper.addAll(sumo.getSuperclasses(concept));
										}

										for ( String concept : landmarkConcepts ){
											landmarkConceptsSuper.addAll(sumo.getSuperclasses(concept));
										}

										if ( ( trajectorConceptsSuper.contains("physical")
												|| trajectorConceptsSuper.contains("object") ) && landmarkConceptsSuper.contains("physical") ){
											System.out.println("PHYSICAL/OBJECT");
										}
									}
									pass = false;
									break;
								}								
							}
							
							if ( pass ){
								StringBuilder sb = new StringBuilder();
								for ( SpatialRelationSchema p : semanticFilter.match(rel) ){
									if ( sb.length() > 0 ){
										sb.append(" & ");
									}
									sb.append(p.getName());
								}
								
								System.out.println("+ " + rel.toString());
							}
						}
							
					}
				}
			}		
		}
			
		reader.close();
	}
	
	/**
	 * Dla fraz NG, które pokrywają się z nazwą własną, zmienia NG na nazwę.
	 * @param sentence
	 * @param relations
	 */
	private void replaceNgWithNames(Sentence sentence, List<SpatialExpression> relations){
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
		for ( SpatialExpression relation : relations ){
			// Sprawdź landmark
			String landmarkKey = String.format("%d:%d",relation.getLandmark().getSpatialObject().getBegin(), relation.getLandmark().getSpatialObject().getEnd());
			Annotation landmarkName = names.get(landmarkKey);
			if ( landmarkName != null ){
				Logger.getLogger(this.getClass()).info(String.format("Replace %s (%s) with nam (%s)", relation.getLandmark().getSpatialObject().getType(), relation.getLandmark(), landmarkName));
				landmarkName.setHead(relation.getLandmark().getSpatialObject().getHead());
				relation.setLandmark(landmarkName);
			}
			// Sprawdź trajector
			String trajectorKey = String.format("%d:%d",relation.getTrajector().getSpatialObject().getBegin(), relation.getTrajector().getSpatialObject().getEnd());
			Annotation trajectorName = names.get(trajectorKey);
			if ( trajectorName != null ){
				Logger.getLogger(this.getClass()).info(String.format("Replace %s (%s) with nam (%s)", relation.getTrajector().getSpatialObject().getType(), relation.getTrajector(), trajectorName));
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
	public List<SpatialExpression> findCandidatesNgAnyPrepNg(Sentence sentence, 
			Map<Integer, List<Annotation>> mapTokenIdToAnnotations, 
			Map<Integer, Annotation> chunkNpTokens, 
			Map<Integer, Annotation> chunkPrepTokens){				
		List<SpatialExpression> relations = new LinkedList<SpatialExpression>();
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
	public List<SpatialExpression> findCandidatesNgPrepNgNoNp(Sentence sentence, 
			Map<Integer, List<Annotation>> mapTokenIdToAnnotations, 
			Map<Integer, Annotation> chunkNpTokens, 
			Map<Integer, Annotation> chunkPrepTokens){				
		List<SpatialExpression> relations = new LinkedList<SpatialExpression>();
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
	public List<SpatialExpression> findCandidatesNgPrepNgDiffNp(Sentence sentence, 
			Map<Integer, List<Annotation>> mapTokenIdToAnnotations, 
			Map<Integer, Annotation> chunkNpTokens, 
			Map<Integer, Annotation> chunkPrepTokens){				
		List<SpatialExpression> relations = new LinkedList<SpatialExpression>();
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
	public List<SpatialExpression> findCandidatesPrepNgNgDiffNp(Sentence sentence, 
			Map<Integer, List<Annotation>> mapTokenIdToAnnotations, 
			Map<Integer, Annotation> chunkNpTokens, 
			Map<Integer, Annotation> chunkPrepTokens){				
		List<SpatialExpression> relations = new LinkedList<SpatialExpression>();
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
	public List<SpatialExpression> findCandidatesNgPrepNgPpasPrepNg(Sentence sentence, 
			Map<Integer, List<Annotation>> mapTokenIdToAnnotations, 
			Map<Integer, Annotation> chunkNpTokens, 
			Map<Integer, Annotation> chunkPrepTokens,
			Map<Integer, Annotation> chunkPpasTokens){				
		List<SpatialExpression> relations = new LinkedList<SpatialExpression>();
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
	public List<SpatialExpression> findCandidatesNgPrepNgCommaPrepNg(Sentence sentence, 
			Map<Integer, List<Annotation>> mapTokenIdToAnnotations, 
			Map<Integer, Annotation> chunkNpTokens, 
			Map<Integer, Annotation> chunkPrepTokens){				
		List<SpatialExpression> relations = new LinkedList<SpatialExpression>();
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
	public List<SpatialExpression> findCandidatesNgPrepNgPrepNg(Sentence sentence, 
			Map<Integer, List<Annotation>> mapTokenIdToAnnotations, 
			Map<Integer, Annotation> chunkNpTokens, 
			Map<Integer, Annotation> chunkPrepTokens){				
		List<SpatialExpression> relations = new LinkedList<SpatialExpression>();
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
	public List<SpatialExpression> findCandidatesNgNgPrepNg(Sentence sentence, 
			Map<Integer, List<Annotation>> mapTokenIdToAnnotations, 
			Map<Integer, Annotation> chunkNpTokens, 
			Map<Integer, Annotation> chunkPrepTokens){				
		List<SpatialExpression> relations = new LinkedList<SpatialExpression>();
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
	public List<SpatialExpression> findCandidatesFirstNgAnyPrepNg(Sentence sentence, 
			Map<Integer, List<Annotation>> mapTokenIdToAnnotations, 
			Map<Integer, Annotation> chunkNpTokens, 
			Map<Integer, Annotation> chunkPrepTokens){				
		List<SpatialExpression> relations = new LinkedList<SpatialExpression>();
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
	public List<SpatialExpression> findCandidatesPrepNgNg(Sentence sentence, 
			Map<Integer, List<Annotation>> mapTokenIdToAnnotations, 
			Map<Integer, Annotation> chunkNpTokens, 
			Map<Integer, Annotation> chunkPrepTokens){				
		List<SpatialExpression> relations = new LinkedList<SpatialExpression>();
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
	public List<SpatialExpression> findCandidatesNgPrepNg(Sentence sentence, 
			Map<Integer, List<Annotation>> mapTokenIdToAnnotations, 
			Map<Integer, Annotation> chunkNpTokens, 
			Map<Integer, Annotation> chunkPrepTokens){				
		List<SpatialExpression> relations = new LinkedList<SpatialExpression>();
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
	public List<SpatialExpression> findCandidatesPrepNgVerbfinNg(Sentence sentence, 
			Map<Integer, List<Annotation>> mapTokenIdToAnnotations, 
			Map<Integer, Annotation> chunkVerbfinTokens, 
			Map<Integer, Annotation> chunkPrepTokens){				
		List<SpatialExpression> relations = new LinkedList<SpatialExpression>();
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
	public List<SpatialExpression> findCandidatesNgVerbfinPrepNg(Sentence sentence, 
			Map<Integer, List<Annotation>> mapTokenIdToAnnotations, 
			Map<Integer, Annotation> chunkVerbfinTokens, 
			Map<Integer, Annotation> chunkPrepTokens){				
		List<SpatialExpression> relations = new LinkedList<SpatialExpression>();
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
	public List<SpatialExpression> findCandidatesNgPpasPrepNg(Sentence sentence, 
			Map<Integer, List<Annotation>> mapTokenIdToAnnotations,
			Map<Integer, Annotation> chunkNpTokens,
			Map<Integer, Annotation> chunkPpasTokens, 
			Map<Integer, Annotation> chunkPrepTokens){				
		List<SpatialExpression> relations = new LinkedList<SpatialExpression>();
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
	public List<SpatialExpression> findCandidatesNgPactPrepNg(Sentence sentence, 
			Map<Integer, List<Annotation>> mapTokenIdToAnnotations,
			Map<Integer, Annotation> chunkNpTokens,
			Map<Integer, Annotation> chunkPactTokens, 
			Map<Integer, Annotation> chunkPrepTokens){				
		List<SpatialExpression> relations = new LinkedList<SpatialExpression>();
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
	public List<SpatialExpression> findCandidatesByMalt(Sentence sentence, MaltSentence maltSentence){
		List<SpatialExpression> srs = new ArrayList<SpatialExpression>();
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
						else if ( this.objectPos.contains(tokenChild.getDisambTag().getPos()) ){
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
							else if ( this.objectPos.contains(lm.getDisambTag().getPos()) ){
								landmarks.add(prepLink.getSourceIndex());
							}
						}
					}
				}
			}
			
			if ( landmarks.size() > 0 && trajectors.size() > 0 && indicator != null ){
				for ( Integer landmark : landmarks ){
					for ( Integer trajector : trajectors ){
						SpatialExpression sr = new SpatialExpression(
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
	private List<SpatialExpression> generateAllCombinations(String type, List<Annotation> trajectors, List<Annotation> landmarks, Annotation preposition){
		List<SpatialExpression> relations = new ArrayList<SpatialExpression>();
		if ( trajectors != null && landmarks != null ){
			for ( Annotation trajector : trajectors ){
				for ( Annotation landmark : landmarks ){
					SpatialExpression sr = new SpatialExpression(type, trajector, preposition, landmark);
					relations.add(sr);						
				}
			}
		}	
		return relations;
	}
		
}
