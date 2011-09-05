package liner2.action;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import liner2.chunker.Chunker;
import liner2.chunker.factory.ChunkerFactory;

import liner2.reader.FeatureGenerator;

import liner2.structure.AttributeIndex;
import liner2.structure.Chunk;
import liner2.structure.Chunking;
import liner2.structure.Sentence;
import liner2.structure.Token;

import liner2.tools.ParameterException;
import liner2.LinerOptions;

/**
 * Chunking in batch mode.
 * @author Maciej Janicki, Michał Marcińczuk
 *
 */
public class ActionBatch extends Action{

	/**
	 * Module entry function.
	 */
	public void run() throws Exception {

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String cSeq = "";
        
        if ( !LinerOptions.isOption(LinerOptions.OPTION_USE) ){
			throw new ParameterException("Parameter --use <chunker_pipe_desription> not set");
		}
		
		ChunkerFactory.loadChunkers(LinerOptions.get().chunkersDescription);		
		Chunker chunker = ChunkerFactory.getChunkerPipe(LinerOptions.getOption(LinerOptions.OPTION_USE));
		
		if (!LinerOptions.get().silent){
			System.out.println("# Enter a sentence and press Enter.");
			System.out.println("#   Tokens should be seperated with double spaces.");
			System.out.println("#   Token attributes should be seperated with a single space.");
			System.out.println("#   Example: Ala ala subst:sg:nom:f  ma mieć fin:sg:ter:imperf  kota kot subst:sg:nom:f");			
			System.out.println("# To disable the additional outputs rerun with `-silent` option.");
			System.out.println("# To finish enter 'EOF'.");
        }
		
		do {
	        if (!LinerOptions.get().silent)
	        	System.out.print("> ");
			
			// Get line of text to process
			cSeq = in.readLine();

			// If the text is not EndOfFile then process it
			if (!cSeq.equals("EOF")) {

				Sentence sentence = new Sentence();
				AttributeIndex ai = new AttributeIndex();
				ai.addAttribute("orth");
				ai.addAttribute("base");
				ai.addAttribute("ctag");
				sentence.setAttributeIndex(ai);

				String[] tokens = cSeq.trim().split("  ");
				for (String tokenStr : tokens) {
					Token token = new Token();
					String[] tokenAttrs = tokenStr.split(" ");
					for (int i = 0; i < tokenAttrs.length; i++)
						token.setAttributeValue(i, tokenAttrs[i]);
					sentence.addToken(token);
				}
				
				if (FeatureGenerator.isInitialized()) {
					try {
						FeatureGenerator.generateFeatures(sentence, true);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				
				Chunking chunking = chunker.chunkSentence(sentence);
				String response = "";
				for (Chunk chunk : chunking.chunkSet())
					response += String.format("[%d,%d,%s]", chunk.getBegin()+1, chunk.getEnd()+1,
						chunk.getType());
				if (response.isEmpty())
					response = "NONE";
				System.out.println(response);
			}
		} while (!cSeq.equals("EOF"));
	}
}
