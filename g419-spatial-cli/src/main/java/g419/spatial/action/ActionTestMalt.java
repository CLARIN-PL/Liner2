package g419.spatial.action;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import g419.corpus.schema.tagset.MappingNkjpToConllPos;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.log4j.Logger;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.liner2.core.features.tokens.ClassFeature;
import g419.liner2.core.tools.parser.MaltParser;
import g419.liner2.core.tools.parser.MaltSentence;
import g419.liner2.core.tools.parser.MaltSentenceLink;
import g419.spatial.structure.SpatialExpression;

public class ActionTestMalt extends Action {
	
	private final static String OPTION_FILENAME_LONG = "filename";
	private final static String OPTION_FILENAME = "f";
	
	private String filename = null;
	private String inputFormat = null;

	public ActionTestMalt() {
		super("test-malt");
		this.setDescription("recognize spatial relations");
		this.options.addOption(this.getOptionInputFilename());		
		this.options.addOption(CommonOptions.getInputFileFormatOption());
	}
	
	/**
	 * Create Option object for input file name.
	 * @return Object for input file name parameter.
	 */
	private Option getOptionInputFilename(){
		return Option.builder(OPTION_FILENAME).longOpt(OPTION_FILENAME_LONG)
				.hasArg().argName("filename").required().desc("path to the input file").build();
	}

	/**
	 * Parse action options
	 * @param arg0 The array with command line parameters
	 */
	@Override
	public void parseOptions(final CommandLine line) throws Exception {
        this.filename = line.getOptionValue(ActionTestMalt.OPTION_FILENAME);
        this.inputFormat = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT);
    }

	@Override
	public void run() throws Exception {
		AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(this.filename, this.inputFormat);
		Document document = reader.nextDocument();
		
		MaltParser malt = new MaltParser("/nlp/resources/maltparser/skladnica_liblinear_stackeager_final.mco");
				
		while ( document != null ){					
			Logger.getLogger(this.getClass()).info("\nDocument: " + document.getName());
			
			for (Paragraph paragraph : document.getParagraphs()){
				for (Sentence sentence : paragraph.getSentences()){
					MaltSentence maltSentence = new MaltSentence(sentence, MappingNkjpToConllPos.get());
					malt.parse(maltSentence);
					
					List<SpatialExpression> srs = this.findByMalt(sentence, maltSentence);
					for ( SpatialExpression sr : srs){
						System.out.println(sr.toString());
					}
				}
			}
			document = reader.nextDocument();
		}
			
		reader.close();
	}
		
	/**
	 * 
	 * @param sentence
	 * @param maltSentence
	 * @return
	 */
	public List<SpatialExpression> findByMalt(Sentence sentence, MaltSentence maltSentence){
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
						SpatialExpression sr = new SpatialExpression(
								type + typeLM + typeTR, 
								new Annotation(trajector, "trajector", sentence), 
								new Annotation(indicator, "indicator", sentence), 
								new Annotation(landmark, "landmark", sentence));
						srs.add(sr);
					}
				}
			}
		}
		return srs;
	}

}
