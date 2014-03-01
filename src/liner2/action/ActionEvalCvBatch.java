package liner2.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import liner2.LinerOptions;
import liner2.chunker.Chunker;
import liner2.chunker.factory.ChunkerFactory;
import liner2.chunker.factory.ChunkerManager;
import liner2.features.TokenFeatureGenerator;
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
 *
 */
public class ActionEvalCvBatch extends Action{

	public void run() throws Exception {
	
		if ( !LinerOptions.isOption(LinerOptions.OPTION_USE) ){
			throw new ParameterException("Parameter --use <chunker_pipe_desription> not set");
		}
	
		ChunkerEvaluator globalEval = new ChunkerEvaluator();
		ChunkerEvaluatorMuc globalEvalMuc = new ChunkerEvaluatorMuc();
		
		int folds_num = LinerOptions.getGlobal().getFoldsNumber();
		String inputPattern =  LinerOptions.getGlobal().getOption(LinerOptions.OPTION_INPUT_FILE);
		ArrayList<List<String>> folds = new ArrayList<List<String>>();
		
		/** Wczytaj listy plików */
		for (int i=1; i<=folds_num; i++){
			String filename = inputPattern.replace("FOLD", ""+i);
			String root = (new File(filename)).getParent();
			FileInputStream fis = null;
			BufferedReader bf = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
			List<String> lines = new ArrayList<String>();
			String line = bf.readLine();
			while ( line != null ){
				if (!line.startsWith("/"))
					line = root + "/" + line;
				lines.add(line);
				line = bf.readLine();
			}
			folds.add(lines);
		}
				
		for (int i = 1; i <= folds_num; i++) {			
			StringBuilder sbtrain = new StringBuilder();
			
			for ( int j=1; j<=folds_num; j++){
				for ( String line : folds.get(j-1))
					sbtrain.append(line + "\n");							
			}
			
			/** Ustaw wartość dla globalnej zmiennej dla ini {CV-TRAIN} */
			LinerOptions.getGlobal().setCvTrain(sbtrain.toString());

            ChunkerManager cm = ChunkerFactory.loadChunkers(LinerOptions.getGlobal());
            Chunker chunker = cm.getChunkerByName(LinerOptions.getGlobal().getOptionUse());
			
			StreamReader reader = ReaderFactory.get().getStreamReader(inputPattern.replace("FOLD", ""+i), 
    			LinerOptions.getGlobal().getOption(LinerOptions.OPTION_INPUT_FORMAT));
    		ParagraphSet ps = reader.readParagraphSet();

            if (!LinerOptions.getGlobal().features.isEmpty()){
                TokenFeatureGenerator gen = new TokenFeatureGenerator(LinerOptions.getGlobal().features);
                gen.generateFeatures(ps);
            }
			
			ChunkerEvaluator localEval = new ChunkerEvaluator();
			ChunkerEvaluatorMuc localEvalMuc = new ChunkerEvaluatorMuc();
			
			localEval.evaluate(ps.getSentences(), chunker.chunk(ps), ps.getChunkings());
			localEvalMuc.evaluate(chunker.chunk(ps), ps.getChunkings());
			
			System.out.println("******************************** FOLD " + i + " ********************************");	
			localEval.printResults();
			localEvalMuc.printResults();
			System.out.println("");
			
			globalEval.join(localEval);
			globalEvalMuc.join(localEvalMuc);
		}
		
		System.out.println("******************************** SUMMARY ********************************");	
		globalEval.printResults();
		globalEvalMuc.printResults();
	}
}
