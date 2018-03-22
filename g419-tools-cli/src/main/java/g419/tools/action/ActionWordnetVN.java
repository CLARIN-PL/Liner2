package g419.tools.action;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;

import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.toolbox.wordnet.WordnetXmlReader;
import g419.toolbox.wordnet.struct.LexicalRelation;
import g419.toolbox.wordnet.struct.WordnetPl;

/**
 */
public class ActionWordnetVN extends Action {

    public static final String OPTION_WORDNET = "w";
    public static final String OPTION_WORDNET_LONG = "wordnet";

    String wordnet = null;

    public ActionWordnetVN(){
        super("wordnet-vn");
        this.setDescription("ToDo");
        this.options.addOption(CommonOptions.getOutputFileNameOption());
        this.options.addOption(Option.builder(OPTION_WORDNET).longOpt(OPTION_WORDNET_LONG).hasArg().argName("wordnet").required()
        						.desc("ścieżka do pliku xml ze Słowosiecią").build());
    }

    @Override
    public void parseOptions(final CommandLine line) throws Exception {
        this.wordnet = line.getOptionValue(OPTION_WORDNET);

    }

    @Override
    public void run() throws Exception {
    	WordnetPl wordnet = WordnetXmlReader.load(this.wordnet);
    	for ( LexicalRelation relation : wordnet.getLexicalRelations("141") ){
    		System.out.println(String.format("v2n\t%s\t%s", relation.getParent().getName(), relation.getChild().getName()));
    	}
    }

}
