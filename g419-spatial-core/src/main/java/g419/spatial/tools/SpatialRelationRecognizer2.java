package g419.spatial.tools;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import g419.corpus.schema.kpwr.KpwrSpatial;
import g419.corpus.schema.tagset.MappingNkjpToConllPos;
import g419.corpus.structure.*;
import g419.liner2.core.features.tokens.ClassFeature;
import g419.liner2.core.tools.parser.MaltParser;
import g419.liner2.core.tools.parser.MaltSentence;
import g419.liner2.core.tools.parser.MaltSentenceLink;
import g419.spatial.filter.*;
import g419.spatial.structure.SpatialExpression;
import g419.spatial.structure.SpatialRelationSchema;
import g419.toolbox.wordnet.NamToWordnet;
import g419.toolbox.wordnet.Wordnet3;
import org.apache.log4j.Logger;
import org.maltparser.core.exception.MaltChainedException;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SpatialRelationRecognizer2 {

	final private MaltParser malt;
	final List<IRelationFilter> filters;
	final RelationFilterSemanticPattern semanticFilter;

	final private Pattern annotationsPrep = Pattern.compile("^PrepNG.*$");
	final private Pattern annotationsNg = Pattern.compile("^NG.*$");
	final private Pattern patternAnnotationNam = Pattern.compile("^nam(_(fac|liv|loc|pro|oth).*|$)");

	final private Set<String> objectPos;
	final private Set<String> regions = SpatialResources.getRegions();

	final private Logger logger = Logger.getLogger(this.getClass());


	/**
	 * @throws IOException
	 * @param malt Ścieżka do modelu Maltparsera
	 * @param wordnet Ścieżka do wordnetu w formacie PWN
	 */
	public SpatialRelationRecognizer2(MaltParser malt, Wordnet3 wordnet) throws IOException{
		this.malt = malt;
		this.objectPos = Sets.newHashSet("subst", "ign", "brev");
		this.semanticFilter = new RelationFilterSemanticPattern();

		this.filters = Lists.newLinkedList();
		this.filters.add(new RelationFilterPronoun());
		this.filters.add(new RelationFilterDifferentObjects());
		this.filters.add(this.semanticFilter);
		this.filters.add(new RelationFilterPrepositionBeforeLandmark());
		this.filters.add(new RelationFilterLandmarkTrajectorException());
		this.filters.add(new RelationFilterHolonyms(wordnet, new NamToWordnet(wordnet)));
	}
	
	/**
	 * 
	 * @return
	 */
	public List<IRelationFilter> getFilters(){
		return this.filters;
	}
	
	/**
	 * 
	 * @return
	 */
	public RelationFilterSemanticPattern getSemanticFilter(){
		return this.semanticFilter;
	}

	public List<SpatialExpression> recognize(Document document) {
		List<SpatialExpression> expressions = Lists.newArrayList();
		document.getSentences().stream().forEach((Sentence s) -> expressions.addAll(this.recognize(s)));
		return expressions;
	}

	/**
	 * Rozpoznaje wyrażenia przestrzenne i zwraca je jako listę obiektów SpatialExpression
	 * @param sentence
	 * @return
	 * @throws MaltChainedException 
	 */
	public List<SpatialExpression> recognize(Sentence sentence) {
		List<SpatialExpression> finalRelations = Lists.newArrayList();
		try {
			findCandidates(sentence).stream()
					.filter(se->!getFilterDiscardingRelation(se).isPresent()).forEach(finalRelations::add);
		} catch (MaltChainedException ex){
			logger.error("Failed to recognize spatial expressions due to an exception", ex);
		}
		return finalRelations;
	}

	/**
	 * Passes the spatial expression through the list of filters and return the first filter, for which
	 * the expressions was discarded.
	 * @param se Spatial expression to test
	 * @return
	 */
	public Optional<String> getFilterDiscardingRelation(SpatialExpression se){
		Iterator<IRelationFilter> filters = this.getFilters().iterator();
		while (filters.hasNext()) {
			IRelationFilter filter = filters.next();
			if (!filter.pass(se)) {
				return Optional.ofNullable(filter.getClass().getSimpleName());
			}
		}
		return Optional.ofNullable(null);
	}

	/**
	 * 
	 * @param sentence
	 * @return
	 * @throws MaltChainedException
	 */
	public List<SpatialExpression> findCandidates(Sentence sentence) throws MaltChainedException{
		NkjpSyntacticChunks.splitPrepNg(sentence);

		SentenceAnnotationIndexTypePos anIndex = new SentenceAnnotationIndexTypePos(sentence);

		MaltSentence maltSentence = new MaltSentence(sentence, MappingNkjpToConllPos.get());
		this.malt.parse(maltSentence);

		/* Zaindeksuj różne typy fraz */
		Map<Integer, Annotation> chunkNpTokens = new HashMap<Integer, Annotation>();
		Map<Integer, Annotation> chunkVerbfinTokens = new HashMap<Integer, Annotation>();
		Map<Integer, Annotation> chunkPrepTokens = new HashMap<Integer, Annotation>();
		Map<Integer, Annotation> chunkPpasTokens = new HashMap<Integer, Annotation>();
		Map<Integer, Annotation> chunkPactTokens = new HashMap<Integer, Annotation>();
		Map<Integer, Annotation> chunkNamesTokens = new HashMap<Integer, Annotation>();
		Map<Integer, List<Annotation>> chunkNgTokens = new HashMap<Integer, List<Annotation>>();
		
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

		/* Zaindeksuj tokeny jednostek identyfikacyjnych */
		for ( Annotation an : sentence.getAnnotations(this.patternAnnotationNam) ){
			for ( Integer n = an.getBegin(); n <= an.getEnd(); n++){
				chunkNamesTokens.put(n, an);					
			}
		}
		
		/* Zaindeksuj pierwsze tokeny anotacji NG* */
		for ( Annotation an : sentence.getAnnotations(this.annotationsNg) ){
			for ( Integer n = an.getBegin(); n <= an.getEnd(); n++){
				if ( !chunkNgTokens.containsKey(n) ){
					chunkNgTokens.put(n, new LinkedList<Annotation>());
				}
				chunkNgTokens.get(n).add(an);
			}
		}
		
		List<SpatialExpression> relations = new LinkedList<SpatialExpression>();

		// Second Iteration only		
		//relations.addAll( this.findCandidatesFirstNgAnyPrepNg(sentence, chunkNgTokens, chunkNpTokens, chunkPrepTokens) );
		relations.addAll( this.findCandidatesByMalt(sentence, maltSentence, chunkPrepTokens, chunkNamesTokens, chunkNgTokens));
		//relations.addAll( this.findCandidatesNgPpasPrepNg(sentence, chunkNgTokens, chunkNpTokens, chunkPpasTokens, chunkPrepTokens));
		//relations.addAll( this.findCandidatesNgPactPrepNg(sentence, chunkNgTokens, chunkNpTokens, chunkPactTokens, chunkPrepTokens));

		// Sprawdź, czy landmarkiem jest region. Jeżeli tak, to przesuń landmark na najbliższych ign lub subst
		for ( SpatialExpression rel : relations ){
			if ( this.regions.contains(rel.getLandmark().getSpatialObject().getHeadToken().getDisambTag().getBase()) ){
				int i = rel.getLandmark().getSpatialObject().getEnd() + 1;
				List<Token> tokens = rel.getLandmark().getSpatialObject().getSentence().getTokens();
				
				if ( i < tokens.size() && chunkNamesTokens.get(i) != null ){
					Annotation an = chunkNamesTokens.get(i);
					this.logger.info("REPLACE_REGION_NEXT_NAME " + rel.getLandmark().toString() + " => " + an.toString());
					rel.getLandmark().setRegion(rel.getLandmark().getSpatialObject());
					rel.getLandmark().setSpatialObject(an);
				}
				else{
					// Znajdż zagnieżdżone NG występujące po regionie
					Annotation ng = null;
					int j = rel.getLandmark().getSpatialObject().getHead() + 1;
					while ( j <= rel.getLandmark().getSpatialObject().getEnd() && ng == null ){
						List<Annotation> innerNgs = chunkNgTokens.get(j);
						if ( innerNgs != null ){
							for (Annotation an : innerNgs ){
								if ( an.getBegin() > rel.getLandmark().getSpatialObject().getHead() ){
									ng = an;
								}
							}							
						}
						j++;
					}
					if ( ng != null ){
						this.logger.info("REPLACE_REGION_INNER_NG" + rel.getLandmark().toString() + " => " + ng.toString());
						Annotation newLandmark = null;
						List<Annotation> newLandmakrs = chunkNgTokens.get(rel.getLandmark().getSpatialObject().getHead());
						if ( newLandmakrs != null ){
							for (Annotation an : newLandmakrs ){
								if ( an != rel.getLandmark().getSpatialObject() && (newLandmark == null || newLandmark.getTokens().size() < an.getTokens().size() ) ){
									newLandmark = an;
								}
							}
						}
						if ( newLandmark == null ){
							newLandmark = new Annotation(rel.getLandmark().getSpatialObject().getBegin(), ng.getBegin()-1, "NG", sentence);
						}
						rel.getLandmark().setRegion(newLandmark);
						rel.getLandmark().setSpatialObject(ng);
					}
					else{
						// Znajdź pierwszy subst lub ign po prawej stronie
						Integer subst_or_ign = null;
						int k = rel.getLandmark().getSpatialObject().getHead() + 1;
						while ( k <= rel.getLandmark().getSpatialObject().getEnd() && subst_or_ign == null ){
							if ( this.objectPos.contains(tokens.get(k).getDisambTag().getPos()) ){
								subst_or_ign = k;
							}
							k++;
						}
						if ( subst_or_ign != null ){
							// Wszystko po prawje staje się nową anotacją NG
							Annotation newRegion = new Annotation(rel.getLandmark().getSpatialObject().getHead(), "NG", sentence);
							Annotation newLandmark = new Annotation(rel.getLandmark().getSpatialObject().getHead()+1, rel.getLandmark().getSpatialObject().getEnd(), "NG", sentence);
							sentence.addChunk(newRegion);
							sentence.addChunk(newLandmark);
							this.logger.info("REPLACE_REGION_INNER_SUBST_IGN" + rel.getLandmark().toString() + " => " + newLandmark);
							rel.getLandmark().setSpatialObject(newLandmark);
							rel.getLandmark().setRegion(newRegion);
						}
					}
				}
			}
		}
		
		// Usuń kandydatów, dla których spatial indicator jest częścią nazwy
		relations.removeAll(relations.stream()
				.filter(r->chunkNamesTokens.get(r.getSpatialIndicator().getHead()) != null).collect(Collectors.toList()));
		
		// Jeżeli frazy NG pokrywają się z nam_, to podmień anotacje
		this.replaceNgWithNames(sentence, relations, chunkNamesTokens);
		
		return relations;
	}
	
	/**
	 * Generuje wszystkie możliwe kombinacje NG + PrepNG
	 * @return
	 */
	private Collection<? extends SpatialExpression> findCandidatesAllCombinations() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Dla fraz NG, które pokrywają się z nazwą własną, zmienia NG na nazwę.
	 * @param sentence
	 * @param relations
	 */
	private void replaceNgWithNames(Sentence sentence, List<SpatialExpression> relations, Map<Integer, Annotation> names){
		for ( SpatialExpression relation : relations ){
			// Sprawdź landmark
			//String landmarkKey = String.format("%d:%d",relation.getLandmark().getBegin(), relation.getLandmark().getEnd());
			Integer landmarkKey = relation.getLandmark().getSpatialObject().getBegin();
			Annotation landmarkName = names.get(landmarkKey);
			if ( landmarkName != null && landmarkName != relation.getLandmark().getSpatialObject() ){
				Logger.getLogger(this.getClass()).info(String.format("Replace %s (%s) with nam (%s)", relation.getLandmark().getSpatialObject().getType(), relation.getLandmark(), landmarkName));
				landmarkName.setHead(relation.getLandmark().getSpatialObject().getHead());
				relation.getLandmark().setSpatialObject(landmarkName);
			}
			// Sprawdź trajector
			//String trajectorKey = String.format("%d:%d",relation.getTrajector().getBegin(), relation.getTrajector().getEnd());
			Integer trajectorKey = relation.getTrajector().getSpatialObject().getBegin();
			Annotation trajectorName = names.get(trajectorKey);
			if ( trajectorName != null && trajectorName != relation.getTrajector().getSpatialObject() ){
				Logger.getLogger(this.getClass()).info(String.format("Replace %s (%s) with nam (%s)", relation.getTrajector().getSpatialObject().getType(), relation.getTrajector(), trajectorName));
				trajectorName.setHead(relation.getTrajector().getSpatialObject().getHead());
				relation.getTrajector().setSpatialObject(trajectorName);
			}
		}
	}
	
	/**
	 * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
	 * @param sentence
	 */
//	public List<SpatialExpression> findCandidatesNgAnyPrepNg(Sentence sentence,
//			Map<Integer, List<Annotation>> mapTokenIdToAnnotations,
//			Map<Integer, Annotation> chunkNpTokens,
//			Map<Integer, Annotation> chunkPrepTokens){
//		List<SpatialExpression> relations = new LinkedList<SpatialExpression>();
//		/* Szukaj wzorców NG* prep NG* */
//		for ( Annotation an : sentence.getAnnotations(this.annotationsPrep) ){
//			Annotation preposition = chunkPrepTokens.get(an.getBegin());
//			if ( preposition == null ){
//				this.logger.warn("Prep annotation for PrepNG not found: " + an.toString());
//				continue;
//			}
//
//			Integer landmarkId = an.getBegin()+preposition.getTokens().size();
//			Integer trajectorPrevId = an.getBegin()-1;
//
//			boolean breakSearchPrev = false;
//			while ( !breakSearchPrev && trajectorPrevId >=0 && mapTokenIdToAnnotations.get(trajectorPrevId) == null ){
//				/* Przecinek i nawias zamykający przerywają poszykiwanie */
//				String orth = sentence.getTokens().get(trajectorPrevId).getOrth();
//				if ( orth.equals(",") || orth.equals(")") ){
//					breakSearchPrev = true;
//				}
//				trajectorPrevId--;
//			}
//			Integer trajectorId = trajectorPrevId;
//
//			if ( chunkNpTokens.get(landmarkId) != null && !breakSearchPrev && chunkNpTokens.get(trajectorPrevId) == chunkNpTokens.get(landmarkId) ){
//				String type = "";
//				if ( trajectorPrevId+1 == preposition.getBegin() ){
//					type = "<NG|PrepNG>";
//				}
//				else{
//					type = "<NG|...|PrepNG>";
//				}
//				List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
//				List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
//				relations.addAll(this.generateAllCombinations(type, trajectors, landmarks, preposition));
//			}
//		}
//		return relations;
//	}
	
	/**
	 * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
	 * @param sentence
	 */
//	public List<SpatialExpression> findCandidatesNgPrepNgNoNp(Sentence sentence,
//			Map<Integer, List<Annotation>> mapTokenIdToAnnotations,
//			Map<Integer, Annotation> chunkNpTokens,
//			Map<Integer, Annotation> chunkPrepTokens){
//		List<SpatialExpression> relations = new LinkedList<SpatialExpression>();
//		/* Szukaj wzorców NG* prep NG* */
//		for ( Annotation an : sentence.getAnnotations(this.annotationsPrep) ){
//			Annotation preposition = chunkPrepTokens.get(an.getBegin());
//			if ( preposition == null ){
//				this.logger.warn("Prep annotation for PrepNG not found: " + an.toString());
//				continue;
//			}
//
//			Integer landmarkId = an.getBegin()+preposition.getTokens().size();
//			Integer trajectorId = an.getBegin()-1;
//
//			if ( chunkNpTokens.get(landmarkId) == null
//					&& chunkNpTokens.get(trajectorId) == null ){
//				String type = "NG|PrepNG";
//				List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
//				List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
//				if ( trajectors != null ){
//					relations.addAll(this.generateAllCombinations(type, trajectors, landmarks, preposition));
//				}
//			}
//		}
//		return relations;
//	}

	/**
	 * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
	 * @param sentence
	 */
//	public List<SpatialExpression> findCandidatesNgPrepNgDiffNp(Sentence sentence,
//			Map<Integer, List<Annotation>> mapTokenIdToAnnotations,
//			Map<Integer, Annotation> chunkNpTokens,
//			Map<Integer, Annotation> chunkPrepTokens){
//		List<SpatialExpression> relations = new LinkedList<SpatialExpression>();
//		/* Szukaj wzorców NG* prep NG* */
//		for ( Annotation an : sentence.getAnnotations(this.annotationsPrep) ){
//			Annotation preposition = chunkPrepTokens.get(an.getBegin());
//			if ( preposition == null ){
//				this.logger.warn("Prep annotation for PrepNG not found: " + an.toString());
//				continue;
//			}
//
//			Integer landmarkId = an.getBegin()+preposition.getTokens().size();
//			Integer trajectorId = an.getBegin()-1;
//
//			if ( chunkNpTokens.get(landmarkId) != null
//					&& chunkNpTokens.get(trajectorId) != null
//					&& chunkNpTokens.get(trajectorId) != chunkNpTokens.get(landmarkId) ){
//				String type = "<NG><PrepNG>";
//				List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
//				List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
//				if ( trajectors != null ){
//					relations.addAll(this.generateAllCombinations(type, trajectors, landmarks, preposition));
//				}
//			}
//		}
//		return relations;
//	}

	/**
	 * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
	 * @param sentence
	 */
//	public List<SpatialExpression> findCandidatesPrepNgNgDiffNp(Sentence sentence,
//			Map<Integer, List<Annotation>> mapTokenIdToAnnotations,
//			Map<Integer, Annotation> chunkNpTokens,
//			Map<Integer, Annotation> chunkPrepTokens){
//		List<SpatialExpression> relations = new LinkedList<SpatialExpression>();
//		/* Szukaj wzorców NG* prep NG* */
//		for ( Annotation an : sentence.getAnnotations(this.annotationsPrep) ){
//			Annotation preposition = chunkPrepTokens.get(an.getBegin());
//			if ( preposition == null ){
//				this.logger.warn("Prep annotation for PrepNG not found: " + an.toString());
//				continue;
//			}
//
//			Integer landmarkId = an.getBegin() + preposition.getTokens().size();
//			Integer trajectorId = an.getEnd() + 1;
//
//			if ( chunkNpTokens.get(landmarkId) != null
//					&& chunkNpTokens.get(trajectorId) != null
//					&& chunkNpTokens.get(trajectorId) != chunkNpTokens.get(landmarkId) ){
//				String type = "<PrepNG><NG>";
//				List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
//				List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
//				relations.addAll(this.generateAllCombinations(type, trajectors, landmarks, preposition));
//			}
//		}
//		return relations;
//	}

	/**
	 * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
	 * @param sentence
	 */
//	public List<SpatialExpression> findCandidatesNgPrepNgPpasPrepNg(Sentence sentence,
//			Map<Integer, List<Annotation>> mapTokenIdToAnnotations,
//			Map<Integer, Annotation> chunkNpTokens,
//			Map<Integer, Annotation> chunkPrepTokens,
//			Map<Integer, Annotation> chunkPpasTokens){
//		List<SpatialExpression> relations = new LinkedList<SpatialExpression>();
//		/* Szukaj wzorców NG* prep NG* */
//		for ( Annotation an : sentence.getAnnotations(this.annotationsPrep) ){
//			Annotation preposition = chunkPrepTokens.get(an.getBegin());
//			if ( preposition == null ){
//				this.logger.warn("Prep annotation for PrepNG not found: " + an.toString());
//				continue;
//			}
//
//			Integer landmarkId = an.getBegin()+preposition.getTokens().size();
//			if ( an.getBegin()-1 <= 0 || !sentence.getTokens().get(an.getBegin()-1).getOrth().equals(",") ){
//				continue;
//			}
//
//			Annotation ppas = chunkPpasTokens.get(an.getBegin()-1);
//
//			if ( ppas == null ){
//				continue;
//			}
//
//			List<Annotation> ngs = mapTokenIdToAnnotations.get(ppas.getBegin()-1);
//			if ( ngs == null ){
//				continue;
//			}
//
//			Annotation prep = chunkPrepTokens.get(ngs.get(0).getBegin()-1);
//			if ( prep == null ){
//				continue;
//			}
//
//			Integer trajectorId = prep.getBegin()-1;
//
//			if ( chunkNpTokens.get(landmarkId) != null && chunkNpTokens.get(trajectorId) == chunkNpTokens.get(landmarkId) ){
//				String type = "<NG|PrepNG|Ppas|PrepNG>";
//				List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
//				List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
//				if ( trajectors != null ){
//					relations.addAll(this.generateAllCombinations(type, trajectors, landmarks, preposition));
//				}
//			}
//		}
//		return relations;
//	}

	/**
	 * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
	 * @param sentence
	 */
//	public List<SpatialExpression> findCandidatesNgPrepNgCommaPrepNg(Sentence sentence,
//			Map<Integer, List<Annotation>> mapTokenIdToAnnotations,
//			Map<Integer, Annotation> chunkNpTokens,
//			Map<Integer, Annotation> chunkPrepTokens){
//		List<SpatialExpression> relations = new LinkedList<SpatialExpression>();
//		/* Szukaj wzorców NG* prep NG* */
//		for ( Annotation an : sentence.getAnnotations(this.annotationsPrep) ){
//			Annotation preposition = chunkPrepTokens.get(an.getBegin());
//			if ( preposition == null ){
//				this.logger.warn("Prep annotation for PrepNG not found: " + an.toString());
//				continue;
//			}
//
//			Integer landmarkId = an.getBegin()+preposition.getTokens().size();
//			Integer commaId = preposition.getBegin()-1;
//
//			if ( commaId <= 0 || !sentence.getTokens().get(commaId).getOrth().equals(",") ){
//				continue;
//			}
//
//			Integer prepId = null;
//			List<Annotation> ngs = mapTokenIdToAnnotations.get(commaId-1);
//			if ( ngs == null ){
//				continue;
//			}
//			else{
//				for ( Annotation a : ngs ){
//					if ( prepId == null ){
//						prepId = a.getBegin()-1;
//					}
//					else{
//						prepId = Math.min(prepId, a.getBegin()-1);
//					}
//				}
//			}
//
//			Annotation prep = chunkPrepTokens.get(prepId);
//			if ( prep == null ){
//				continue;
//			}
//
//			Integer trajectorId = prep.getBegin()-1;
//
//			if ( chunkNpTokens.get(landmarkId) != null
//					&& chunkNpTokens.get(trajectorId) == chunkNpTokens.get(landmarkId) ){
//				String type = "<NG|PrepNG|Comma|PrepNG>";
//				List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
//				List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
//				if ( trajectors != null ){
//					relations.addAll(this.generateAllCombinations(type, trajectors, landmarks, preposition));
//				}
//			}
//		}
//		return relations;
//	}
	
	/**
	 * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
	 * @param sentence
	 */
//	public List<SpatialExpression> findCandidatesNgPrepNgPrepNg(Sentence sentence,
//			Map<Integer, List<Annotation>> mapTokenIdToAnnotations,
//			Map<Integer, Annotation> chunkNpTokens,
//			Map<Integer, Annotation> chunkPrepTokens){
//		List<SpatialExpression> relations = new LinkedList<SpatialExpression>();
//		/* Szukaj wzorców NG* prep NG* */
//		for ( Annotation an : sentence.getAnnotations(this.annotationsPrep) ){
//			Annotation preposition = chunkPrepTokens.get(an.getBegin());
//			if ( preposition == null ){
//				this.logger.warn("Prep annotation for PrepNG not found: " + an.toString());
//				continue;
//			}
//
//			Integer landmarkId = an.getBegin()+preposition.getTokens().size();
//
//			List<Annotation> ngs = mapTokenIdToAnnotations.get(an.getBegin()-1);
//			Integer prepId = null;
//			if ( ngs == null ){
//				continue;
//			}
//			else{
//				for ( Annotation a : ngs ){
//					if ( prepId == null ){
//						prepId = a.getBegin()-1;
//					}
//					else{
//						prepId = Math.min(prepId, a.getBegin()-1);
//					}
//				}
//			}
//
//			Annotation prep = chunkPrepTokens.get(prepId);
//			if ( prep == null ){
//				continue;
//			}
//
//			Integer trajectorId = prep.getBegin()-1;
//
//			if ( chunkNpTokens.get(landmarkId) != null
//					&& chunkNpTokens.get(trajectorId) == chunkNpTokens.get(landmarkId) ){
//				String type = "<NG|PrepNG|PrepNG>";
//				List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
//				List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
//				relations.addAll(this.generateAllCombinations(type, trajectors, landmarks, preposition));
//			}
//		}
//		return relations;
//	}
	
	/**
	 * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
	 * @param sentence
	 */
//	public List<SpatialExpression> findCandidatesNgNgPrepNg(Sentence sentence,
//			Map<Integer, List<Annotation>> mapTokenIdToAnnotations,
//			Map<Integer, Annotation> chunkNpTokens,
//			Map<Integer, Annotation> chunkPrepTokens){
//		List<SpatialExpression> relations = new LinkedList<SpatialExpression>();
//		/* Szukaj wzorców NG* prep NG* */
//		for ( Annotation an : sentence.getAnnotations(this.annotationsPrep) ){
//			Annotation preposition = chunkPrepTokens.get(an.getBegin());
//			if ( preposition == null ){
//				this.logger.warn("Prep annotation for PrepNG not found: " + an.toString());
//				continue;
//			}
//
//			Integer landmarkId = an.getBegin()+preposition.getTokens().size();
//
//			List<Annotation> ngs = mapTokenIdToAnnotations.get(preposition.getBegin()-1);
//			Integer trajectorId = null;
//			if ( ngs == null ){
//				continue;
//			}
//			else{
//				for ( Annotation a : ngs ){
//					if ( trajectorId == null ){
//						trajectorId = a.getBegin()-1;
//					}
//					else{
//						trajectorId = Math.min(trajectorId, a.getBegin()-1);
//					}
//				}
//			}
//
//			if ( chunkNpTokens.get(landmarkId) != null && chunkNpTokens.get(trajectorId) == chunkNpTokens.get(landmarkId) ){
//				String type = "<NG|NG|PrepNG>";
//				List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
//				List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
//				if ( trajectors != null ){
//					relations.addAll(this.generateAllCombinations(type, trajectors, landmarks, preposition));
//				}
//			}
//		}
//		return relations;
//	}

	/**
	 * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
	 * @param sentence
	 */
//	public List<SpatialExpression> findCandidatesFirstNgAnyPrepNg(Sentence sentence,
//			Map<Integer, List<Annotation>> mapTokenIdToAnnotations,
//			Map<Integer, Annotation> chunkNpTokens,
//			Map<Integer, Annotation> chunkPrepTokens){
//		List<SpatialExpression> relations = new LinkedList<SpatialExpression>();
//		/* Szukaj wzorców NG* prep NG* */
//		for ( Annotation an : sentence.getAnnotations(this.annotationsPrep) ){
//			Annotation preposition = chunkPrepTokens.get(an.getBegin());
//			if ( preposition == null ){
//				this.logger.warn("Prep annotation for PrepNG not found: " + an.toString());
//				continue;
//			}
//
//			Integer landmarkId = an.getBegin()+preposition.getTokens().size();
//			Integer trajectorId = an.getBegin()-1;
//
//			boolean breakSearchPrev = false;
//			while ( !breakSearchPrev && trajectorId >=0 && mapTokenIdToAnnotations.get(trajectorId) == null ){
//				/* Przecinek i nawias zamykający przerywają poszykiwanie */
//				String orth = sentence.getTokens().get(trajectorId).getOrth();
//				if ( orth.equals(",") || orth.equals(")") ){
//					breakSearchPrev = true;
//				}
//				trajectorId--;
//			}
//
//			if ( chunkNpTokens.get(landmarkId) != null && !breakSearchPrev && chunkNpTokens.get(trajectorId) == chunkNpTokens.get(landmarkId) ){
//
//				while ( trajectorId > 0
//						&& mapTokenIdToAnnotations.get(trajectorId-1) != null
//						&& chunkNpTokens.get(trajectorId-1) == chunkNpTokens.get(landmarkId)
//						){
//					trajectorId = mapTokenIdToAnnotations.get(trajectorId-1).get(0).getBegin();
//				}
//
//				String type = "";
//				if ( trajectorId+1 == preposition.getBegin() ){
//					type = "<FirstNG|PrepNG>";
//				}
//				else{
//					type = "<FirstNG|...|PrepNG>";
//				}
//
//				List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
//				List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
//				relations.addAll(this.generateAllCombinations(type, trajectors, landmarks, preposition));
//			}
//		}
//		return relations;
//	}
	
	/**
	 * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
	 * @param sentence
	 */
//	public List<SpatialExpression> findCandidatesPrepNgNg(Sentence sentence,
//			Map<Integer, List<Annotation>> mapTokenIdToAnnotations,
//			Map<Integer, Annotation> chunkNpTokens,
//			Map<Integer, Annotation> chunkPrepTokens){
//		List<SpatialExpression> relations = new LinkedList<SpatialExpression>();
//		/* Szukaj wzorców prep NG* NG* */
//		for ( Annotation an : sentence.getAnnotations(this.annotationsPrep) ){
//			Annotation preposition = chunkPrepTokens.get(an.getBegin());
//			if ( preposition == null ){
//				this.logger.warn("Prep annotation for PrepNG not found: " + an.toString());
//				continue;
//			}
//
//			Integer landmarkId = an.getBegin()+preposition.getTokens().size();
//			Integer trajectorId = an.getEnd()+1;
//
//			if ( chunkNpTokens.get(landmarkId) != null
//					&& chunkNpTokens.get(landmarkId) == chunkNpTokens.get(trajectorId) ){
//				String type = "<PrepNG|NG>";
//				List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
//				List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
//				relations.addAll(this.generateAllCombinations(type, trajectors, landmarks, preposition));
//			}
//		}
//		return relations;
//	}

	/**
	 * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
	 * @param sentence
	 */
//	public List<SpatialExpression> findCandidatesNgPrepNg(Sentence sentence,
//			Map<Integer, List<Annotation>> mapTokenIdToAnnotations,
//			Map<Integer, Annotation> chunkNpTokens,
//			Map<Integer, Annotation> chunkPrepTokens){
//		List<SpatialExpression> relations = new LinkedList<SpatialExpression>();
//		/* Szukaj wzorców prep NG* NG* */
//		for ( Annotation landmark : sentence.getAnnotations(this.annotationsNg) ){
//
//			Integer prepId = landmark.getBegin()-1;
//			if ( prepId <= 0
//					|| !sentence.getTokens().get(prepId).getDisambTag().equals("prep")
//					|| chunkPrepTokens.get(prepId) != null ){
//				continue;
//			}
//
//			Integer landmarkId = landmark.getBegin();
//			Integer trajectorId = prepId-1;
//
//			if ( chunkNpTokens.get(landmarkId) != null
//					&& chunkNpTokens.get(landmarkId) == chunkNpTokens.get(trajectorId) ){
//				String type = "<NG|prep|NG>";
//				List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
//				List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
//				relations.addAll(this.generateAllCombinations(type, trajectors, landmarks,
//						new Annotation(prepId, "Prep", sentence)));
//			}
//		}
//		return relations;
//	}

	/**
	 * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
	 * @param sentence
	 */
//	public List<SpatialExpression> findCandidatesPrepNgVerbfinNg(Sentence sentence,
//			Map<Integer, List<Annotation>> mapTokenIdToAnnotations,
//			Map<Integer, Annotation> chunkVerbfinTokens,
//			Map<Integer, Annotation> chunkPrepTokens){
//		List<SpatialExpression> relations = new LinkedList<SpatialExpression>();
//		/* Szukaj wzorców prep NG* NG* */
//		for ( Annotation an : sentence.getAnnotations(this.annotationsPrep) ){
//			Annotation preposition = chunkPrepTokens.get(an.getBegin());
//			if ( preposition == null ){
//				this.logger.warn("Prep annotation for PrepNG not found: " + an.toString());
//				continue;
//			}
//
//			Integer landmarkId = an.getBegin()+preposition.getTokens().size();
//			Integer verbfinId = an.getEnd()+1;
//			Annotation verbfin = chunkVerbfinTokens.get(verbfinId);
//
//			if ( verbfin != null ){
//				Integer trajectorId = verbfin.getEnd()+1;
//				String type = "PrepNG|Verbfin|NG";
//				List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
//				List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
//				relations.addAll(this.generateAllCombinations(type, trajectors, landmarks, preposition));
//			}
//		}
//		return relations;
//	}
	
	/**
	 * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
	 * @param sentence
	 */
//	public List<SpatialExpression> findCandidatesNgVerbfinPrepNg(Sentence sentence,
//			Map<Integer, List<Annotation>> mapTokenIdToAnnotations,
//			Map<Integer, Annotation> chunkVerbfinTokens,
//			Map<Integer, Annotation> chunkPrepTokens){
//		List<SpatialExpression> relations = new LinkedList<SpatialExpression>();
//		/* Szukaj wzorców prep NG* NG* */
//		for ( Annotation an : sentence.getAnnotations(this.annotationsPrep) ){
//			Annotation preposition = chunkPrepTokens.get(an.getBegin());
//			if ( preposition == null ){
//				this.logger.warn("Prep annotation for PrepNG not found: " + an.toString());
//				continue;
//			}
//
//			Integer landmarkId = an.getBegin()+preposition.getTokens().size();
//			Integer verbfinId = preposition.getBegin()-1;
//			Annotation verbfin = chunkVerbfinTokens.get(verbfinId);
//
//			if ( verbfin != null ){
//				Integer trajectorId = verbfin.getBegin()-1;
//				String type = "NG|Verbfin|PrepNG";
//				List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
//				List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
//				if ( trajectors != null ){
//					relations.addAll(this.generateAllCombinations(type, trajectors, landmarks, preposition));
//				}
//			}
//		}
//		return relations;
//	}

	/**
	 * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
	 * @param sentence
	 */
//	public List<SpatialExpression> findCandidatesNgPpasPrepNg(Sentence sentence,
//			Map<Integer, List<Annotation>> mapTokenIdToAnnotations,
//			Map<Integer, Annotation> chunkNpTokens,
//			Map<Integer, Annotation> chunkPpasTokens,
//			Map<Integer, Annotation> chunkPrepTokens){
//		List<SpatialExpression> relations = new LinkedList<SpatialExpression>();
//		/* Szukaj wzorców prep NG* NG* */
//		for ( Annotation an : sentence.getAnnotations(this.annotationsPrep) ){
//			Annotation preposition = chunkPrepTokens.get(an.getBegin());
//			if ( preposition == null ){
//				this.logger.warn("Prep annotation for PrepNG not found: " + an.toString());
//				continue;
//			}
//
//			Integer landmarkId = an.getBegin()+preposition.getTokens().size();
//			Integer verbfinId = preposition.getBegin()-1;
//			Annotation ppas = chunkPpasTokens.get(verbfinId);
//			if ( ppas == null ){
//				continue;
//			}
//			Integer trajectorId = ppas.getBegin()-1;
//
//			if ( ppas != null && chunkNpTokens.get(landmarkId) != null
//					&& chunkNpTokens.get(landmarkId) == chunkNpTokens.get(trajectorId) ){
//				String type = "<NG|Ppas|PrepNG>";
//				List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
//				List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
//				if ( trajectors != null ){
//					relations.addAll(this.generateAllCombinations(type, trajectors, landmarks, preposition));
//				}
//			}
//		}
//		return relations;
//	}
	
	/**
	 * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
	 * @param sentence
	 */
//	public List<SpatialExpression> findCandidatesNgPactPrepNg(Sentence sentence,
//			Map<Integer, List<Annotation>> mapTokenIdToAnnotations,
//			Map<Integer, Annotation> chunkNpTokens,
//			Map<Integer, Annotation> chunkPactTokens,
//			Map<Integer, Annotation> chunkPrepTokens){
//		List<SpatialExpression> relations = new LinkedList<SpatialExpression>();
//		/* Szukaj wzorców prep NG* NG* */
//		for ( Annotation an : sentence.getAnnotations(this.annotationsPrep) ){
//			Annotation preposition = chunkPrepTokens.get(an.getBegin());
//			if ( preposition == null ){
//				this.logger.warn("Prep annotation for PrepNG not found: " + an.toString());
//				continue;
//			}
//
//			Integer landmarkId = an.getBegin()+preposition.getTokens().size();
//			Integer verbfinId = preposition.getBegin()-1;
//			Annotation pact = chunkPactTokens.get(verbfinId);
//			if ( pact == null ){
//				continue;
//			}
//			Integer trajectorId = pact.getBegin()-1;
//
//			if ( pact != null && chunkNpTokens.get(landmarkId) != null
//					&& chunkNpTokens.get(landmarkId) == chunkNpTokens.get(trajectorId) ){
//				String type = "<NG|Pact|PrepNG>";
//				List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
//				List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
//				if ( trajectors != null ){
//					relations.addAll(this.generateAllCombinations(type, trajectors, landmarks, preposition));
//				}
//			}
//		}
//		return relations;
//	}

	/**
	 * 
	 * @param sentence
	 * @param maltSentence
	 * @return
	 */
	public List<SpatialExpression> findCandidatesByMalt(Sentence sentence, MaltSentence maltSentence,
			Map<Integer, Annotation> chunkPrepTokens, Map<Integer, Annotation> chunkNamesTokens, Map<Integer, List<Annotation>> mapTokenIdToAnnotations){
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
						/* Ustal spatial indicator */
						Annotation si = chunkPrepTokens.get(indicator);
						if ( si == null ){
							si = new Annotation(indicator, "SI", sentence);
							sentence.addChunk(si);
						}
						
						/* Ustal trajector */
						Annotation tr = null;
						List<Annotation> trs = mapTokenIdToAnnotations.get(trajector);
						if ( trs != null ){
							tr = trs.get(0);
						}
						if ( tr == null ){
							tr = new Annotation(trajector, "TR", sentence);
							sentence.addChunk(tr);
						}
						
						/* Ustal landmark */
						Annotation lm = null;
						List<Annotation> lms = mapTokenIdToAnnotations.get(landmark);
						if ( lms != null ){
							lm = lms.get(0);
						}
						if ( lm == null ){
							lm = new Annotation(landmark, "LM", sentence);
							sentence.addChunk(lm);
						}
						
						srs.add(new SpatialExpression(type + typeLM + typeTR, tr, si, lm));
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
