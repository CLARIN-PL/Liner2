package liner2.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.IOUtils;

import liner2.LinerOptions;
import liner2.chunker.Chunker;
import liner2.chunker.factory.ChunkerFactory;
import liner2.chunker.factory.ChunkerManager;
import liner2.features.TokenFeatureGenerator;
import liner2.reader.ReaderFactory;
import liner2.reader.AbstractDocumentReader;
import liner2.structure.AnnotationSet;
import liner2.structure.Document;
import liner2.structure.Sentence;
import liner2.tools.ChunkerEvaluator;
import liner2.tools.ChunkerEvaluatorMuc;
import liner2.tools.ParameterException;
import liner2.tools.ProcessingTimer;

/**
 * Perform cross-validation on a specified corpus.
 * 
 * @author Michał Marcińczuk
 *
 */
public class ActionEvalCvBatch extends Action{

	ArrayList<List<String>> folds = new ArrayList<List<String>>();
	
	public void run() throws Exception {
	
		if ( !LinerOptions.isOption(LinerOptions.OPTION_USE) ){
			throw new ParameterException("Parameter --use <chunker_pipe_desription> not set");
		}
	
		ProcessingTimer timer = new ProcessingTimer();
		ChunkerEvaluator globalEval = new ChunkerEvaluator(LinerOptions.getGlobal().getTypes());
		ChunkerEvaluatorMuc globalEvalMuc = new ChunkerEvaluatorMuc(LinerOptions.getGlobal().getTypes());
		
		this.loadFolds();
		TokenFeatureGenerator gen = null;
        
		if (!LinerOptions.getGlobal().features.isEmpty()){
            gen = new TokenFeatureGenerator(LinerOptions.getGlobal().features);
        }
		
		for (int i = 1; i <= folds.size(); i++) {			
			
			/** Ustaw wartość dla globalnej zmiennej {CV-TRAIN} */
			LinerOptions.getGlobal().setCvTrain(this.getTrainingSet(i));

	    	timer.startTimer("Model training");
            ChunkerManager cm = ChunkerFactory.loadChunkers(LinerOptions.getGlobal());
            Chunker chunker = cm.getChunkerByName(LinerOptions.getGlobal().getOptionUse());
            timer.stopTimer();
			
			AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(
				LinerOptions.getGlobal().getOption(LinerOptions.OPTION_INPUT_FILE).replace("FOLD", ""+i),
				IOUtils.toInputStream(this.getTestingSet(i)),
    			LinerOptions.getGlobal().getOption(LinerOptions.OPTION_INPUT_FORMAT));

			ChunkerEvaluator localEval = new ChunkerEvaluator(LinerOptions.getGlobal().getTypes());
			ChunkerEvaluatorMuc localEvalMuc = new ChunkerEvaluatorMuc(LinerOptions.getGlobal().getTypes());
			HashMap<Sentence, AnnotationSet> chunkings = null;

    		Document ps = reader.nextDocument();
	    	while ( ps != null ){
	    		
	    		/* Get reference set of annotations */
	    		HashMap<Sentence, AnnotationSet> referenceChunks = ps.getChunkings();
	    		
	    		/* Remove annotation to be evaluated */
	    		for (String annotation : LinerOptions.getGlobal().getTypes())
	    			ps.removeAnnotations(annotation);	    		
	    			    		
	    		/* Generate features */
	    		timer.startTimer("Feature generation");
	    		if ( gen != null )
	    			gen.generateFeatures(ps);
	            timer.stopTimer();
	    		
	    		timer.startTimer("Chunking");
	    		chunker.prepare(ps);
	        	chunkings = chunker.chunk(ps);
	        	timer.stopTimer();
	        	
	    		timer.startTimer("Evaluation");
	    		timer.addTokens(ps);
	    		localEval.evaluate(ps.getSentences(), chunkings, referenceChunks);
	    		localEvalMuc.evaluate(chunkings, ps.getChunkings());
	    		globalEval.evaluate(ps.getSentences(), chunkings, referenceChunks);
	    		globalEvalMuc.evaluate(chunkings, ps.getChunkings());
	    		timer.stopTimer();
	            
	        	timer.startTimer("Data reading");
	        	ps = reader.nextDocument();
	        	timer.stopTimer();
	    	}			
			
			System.out.println("***************************************** FOLD " + i + " *****************************************");	
			localEval.printResults();
			localEvalMuc.printResults();
			System.out.println("");			
		}
		
		System.out.println("***************************************** SUMMARY *****************************************");	
		globalEval.printResults();
		globalEvalMuc.printResults();
		System.out.println("");
		timer.printStats();
	}
	
	private void loadFolds() throws IOException{
		int folds_num = LinerOptions.getGlobal().getFoldsNumber();
		String inputPattern =  LinerOptions.getGlobal().getOption(LinerOptions.OPTION_INPUT_FILE);
		
		/** Wczytaj listy plików */
		for (int i=1; i<=folds_num; i++){
			String filename = inputPattern.replace("FOLD", ""+i);
			String root = (new File(filename)).getParentFile().getAbsolutePath();
			BufferedReader bf = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
			List<String> lines = new ArrayList<String>();
			String line = bf.readLine();
			while ( line != null ){
				if (!line.startsWith("/"))
					line = root + "/" + line;
				lines.add(line);
				line = bf.readLine();
			}
			bf.close();
			folds.add(lines);
		}		
	}
	
	private String getTrainingSet(int fold){
		StringBuilder sbtrain = new StringBuilder();
		
		for ( int j = 1; j<= folds.size(); j++){
			if ( j != fold ) 
				for ( String line : folds.get(j-1))
					sbtrain.append(line + "\n");							
		}
		return sbtrain.toString().trim();
	}
	
	private String getTestingSet(int fold){
		StringBuilder sbtrain = new StringBuilder();
		for ( String line : folds.get(fold-1))
			sbtrain.append(line + "\n");							
		return sbtrain.toString().trim();		
	}
}
