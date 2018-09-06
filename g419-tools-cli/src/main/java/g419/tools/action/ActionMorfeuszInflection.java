package g419.tools.action;

import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import g419.lib.cli.Action;
import g419.tools.utils.Counter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class ActionMorfeuszInflection extends Action {
	
	private final String OPTION_MORFEUSZ_LONG = "morfeusz";
	private final String OPTION_MORFEUSZ = "m";
	
	private String morfeusz = null;
	
	public ActionMorfeuszInflection() {
		super("morfeusz-inflection");
		this.setDescription("tworzy słownik alternacji końcówek dla formy odmienionej i formy bazowej");
        this.options.addOption(Option.builder(OPTION_MORFEUSZ).longOpt(OPTION_MORFEUSZ_LONG).hasArg().argName("filename")
        		.desc("ścieżka do słownika Morfeusz").required().required().build());
	}
	
	/**
	 * Parse action options
	 * @param line The array with command line parameters
	 */
	@Override
	public void parseOptions(final CommandLine line) throws Exception {
        this.morfeusz = line.getOptionValue(OPTION_MORFEUSZ_LONG);
    }

	@Override
	public void run() throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(this.morfeusz)));
		boolean morph = true;
		String line = null;
		Map<String, Counter> inflections = new HashMap<String, Counter>();
		while ( (line = br.readLine()) != null ){
			line = line.trim();
			
			if ( line.contains("<COPYRIGHT>") ){
				morph = false;
			}
			
			if ( line.startsWith("#") ){
				// Pomiń komentarze
			}
			else if ( morph ){
				String[] cols = line.split("\t");
				String form = cols[0];
				String base = cols[1].split(":")[0];
				String ctag = cols[2].trim();
				if ( !form.equals(base) ){
					int i=0;
					while ( i< form.length() && i < base.length() && form.charAt(i) == base.charAt(i) ){
						i++;
					}
					if ( i > 1 ){
						String template = form.substring(i) + "#" + base.substring(i);
						
						template = ctag + "#" + template;
						
						Counter counter = inflections.get(template);
						if ( counter == null ){
							counter = new Counter();
							inflections.put(template, counter);
						}
						counter.increment();
					}
				}
			}
			else if ( line.equals("#</COPYRIGHT>")){
				morph = true;
			}
		}
		br.close();
		
		Set<String> patterns = new TreeSet<String>();
		patterns.addAll(inflections.keySet());
		for ( String pattern : patterns ){
			if ( inflections.get(pattern).getValue() > 1 )
				System.out.println(String.format("%s %d", pattern, inflections.get(pattern).getValue()));
		}
		
	}

	/**
	 * 
	 * @param sentence
	 * @param query
	 * @return
	 */
	private String sentenceWithHighlightToString(Sentence sentence, String query){
		StringBuilder sb = new StringBuilder();
		int matched = 0;
		boolean bracketOpen = false;
		for ( int i=0; i<sentence.getTokenNumber(); i++ ){
			Token token = sentence.getTokens().get(i);
			if ( matched == 0 ){
				matched = this.matches(sentence, i, query);
				if ( matched > 0 ){
					sb.append("[");
					bracketOpen = true;
					matched--;
				}
			}
			sb.append(token.getOrth());
			if ( matched == 0 && bracketOpen ){
				sb.append("]");
				bracketOpen = false;
			}
			if ( token.getNoSpaceAfter() == false ){
				sb.append(" ");
			}
		}
		return sb.toString();
	}
	
	/**
	 * 
	 * @param sentence
	 * @param position
	 * @param query
	 * @return
	 */
	private int matches(Sentence sentence, int position, String query){
		Token token = sentence.getTokens().get(position);
		if ( token.getOrth().toLowerCase().startsWith(query) ){
			return 1;
		}
		else{
			return 0;
		}
	}
}
