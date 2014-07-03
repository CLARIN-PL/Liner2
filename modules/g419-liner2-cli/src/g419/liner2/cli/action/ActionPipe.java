package g419.liner2.cli.action;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.writer.AbstractDocumentWriter;
import g419.corpus.structure.Document;
import g419.liner2.api.LinerOptions;
import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.factory.ChunkerFactory;
import g419.liner2.api.chunker.factory.ChunkerManager;
import g419.liner2.api.features.TokenFeatureGenerator;
import g419.liner2.api.tools.ParameterException;

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
       /* Ustawienie domyślnych parametrów. */
        LinerOptions.getGlobal().setDefaultDataFormats("ccl", "ccl");
	
        if ( !LinerOptions.getGlobal().isOption(LinerOptions.OPTION_USE) ){
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
