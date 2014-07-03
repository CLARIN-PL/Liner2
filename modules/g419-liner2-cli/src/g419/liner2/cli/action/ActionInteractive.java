package g419.liner2.cli.action;


import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.io.writer.AbstractDocumentWriter;
import g419.corpus.io.writer.WriterFactory;
import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import g419.liner2.api.LinerOptions;
import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.factory.ChunkerFactory;
import g419.liner2.api.chunker.factory.ChunkerManager;
import g419.liner2.api.features.TokenFeatureGenerator;
import g419.liner2.api.tools.ParameterException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

/**
 * Chunking in interactive mode.
 * @author Maciej Janicki, Michał Marcińczuk
 *
 */
public class ActionInteractive extends Action{

    /**
	 * Module entry function.
	 */

    private static HashSet<String> validInputFormats = new HashSet<String>();
    private static HashSet<String> validOutputFormats = new HashSet<String>();

    static{
        validInputFormats.add("plain");
        validInputFormats.add("plain:maca");
        validInputFormats.add("plain:wcrft");

        validOutputFormats.add("ccl");
        validOutputFormats.add("iob");
        validOutputFormats.add("tuples");
        validOutputFormats.add("tokens");
    }


	public void run() throws Exception {
        LinerOptions.getGlobal().setDefaultDataFormats("plain:maca", "ccl");
        
        if ( !LinerOptions.isOption(LinerOptions.OPTION_USE) ){
			throw new ParameterException("Parameter --use <chunker_pipe_desription> not set");
		}

        TokenFeatureGenerator gen = null;
        String inputFormat = LinerOptions.getGlobal().getOption(LinerOptions.OPTION_INPUT_FORMAT);
        if(!validInputFormats.contains(inputFormat)){
            throw new ParameterException("Input format " + inputFormat + " is not valid for interactive mode. Use one of: " + validInputFormats);
        }
        String outputFormat = LinerOptions.getGlobal().getOption(LinerOptions.OPTION_OUTPUT_FORMAT);
        if(!validOutputFormats.contains(outputFormat)){
            throw new ParameterException("Output format " + outputFormat + " is not valid for interactive mode. Use one of: " + validOutputFormats);
        }
        AbstractDocumentWriter writer = WriterFactory.get().getStreamWriter(System.out, outputFormat);
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String cSeq = "";
		
		if (!LinerOptions.getGlobal().silent){
			System.out.println("# Loading, please wait...");
		}
        ChunkerManager cm = ChunkerFactory.loadChunkers(LinerOptions.getGlobal());
        Chunker chunker = cm.getChunkerByName(LinerOptions.getGlobal().getOptionUse());
		
        if (!LinerOptions.getGlobal().features.isEmpty()){
            gen = new TokenFeatureGenerator(LinerOptions.getGlobal().features);
        }

		if (!LinerOptions.getGlobal().silent){
			System.out.println("# Enter a sentence and press Enter.");
			System.out.println("# Input format: " + inputFormat);
			System.out.println("# To finish, enter 'EOF'.");
        }
		
		do {
	        if (!LinerOptions.getGlobal().silent)
	        	System.out.print("\n> ");
			
			// Get line of text to process
			cSeq = in.readLine();

			// If the text is not EndOfFile then process it
			if (!cSeq.equals("EOF")) {

				// if empty line -- continue
				if (Pattern.matches("^\\s*$", cSeq))
					continue;

				// force treating everything as one sentence? -- [SENTENCE] prefix
				boolean forceSentence = false;
				Pattern pSentence = Pattern.compile("^\\s*\\[SENTENCE\\]\\s*(.*)$");
				Matcher mSentence = pSentence.matcher(cSeq);
				if (mSentence.matches()) {
					cSeq = mSentence.group(1);
					forceSentence = true;
				}

				// morphological analysis, feature generation
				AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(
						"terminal input", 
						IOUtils.toInputStream(cSeq), 
						inputFormat);
				Document ps = reader.nextDocument();
				reader.close();

				if (forceSentence)
					ps = mergeSentences(ps);
				
				if ( gen != null )
					gen.generateFeatures(ps);
                
				chunker.chunkInPlace(ps);

				// write output
                writer.writeDocument(ps);
                writer.flush();
			}
        } while (!cSeq.equals("EOF"));
		writer.close();
	}
	

	/**
	 * Merge all tokens into a single sentence.	
	 * @param paragraph
	 * @return
	 */
	private Document mergeSentences(Document paragraph) {
		Sentence merged = new Sentence();
		merged.setAttributeIndex(paragraph.getAttributeIndex());

		for (Sentence sentence : paragraph.getSentences())
			for (Token token : sentence.getTokens())
				merged.addToken(token);

		Document document = new Document("interactive mode", paragraph.getAttributeIndex());
		Paragraph resultParagraph = new Paragraph("id1");		
		resultParagraph.addSentence(merged);
		document.addParagraph(resultParagraph);

		return document;
	}
}
