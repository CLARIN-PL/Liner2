package g419.liner2.cli.action;


import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.Document;
import g419.liner2.api.LinerOptions;
import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.factory.ChunkerFactory;
import g419.liner2.api.chunker.factory.ChunkerManager;
import g419.liner2.api.features.TokenFeatureGenerator;
import g419.liner2.api.tools.ChunkerEvaluator;
import g419.liner2.api.tools.ChunkerEvaluatorMuc;
import g419.liner2.api.tools.ParameterException;

import java.io.File;
import java.util.LinkedHashSet;


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
			String trainFile = LinerOptions.getGlobal().getOption(LinerOptions.OPTION_INPUT_FILE) + ".fold-" + i + ".train";
			String testFile = LinerOptions.getGlobal().getOption(LinerOptions.OPTION_INPUT_FILE) + ".fold-" + i + ".test";
			if ((! (new File(trainFile).exists())) || (! (new File(testFile).exists()))){
				break;
			}
			
			LinkedHashSet<String> currentDescriptions = new LinkedHashSet<String>();
			for (String desc : LinerOptions.getGlobal().chunkersDescriptions)
				currentDescriptions.add(desc.replace("FILENAME", trainFile));

            ChunkerManager cm = ChunkerFactory.loadChunkers(LinerOptions.getGlobal());
            Chunker chunker = cm.getChunkerByName(LinerOptions.getGlobal().getOptionUse());
			
			AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(testFile, 
    			LinerOptions.getGlobal().getOption(LinerOptions.OPTION_INPUT_FORMAT));
    		Document ps = reader.nextDocument();

            if (!LinerOptions.getGlobal().features.isEmpty()){
                TokenFeatureGenerator gen = new TokenFeatureGenerator(LinerOptions.getGlobal().features);
                gen.generateFeatures(ps);
            }
			
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
