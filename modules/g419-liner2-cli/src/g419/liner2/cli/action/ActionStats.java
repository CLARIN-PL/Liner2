package g419.liner2.cli.action;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.lib.cli.CommonOptions;
import g419.lib.cli.action.Action;
import g419.liner2.api.LinerOptions;
import g419.liner2.api.chunker.factory.ChunkerManager;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.ParseException;

/**
 * Train chunkers.
 * @author Michał Marcińczuk
 *
 */
public class ActionStats extends Action{

    private String input_file = null;
    private String input_format = null;

	public ActionStats(){
		super("stats");
        this.setDescription("prints corpus statistics");
        this.options.addOption(CommonOptions.getInputFileFormatOption());
        this.options.addOption(CommonOptions.getInputFileNameOption());
	}

	@Override
	public void parseOptions(String[] args) throws ParseException {
        CommandLine line = new GnuParser().parse(this.options, args);
        parseDefault(line);
        this.input_file = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
        this.input_format = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT, "ccl");
	}
	
	/**
	 * Module entry function.
	 * 
	 * Loads annotation recognizers.
	 */
	public void run() throws Exception{
		AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(this.input_file, this.input_format);
		SortedMap<String, TreeSet<String>> groups = new TreeMap<String, TreeSet<String>>();
		Document document = null;
		HashMap<String, LinkedList<Annotation>> annotationsByRange = new HashMap<String, LinkedList<Annotation>>();
		while ( (document = reader.nextDocument()) != null ){
			
			for ( Sentence sentence : document.getSentences() ){
				annotationsByRange.clear();
				for ( Annotation ann : sentence.getChunks() ){
					String range = String.format("%d:%d", ann.getBegin(), ann.getEnd());
					if ( annotationsByRange.containsKey(range))
						annotationsByRange.get(range).add(ann);
					else{
						LinkedList<Annotation> list = new LinkedList<Annotation>();
						list.add(ann);
						annotationsByRange.put(range, list);
					}
				}
				for ( LinkedList<Annotation> list : annotationsByRange.values() )
					if ( list.size() > 1 ){
						SortedSet<String> types = new TreeSet<String>();
						for ( Annotation ann : list ){
							if ( !types.contains(ann.getType()) )
								types.add(ann.getType());
						}
						StringBuilder typessb = new StringBuilder();
						for ( String type : types){
							if (typessb.length() > 0) typessb.append("#");
							typessb.append(type);
						}
						String typesStr = typessb.toString(); 
						
						if ( groups.containsKey(typesStr) ){
							if ( !groups.get(typesStr).contains(typesStr) )
								groups.get(typesStr).add(list.get(0).getText());
						}
						else{
							TreeSet<String> texts = new TreeSet<String>();
							texts.add(list.get(0).getText());
							groups.put(typesStr, texts);
						}
					}
			}			
		}
		
		for ( String type : groups.keySet() ){
			System.out.println(type);
			for ( String text : groups.get(type))
				System.out.println("  " + text);
		}
	}
		
}
