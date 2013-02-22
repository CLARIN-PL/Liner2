package liner2.action;

import java.io.File;
import java.util.ArrayList;

import liner2.LinerOptions;
import liner2.chunker.Chunker;
import liner2.chunker.factory.ChunkerFactory;
import liner2.reader.ReaderFactory;
import liner2.reader.StreamReader;
import liner2.structure.ParagraphSet;
import liner2.tools.ChunkerEvaluator;
import liner2.tools.ChunkerEvaluatorMuc;
import liner2.tools.ParameterException;

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
	public void run() throws Exception {
	
		if ( !LinerOptions.isOption(LinerOptions.OPTION_USE) ){
			throw new ParameterException("Parameter --use <chunker_pipe_desription> not set");
		}
	
		ChunkerEvaluator globalEval = new ChunkerEvaluator();
		ChunkerEvaluatorMuc globalEvalMuc = new ChunkerEvaluatorMuc();
		
		for (int i = 1; i <= 10; i++) {			
			String trainFile = LinerOptions.getOption(LinerOptions.OPTION_INPUT_FILE) + ".fold-" + i + ".train";
			String testFile = LinerOptions.getOption(LinerOptions.OPTION_INPUT_FILE) + ".fold-" + i + ".test";
			if ((! (new File(trainFile).exists())) || (! (new File(testFile).exists()))){
				break;
			}
			
			ArrayList<String> currentDescriptions = new ArrayList<String>();
			for (String desc : LinerOptions.get().chunkersDescription)
				currentDescriptions.add(desc.replace("FILENAME", trainFile));
			
			ChunkerFactory.reset();
			ChunkerFactory.loadChunkers(currentDescriptions);
			Chunker chunker = ChunkerFactory.getChunkerPipe(LinerOptions.getOption(LinerOptions.OPTION_USE));
			
			StreamReader reader = ReaderFactory.get().getStreamReader(testFile, 
    			LinerOptions.getOption(LinerOptions.OPTION_INPUT_FORMAT));
    		ParagraphSet ps = reader.readParagraphSet();
			
			ChunkerEvaluator localEval = new ChunkerEvaluator();
			ChunkerEvaluatorMuc localEvalMuc = new ChunkerEvaluatorMuc();
			
			localEval.evaluate(ps.getSentences(), chunker.chunk(ps), ps.getChunkings());
			localEvalMuc.evaluate(chunker.chunk(ps), ps.getChunkings());
			
			System.out.println("========== FOLD " + i + " ==========");	
			localEval.printResults();
			localEvalMuc.printResults();
			System.out.println("");
			
			globalEval.join(localEval);
			globalEvalMuc.join(localEvalMuc);
		}
		
		globalEval.printResults();
		globalEvalMuc.printResults();
	}
}
