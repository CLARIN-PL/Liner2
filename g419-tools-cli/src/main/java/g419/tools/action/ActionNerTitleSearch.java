package g419.tools.action;

import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.log4j.Logger;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.schema.kpwr.KpwrNer;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;

public class ActionNerTitleSearch extends Action {
	
	private String inputFilename = null;
	private String inputFormat = null;
	
	public ActionNerTitleSearch() {
		super("ner-title-search");
		this.setDescription("wyszukuje frazy w cudzysłowach");
        this.options.addOption(CommonOptions.getInputFileFormatOption());
        this.options.addOption(CommonOptions.getInputFileNameOption());
	}
	
	/**
	 * Parse action options
	 * @param arg0 The array with command line parameters
	 */
	@Override
	public void parseOptions(String[] args) throws Exception {
        CommandLine line = new DefaultParser().parse(this.options, args);
        parseDefault(line);
        
        this.inputFilename = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE_LONG);
        this.inputFormat = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT_LONG);
    }

	@Override
	public void run() throws Exception {

		Document document = null;
		AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(this.inputFilename, this.inputFormat);
		
		QuotationFinder qfinder = new QuotationFinder();
		
		while ( ( document = reader.nextDocument() ) != null ){
			Logger.getLogger(this.getClass()).info("Document: " + document.getName());
			for ( Sentence sentence : document.getSentences() ){
				int i=0;
				while ( i < sentence.getTokenNumber() ){
					int qlen = qfinder.isQuotation(sentence, i);
					if ( qlen > 0 ){
						if ( qlen < 10 ){
							Annotation an = new Annotation(i, i+qlen-1, KpwrNer.NER_PRO_TITLE, sentence);
							String before = i > 0 ? 
									(new Annotation(Math.max(0, i-5), i-1, "x", sentence)).getText() : "";
							String after = i+qlen < sentence.getTokenNumber() ? 
									(new Annotation(i+qlen, Math.min(i+qlen+4, sentence.getTokenNumber()-1), "x", sentence)).getText() : "";
									
							System.out.println(
									String.format("TITLE: %s\t%s\t%s", before, an.getText(), after));
						}
						
						i+=qlen;
					}
					else{
						i++;
					}
				}
			}
		}		
	}
	
	/**
	 * Rozpoznaje sekwencje tekstu występujące w cudzysłowiu.
	 * @author Michał Marcińczuk
	 *
	 */
	public class QuotationFinder{

		Pattern patternQuoteSingle = Pattern.compile("^[‘’'‘’‚’]$");
		Pattern patternQuoteDouble = Pattern.compile("^[“”\"“””„““”]$");
		Pattern patternQuoteOther = Pattern.compile("^[«»]$");

		/**
		 * Sprawdza, czy poczynając od pozycji startPos w zdaniu znajduje się fragment
		 * tekstu otoczony cydzysłowami. Metoda zwraca długość takiego fragmentu.
		 * @param sentence Zdanie, w którym sprawdzane jest wystąpienie cytowania.
		 * @param startPos Indeks tokenu, od którego następuje sprawdzenie.
		 * @return 0 jeżeli od pozycji startPos nie ma cytowania, wpp. długość cytowania razem z cudzysłowami.
		 */
		public int isQuotation(Sentence sentence, int startPos){
			String orth = sentence.getTokens().get(startPos).getOrth();
			boolean singleQuote = patternQuoteSingle.matcher(orth).find();
			boolean doubleQuote = patternQuoteDouble.matcher(orth).find();
			boolean otherQuote = patternQuoteOther.matcher(orth).find();
			
			if (!singleQuote && !doubleQuote && !otherQuote){
				return 0;
			}

			boolean openSingle = singleQuote;
			boolean openDouble = doubleQuote;
			boolean openOther = otherQuote;
			int quotationEnded = -1;
			
			for ( int i = startPos+1; i<sentence.getTokens().size() && quotationEnded==-1; i++ ) {
				Token token = sentence.getTokens().get(i);
				singleQuote = patternQuoteSingle.matcher(token.getOrth()).find();
				doubleQuote = patternQuoteDouble.matcher(token.getOrth()).find();
				otherQuote = patternQuoteOther.matcher(token.getOrth()).find();
				
				if ( (openSingle && singleQuote) || (openDouble && doubleQuote) || (openOther && otherQuote) ){
					openSingle = false;
					openDouble = false;
					openOther = false;
					quotationEnded = i;
				}
			}
			
			if ( quotationEnded!=-1 ){
				return quotationEnded-startPos+1;
			}
			else{
				return 0;
			}
		}
	
		/**
		 * 
		 * @param sentence
		 * @param pos
		 * @return
		 */
		public boolean isQuotationMark(Sentence sentence, int pos){
			String orth = sentence.getTokens().get(pos).getOrth();
			boolean singleQuote = patternQuoteSingle.matcher(orth).find();
			boolean doubleQuote = patternQuoteDouble.matcher(orth).find();
			boolean otherQuote = patternQuoteOther.matcher(orth).find();
			return singleQuote || doubleQuote || otherQuote;
		}
	}

}
