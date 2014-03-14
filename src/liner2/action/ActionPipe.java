package liner2.action;

import liner2.LinerOptions;
import liner2.chunker.Chunker;
import liner2.chunker.factory.ChunkerFactory;
import liner2.chunker.factory.ChunkerManager;
import liner2.features.TokenFeatureGenerator;
import liner2.reader.AbstractDocumentReader;
import liner2.structure.Document;
import liner2.tools.ParameterException;
import liner2.writer.AbstractDocumentWriter;

/**
 * Chunking in pipe mode.
 * @author Maciej Janicki, Michał Marcińczuk
 *
 */
public class ActionPipe extends Action{

	/**
	 * Module entry function.
	 */
	public void run() throws Exception{
	
        if ( !LinerOptions.isOption(LinerOptions.OPTION_USE) ){
			throw new ParameterException("Parameter '--use <chunker_name>' not set");
		}		
	
        AbstractDocumentReader reader = LinerOptions.getGlobal().getInputReader();
		AbstractDocumentWriter writer = LinerOptions.getGlobal().getOutputWriter();
        TokenFeatureGenerator gen = null;
        
        if (!LinerOptions.getGlobal().features.isEmpty()){
            gen = new TokenFeatureGenerator(LinerOptions.getGlobal().features);            
        }
		
		/* Create all defined chunkers. */
        ChunkerManager cm = ChunkerFactory.loadChunkers(LinerOptions.getGlobal());
        Chunker chunker = cm.getChunkerByName(LinerOptions.getGlobal().getOptionUse());

		Document ps = reader.nextDocument();
		while ( ps != null ){
			if ( gen != null )
				gen.generateFeatures(ps);
			chunker.chunkInPlace(ps);
			writer.writeDocument(ps);
			ps = reader.nextDocument();
		}

		reader.close();
		writer.close();
	}
		
}
