package g419.spatial.action;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.schema.kpwr.KpwrSpatial;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.spatial.tools.SpatialResources;

public class ActionTestSpejd extends Action {
	
	private String inputFilename = null;
	private String inputFormat = null;
	private Pattern annotationsNg = Pattern.compile("^(NG|NumG).*$");
	private Pattern annotationsPrep = Pattern.compile("^PrepN.*$");
	private Set<String> regions = SpatialResources.getRegions();

	public ActionTestSpejd() {
		super("test-spejd");
		this.setDescription("wypisuje frazy NG zawierające TR lub LM");
		this.options.addOption(CommonOptions.getInputFileNameOption());
		this.options.addOption(CommonOptions.getInputFileFormatOption());		
	}
	
	/**
	 * Parse action options
	 * @param arg0 The array with command line parameters
	 */
	@Override
	public void parseOptions(String[] args) throws Exception {
        CommandLine line = new DefaultParser().parse(this.options, args);
        parseDefault(line);
        this.inputFilename = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
        this.inputFormat = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT);        
    }

	@Override
	public void run() throws Exception {
        AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(inputFilename, inputFormat);
        Document document = null;
        
        while ( (document = reader.nextDocument()) != null ){
        	for ( Sentence sentence : document.getSentences()){
        		this.splitPrepNg(sentence);
        		this.moveHeadForRegions(sentence);
        		/* Zaindeksuj pierwsze tokeny anotacji NG* */
        		Map<Integer, List<Annotation>> chunkNgTokens = new HashMap<Integer, List<Annotation>>();
        		for ( Annotation an : sentence.getAnnotations(this.annotationsNg) ){
        			for ( Integer n = an.getBegin(); n <= an.getEnd(); n++){
        				if ( !chunkNgTokens.containsKey(n) ){
        					chunkNgTokens.put(n, new LinkedList<Annotation>());
        				}
        				chunkNgTokens.get(n).add(an);
        			}
        		}        		
            	for ( Annotation an : sentence.getChunks() ){
            		if ( an.getType().equals(KpwrSpatial.SPATIAL_ANNOTATION_SPATIAL_OBJECT) ){
            			List<Annotation> ngs = chunkNgTokens.get(an.getBegin());
            			String object = an.toString();
            			String head = "nonhead";
            			String ng = "nong";
            			String ngstr = "";
            			if ( ngs != null ){
            				ng = "inng";
            				if ( ngs.get(0).getHead() == an.getBegin() ){
            					head = "ishead";
            				}
            				ngstr = ngs.toString();
            			}
            			System.out.println(
            					String.format("%s\t%s\t%s\t%s\t%s", document.getName(), object, ng, head, ngstr));
            		}
            	}
        	}
        }
	}
	
	/**
	 * Wydziela z anotacji PrepNG* anotacje zagnieżdżone poprzez odcięcie przymika.
	 * @param sentence
	 */
	public void splitPrepNg(Sentence sentence){
		/* Zaindeksuj tokeny anotacji NG* */
		Map<Integer, List<Annotation>> mapTokenIdToAnnotations = new HashMap<Integer, List<Annotation>>();
		for ( Annotation an : sentence.getAnnotations(this.annotationsNg) ){
			for ( int i = an.getBegin(); i<=an.getEnd(); i++ ){
				if ( !mapTokenIdToAnnotations.containsKey(i) ){
					mapTokenIdToAnnotations.put(i, new LinkedList<Annotation>());
				}
				mapTokenIdToAnnotations.get(i).add(an);
			}
		}
		
		for ( Annotation an : sentence.getAnnotations(this.annotationsPrep) ){
			if ( !mapTokenIdToAnnotations.containsKey(an.getBegin()+1) ){
				Annotation ani = new Annotation(an.getBegin()+1, an.getEnd(), an.getType().substring(4), an.getSentence());
				ani.setHead(an.getHead());
				sentence.addChunk(ani);
			}
			else{
				Integer newNgStart = null;
				for ( int i=an.getBegin() + 1; i <= an.getEnd(); i++ ){
					if ( mapTokenIdToAnnotations.get(i) == null ){
						if ( newNgStart == null ){
							newNgStart = i;
						}
					}
					else{
						if ( newNgStart != null ){
							Annotation newNg = new Annotation(newNgStart, i-1, "NG", sentence);
							sentence.addChunk(newNg);
							newNgStart = null;
						}
					}
				}
				if ( newNgStart != null ) {
					Annotation newNg = new Annotation(newNgStart, an.getEnd(), "NG", sentence);
					sentence.addChunk(newNg);
					newNgStart = null;				
				}
			}
		}
	}
	
	/**
	 * Dla anotacji, który głową jest region, zmień głowę na element podrzędny
	 * @param sentence
	 */
	public void moveHeadForRegions(Sentence sentence){
		/* Zaindeksuj tokeny anotacji NG* */
		Map<Integer, List<Annotation>> mapTokenIdToAnnotations = new HashMap<Integer, List<Annotation>>();
		for ( Annotation an : sentence.getAnnotations(this.annotationsNg) ){
			for ( int i = an.getBegin(); i<=an.getEnd(); i++ ){
				if ( !mapTokenIdToAnnotations.containsKey(i) ){
					mapTokenIdToAnnotations.put(i, new LinkedList<Annotation>());
				}
				mapTokenIdToAnnotations.get(i).add(an);
			}
		}
		
		for ( Annotation an : sentence.getAnnotations(this.annotationsNg) ){
			int i = an.getBegin();
			// Ustaw i na pierwszy token nie będący regionem
			while ( i<=an.getEnd() && this.regions.contains(sentence.getTokens().get(i).getDisambTag().getBase() ) ){
				System.err.println(sentence.getTokens().get(i).getDisambTag().getBase());
				i++;
			}			
			if ( i > an.getBegin() && i <= an.getEnd() ){
				List<Annotation> inners = mapTokenIdToAnnotations.get(i);
				boolean foundNewHead = false;
				if ( inners != null ){
					for ( Annotation inner : inners ){
						if ( inner.getBegin() == i ){
							an.setHead(inner.getHead());
							foundNewHead = true;
							break;
						}
					}
				}
				if ( !foundNewHead ){
					for ( int j=i; j<=an.getEnd() && !foundNewHead; j++){
						String pos = sentence.getTokens().get(j).getDisambTag().getPos();
						if ( "subst".equals(pos) || "ign".equals(pos) ){
							an.setHead(j);
							System.err.println("=> " + sentence.getTokens().get(j).getOrth());
							foundNewHead = true;
						}
					}
				}
//				if ( !foundNewHead ){
//					an.setHead(i);
//					System.err.println("=> " + sentence.getTokens().get(i).getOrth());
//				}
			}
		}
	}
}
