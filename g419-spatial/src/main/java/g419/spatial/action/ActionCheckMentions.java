package g419.spatial.action;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.log4j.Logger;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.lib.cli.CommonOptions;
import g419.lib.cli.action.Action;
import g419.liner2.api.Liner2;

public class ActionCheckMentions extends Action {
	
	private final static String OPTION_FILENAME_LONG = "filename";
	private final static String OPTION_FILENAME = "f";
	
	private List<Pattern> annotationsPrep = new LinkedList<Pattern>();
	private List<Pattern> annotationsNg = new LinkedList<Pattern>();
	
	private String filename = null;
	private String inputFormat = null;
	
	private String config_liner2_model = "/home/czuk/nlp/eclipse/workspace_liner2/models-released/liner2.5/liner25-model-pack-ibl/config-top9.ini";

	
	public ActionCheckMentions() {
		super("mentions");
		this.setDescription("generuje listę wzorców dla relacji przestrzennych");
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
		OptionBuilder.withArgName(ActionCheckMentions.OPTION_FILENAME_LONG);
		OptionBuilder.hasArg();
		OptionBuilder.isRequired();
		OptionBuilder.withDescription("path to the input file");
		OptionBuilder.withLongOpt(OPTION_FILENAME_LONG);
		return OptionBuilder.create(ActionCheckMentions.OPTION_FILENAME);			
	}

	/**
	 * Parse action options
	 * @param arg0 The array with command line parameters
	 */
	@Override
	public void parseOptions(String[] args) throws Exception {
        CommandLine line = new GnuParser().parse(this.options, args);
        parseDefault(line);
        this.filename = line.getOptionValue(ActionCheckMentions.OPTION_FILENAME);
        this.inputFormat = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT);
    }

	@Override
	public void run() throws Exception {
		AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(this.filename, this.inputFormat);	
		
		Liner2 liner2 = new Liner2(this.config_liner2_model);
		
		Document document = null;
		while ( (document = reader.nextDocument()) != null ){					
			Logger.getLogger(this.getClass()).info("\nDocument: " + document.getName());
			
			liner2.chunkInPlace(document);
			
			for ( Sentence sentence : document.getSentences() ){
				
				Map<String, Annotation> indexNG = this.makeAnnotationIndex(sentence.getAnnotations(Pattern.compile("NG")));
				Map<String, Annotation> indexNE = this.makeAnnotationIndex(sentence.getAnnotations(Pattern.compile("^nam_")));
				
				for ( Annotation an : sentence.getAnnotations(Pattern.compile("(spatial_object)", Pattern.CASE_INSENSITIVE)) ){
					String str = an.toString();
					String key = "" + sentence.hashCode() + "#" + an.getBegin();
					str += " # [pos=" + sentence.getTokens().get(an.getBegin()).getDisambTag().getPos() + "] ";
					str += " # ";
					Annotation ne = indexNE.get(key);
					Annotation ng = indexNG.get(key);
					if ( ne != null ){
						str += " " + ne.getType();
						if ( ne.getHead() == an.getBegin() ){
							str += " IS_HEAD";							
						}
						else{
							str += " NO_HEAD";
						}
						str += " # " + ne.toString();
					}
					else if ( ng != null ){
						str += " " + ng.getType();
						if ( ng.getHead() == an.getBegin() ){
							str += " IS_HEAD";							
						}
						else{
							str += " NO_HEAD";
						}
						str += " # " + ng.toString();
					}
					else{
						str += " NOT_FOUND";
					}
					
					System.out.println(str);
				}
			}

		}
			
		reader.close();
	}

	/**
	 * 
	 * @param anns
	 * @return
	 */
	public Map<String, Annotation> makeAnnotationIndex(Collection<Annotation> anns){
		Map<String, Annotation> annotationIndex = new HashMap<String, Annotation>();
		for ( Annotation an : anns ){
			for ( int i=an.getBegin(); i<=an.getEnd(); i++){
				String hash = "" + an.getSentence().hashCode() + "#" + i;
				if ( annotationIndex.get(hash) != null ) {
					if ( annotationIndex.get(hash).getTokens().size() < an.getTokens().size() ){
						annotationIndex.remove(hash);
					}
				}
				else{
					annotationIndex.put(hash, an);
				}
			}
		}
		return annotationIndex;
	}
	
}
