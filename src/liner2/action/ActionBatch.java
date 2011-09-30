package liner2.action;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import liner2.chunker.Chunker;
import liner2.chunker.factory.ChunkerFactory;

import liner2.reader.FeatureGenerator;
import liner2.reader.ReaderFactory;
import liner2.reader.StreamReader;

import liner2.structure.AttributeIndex;
import liner2.structure.Chunk;
import liner2.structure.Chunking;
import liner2.structure.Paragraph;
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
        
        if ( !LinerOptions.isOption(LinerOptions.OPTION_USE) ){
			throw new ParameterException("Parameter --use <chunker_pipe_desription> not set");
		}

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String cSeq = "";
		
		String maca = LinerOptions.getOption(LinerOptions.OPTION_MACA);
		String wmbt = LinerOptions.getOption(LinerOptions.OPTION_WMBT);
//		if (maca != null) System.out.println("maca: " + maca);
//		if (wmbt != null) System.out.println("wmbt: " + wmbt);
		
		if (!LinerOptions.get().silent){
			System.out.println("# Loading, please wait...");
        }
		
		ChunkerFactory.loadChunkers(LinerOptions.get().chunkersDescription);		
		Chunker chunker = ChunkerFactory.getChunkerPipe(LinerOptions.getOption(LinerOptions.OPTION_USE));
		
		if (!LinerOptions.get().silent){
			System.out.println("# Enter a sentence and press Enter.");
			if (maca == null) {
				System.out.println("#   Tokens should be seperated with double spaces.");
				System.out.println("#   Token attributes should be seperated with a single space.");
				System.out.println("#   Example: Ala ala subst:sg:nom:f  ma mieć fin:sg:ter:imperf  kota kot subst:sg:acc:m1");			
			}
//			else {
//				System.out.println("#   Using maca morphological analyzer with path: " + maca);
//				if (wmbt != null)
//					System.out.println("#   Using WMBT tagger with path: " + wmbt);
//			}
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

				Sentence sentence;
				if (maca == null)
					sentence = analyzePlain(cSeq);
				else
					sentence = analyze(cSeq, maca, wmbt);
				
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
	
	private Sentence analyzePlain(String cSeq) {
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
		return sentence;
	}
	
	private Sentence analyze(String cSeq, String maca, String wmbt) {
		// prepare maca command
		String maca_cmd = maca.equals("-") ? "" : maca;
		if (!maca.equals("-")) {
			if (!maca.endsWith("/"))
				maca_cmd += "/";
			maca_cmd += "bin/maca-analyse/";
		}
		maca_cmd += "maca-analyse -qs morfeusz-nkjp -o ccl";
		
		// execute maca
		Process maca_p = null;
		try {
			maca_p = Runtime.getRuntime().exec(maca_cmd);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
		InputStream maca_in = maca_p.getInputStream();
		OutputStream maca_out = maca_p.getOutputStream();
		BufferedWriter maca_writer = new BufferedWriter(
			new OutputStreamWriter(maca_out));
		
		// if using wmbt, then combine maca with wmbt
		if (wmbt != null) {
			// prepare wmbt command
			if (!wmbt.endsWith("/"))
				wmbt += "/";
			String wmbt_cmd = wmbt + "wmbt/wmbt.py";
			wmbt_cmd += " -d " + wmbt + "model_nkjp10 -i ccl -o ccl";
			wmbt_cmd += " " + wmbt + "config/nkjp-k11.ini -";
			
			Process wmbt_p = null;
			try {
				wmbt_p = Runtime.getRuntime().exec(wmbt_cmd);
				InputStream wmbt_in = wmbt_p.getInputStream();
				OutputStream wmbt_out = wmbt_p.getOutputStream();
				
				BufferedReader maca_reader = new BufferedReader(
					new InputStreamReader(maca_in));
				BufferedWriter wmbt_writer = new BufferedWriter(
					new OutputStreamWriter(wmbt_out));
					
				maca_writer.write(cSeq, 0, cSeq.length());
				maca_writer.close();
				
				String line = null;
				while ((line = maca_reader.readLine()) != null)
					wmbt_writer.write(line, 0, line.length());
				wmbt_writer.close(); 
				
				StreamReader reader = ReaderFactory.get().getStreamReader(wmbt_in, "ccl");
				Paragraph paragraph = reader.readParagraph();
				Sentence sentence = paragraph.getSentences().get(0);
				return sentence;
				
			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}		
		}
		
		else {
			try {
				maca_writer.write(cSeq, 0, cSeq.length());
				maca_writer.close();
				
				StreamReader reader = ReaderFactory.get().getStreamReader(maca_in, "ccl");
				Paragraph paragraph = reader.readParagraph();
				Sentence sentence = paragraph.getSentences().get(0);
				return sentence;
			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
		}
	}
}
