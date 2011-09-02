package liner2.action;

import java.io.File;
import java.util.ArrayList;

import liner2.Main;
import liner2.LinerOptions;
import liner2.chunker.Chunker;
import liner2.chunker.factory.ChunkerFactory;
import liner2.reader.FeatureGenerator;
import liner2.reader.ReaderFactory;
import liner2.reader.StreamReader;
import liner2.structure.Paragraph;
import liner2.structure.ParagraphSet;
import liner2.tools.ChunkerEvaluator;
//import liner2.tools.ParameterException;

/**
 * Perform cross-validation on a specified corpus.
 * 
 * @author Michał Marcińczuk
 * @author Maciej Janicki
 *
 */
public class ActionEvalCV extends Action{

	/**
	 * 
	 */
	public void run() throws Exception
	{
		ChunkerEvaluator eval = new ChunkerEvaluator(null);
		
		for (int i = 1; ; i++) {
			String trainFile = LinerOptions.getOption(LinerOptions.OPTION_INPUT_FILE) + ".fold-" + i + ".train";
			String testFile = LinerOptions.getOption(LinerOptions.OPTION_INPUT_FILE) + ".fold-" + i + ".test";
			if ((! (new File(trainFile).exists())) || (! (new File(testFile).exists())))
				break;
			
			ArrayList<String> currentDescriptions = new ArrayList<String>();
			for (String desc : LinerOptions.get().chunkersDescription)
				currentDescriptions.add(desc.replace("FILENAME", trainFile));
			
			ChunkerFactory.reset();
			ChunkerFactory.loadChunkers(currentDescriptions);
			Chunker chunker = ChunkerFactory.getChunkerPipe(LinerOptions.getOption(LinerOptions.OPTION_USE));
			
//			Main.log("Chunker trained. Testing...");
							
			StreamReader reader = ReaderFactory.get().getStreamReader(testFile, 
    			LinerOptions.getOption(LinerOptions.OPTION_INPUT_FORMAT));
    		ParagraphSet ps = reader.readParagraphSet();
			
//			ChunkerEvaluator eval = new ChunkerEvaluator(chunker);
			eval.setChunker(chunker);
			for (Paragraph p : ps.getParagraphs()){
				eval.evaluate(p);
			}
			
//			System.out.println("========== FOLD " + i + " ==========");	
//			eval.printResults();
//			System.out.println("");
		}
		
		eval.printResults();
	}
}
