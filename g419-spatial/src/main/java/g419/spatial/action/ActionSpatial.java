package g419.spatial.action;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;
import g419.lib.cli.CommonOptions;
import g419.lib.cli.action.Action;
import g419.liner2.api.Liner2;
import g419.liner2.api.chunker.IobberChunker;
import g419.spatial.filter.IRelationFilter;
import g419.spatial.filter.RelationFilterPronoun;
import g419.spatial.filter.RelationFilterSemanticPattern;
import g419.spatial.filter.RelationFilterSpatialIndicator;
import g419.spatial.structure.SpatialRelation;
import g419.toolbox.sumo.NamToSumo;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.log4j.Logger;

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
		OptionBuilder.withArgName(ActionSpatial.OPTION_FILENAME_LONG);
		OptionBuilder.hasArg();
		OptionBuilder.isRequired();
		OptionBuilder.withDescription("path to the input file");
		OptionBuilder.withLongOpt(OPTION_FILENAME_LONG);
		return OptionBuilder.create(ActionSpatial.OPTION_FILENAME);			
	}

	/**
	 * Parse action options
	 * @param arg0 The array with command line parameters
	 */
	@Override
	public void parseOptions(String[] args) throws Exception {
        CommandLine line = new GnuParser().parse(this.options, args);
        parseDefault(line);
        this.filename = line.getOptionValue(ActionSpatial.OPTION_FILENAME);
        this.inputFormat = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT);
    }

	@Override
	public void run() throws Exception {
		AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(this.filename, this.inputFormat);
		Document document = reader.nextDocument();
		
		List<IRelationFilter> filters = new LinkedList<IRelationFilter>();
		filters.add(new RelationFilterSpatialIndicator());
		filters.add(new RelationFilterPronoun());
		filters.add(new RelationFilterSemanticPattern());
		
		IobberChunker iobber = new IobberChunker("", this.config_iobber_model, this.config_iobber_config);
		
		Liner2 liner2 = new Liner2(this.config_liner2_model);
		
		while ( document != null ){					
			Logger.getLogger(this.getClass()).info("\nDocument: " + document.getName());
			liner2.chunkInPlace(document);
			iobber.chunkInPlace(document);			
			
			for (Paragraph paragraph : document.getParagraphs()){
				for (Sentence sentence : paragraph.getSentences()){
					this.splitPrepNg(sentence);
					List<SpatialRelation> relations = new LinkedList<SpatialRelation>();
					relations.addAll( this.findSpatialRelationsNgAnyPrepNg(sentence) );
										
					this.replaceNgWithNames(sentence, relations);
					
					if ( relations.size() > 0 ){
						System.out.println();
						System.out.println(sentence);
						System.out.println();
						for ( SpatialRelation rel : relations ){
							String status = "OK    ";
							String filterName = "";
							
							for ( IRelationFilter filter : filters ){
								if ( !filter.pass(rel) ){
									status = "REMOVE";
									filterName = filter.getClass().getSimpleName();
									break;
								}								
							}
							
							System.out.println(String.format("  - %s\t %-80s\t%s",status, rel.toString(), filterName));
						}
							
					}
				}
			}
			document = reader.nextDocument();
		}
			
		reader.close();
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
				Logger.getLogger(this.getClass()).info(String.format("Replace NG (%s) with nam (%s)", relation.getLandmark(), landmarkName));
				landmarkName.setHead(relation.getLandmark().getHead());
				relation.setLandmark(landmarkName);
			}
			// Sprawdź trajector
			String trajectorKey = String.format("%d:%d",relation.getTrajector().getBegin(), relation.getTrajector().getEnd());
			Annotation trajectorName = names.get(trajectorKey);
			if ( trajectorName != null ){
				Logger.getLogger(this.getClass()).info(String.format("Replace NG (%s) with nam (%s)", relation.getTrajector(), trajectorName));
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
	public List<SpatialRelation> findSpatialRelationsNgAnyPrepNg(Sentence sentence){		
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
		
		/* Zaindeksuj frazy np */
		Map<Integer, Annotation> chunkNpTokens = new HashMap<Integer, Annotation>();
		Map<Integer, Annotation> chunkPrepTokens = new HashMap<Integer, Annotation>();
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
		}
		
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
			Integer trajectorNextId = an.getEnd()+1;
			
			boolean breakSearchPrev = false;
			while ( !breakSearchPrev && trajectorPrevId >=0 && mapTokenIdToAnnotations.get(trajectorPrevId) == null ){
				/* Przecinek i nawias zamykający przerywają poszykiwanie */
				String orth = sentence.getTokens().get(trajectorPrevId).getOrth(); 
				if ( orth.equals(",") || orth.equals(")") ){
					breakSearchPrev = true;
				}
				trajectorPrevId--;
			}
			
			boolean breakSearchNext = false;
			while ( !breakSearchNext
					&& trajectorNextId < sentence.getTokenNumber()
					&& mapTokenIdToAnnotations.get(trajectorNextId) == null ){
				/* Przecinek i nawias zamykający przerywają poszykiwanie */
				String orth = sentence.getTokens().get(trajectorNextId).getOrth(); 
				if ( orth.equals(",") || orth.equals(")") ){
					breakSearchNext = true;
				}
				trajectorNextId++;
			}
			
			if ( !breakSearchPrev || !breakSearchNext ){
				String type = "";
				Integer trajectorId = null;
				if ( (!breakSearchPrev && !breakSearchNext && (an.getBegin() - trajectorPrevId < trajectorNextId - an.getEnd()))
						|| !breakSearchPrev ){
					type = "NG*|...|prep|NG*";
					trajectorId = trajectorPrevId;
				}
				else{
					type = "prep|NG*|...|NG*";
					trajectorId = trajectorNextId;						
				}
				List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
				List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
				if ( trajectors != null && landmarks != null ){
					for ( Annotation trajector : trajectors ){
						for ( Annotation landmark : landmarks ){
							SpatialRelation sr = new SpatialRelation(type, trajector, preposition, landmark);
							relations.add(sr);						
						}
					}
				}
			}
		}
		return relations;
	}	
	
//	/**
//	 * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
//	 * @param sentence
//	 */
//	public List<SpatialRelation> findSpatialRelationsNamAnyPrepNg(Sentence sentence){		
//		/* Zaindeksuj pierwsze tokeny anotacji NG* */
//		Map<Integer, List<Annotation>> mapTokenIdToAnnotations = new HashMap<Integer, List<Annotation>>();
//		for ( Annotation an : sentence.getAnnotations(this.annotationsNg) ){
//			for ( Integer n = an.getBegin(); n <= an.getEnd(); n++){
//				if ( !mapTokenIdToAnnotations.containsKey(n) ){
//					mapTokenIdToAnnotations.put(n, new LinkedList<Annotation>());
//				}
//				mapTokenIdToAnnotations.get(n).add(an);
//			}
//		}
//		
//		List<SpatialRelation> relations = new LinkedList<SpatialRelation>();
//		/* Szukaj wzorców NG* prep NG* */
//		for ( Annotation an : sentence.getAnnotations(this.annotationsPrep) ){
//			Integer prepId = an.getBegin();
//			Integer landmarkId = an.getBegin()+1;
//			Integer trajectorId = an.getBegin()-1;
//			while ( trajectorId >=0 && mapTokenIdToAnnotations.get(trajectorId) == null ){
//				trajectorId--;
//			}
//			List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
//			List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
//			if ( trajectors != null && landmarks != null ){
//				for ( Annotation trajector : trajectors ){
//					for ( Annotation landmark : landmarks ){
//						SpatialRelation sr = new SpatialRelation("NG*|...|prep|NG*", trajector, sentence.getTokens().get(prepId), landmark);
//						relations.add(sr);						
//					}
//				}
//			}
//		}
//		return relations;
//	}	
	
}
