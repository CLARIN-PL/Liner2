package liner2.action;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import liner2.LinerOptions;
import liner2.chunker.Chunker;
import liner2.chunker.factory.ChunkerFactory;
import liner2.chunker.factory.ChunkerManager;
import liner2.features.TokenFeatureGenerator;
import liner2.reader.AbstractDocumentReader;
import liner2.reader.ReaderFactory;
import liner2.structure.Document;
import liner2.structure.Paragraph;
import liner2.structure.Sentence;
import liner2.structure.Token;
import liner2.tools.ParameterException;
import liner2.writer.AbstractDocumentWriter;
import liner2.writer.WriterFactory;

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
	public void run() throws Exception {
        
        if ( !LinerOptions.isOption(LinerOptions.OPTION_USE) ){
			throw new ParameterException("Parameter --use <chunker_pipe_desription> not set");
		}

        TokenFeatureGenerator gen = null;
        String inputFormat = LinerOptions.getGlobal().getOption(LinerOptions.OPTION_INPUT_FORMAT);
        String output_format = LinerOptions.getGlobal().getOption(LinerOptions.OPTION_OUTPUT_FORMAT);
        AbstractDocumentWriter writer = WriterFactory.get().getStreamWriter(System.out, output_format);
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
	        	System.out.print("> ");
			
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
