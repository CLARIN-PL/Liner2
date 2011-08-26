package liner2.action;

import java.util.Hashtable;
import java.util.Set;

import liner2.LinerOptions;
import liner2.Main;
import liner2.chunker.Chunker;
import liner2.chunker.CRFPPChunker;
import liner2.chunker.factory.ChunkerFactory;
import liner2.tools.TemplateFactory;

import liner2.reader.ReaderFactory;
import liner2.reader.StreamReader;

import liner2.structure.AttributeIndex;
import liner2.structure.ParagraphSet;

/**
 * Train chunkers.
 * @author Michał Marcińczuk
 *
 */
public class ActionTrain extends Action{

	/**
	 * Module entry function.
	 */
	public void run() throws Exception{

//		StreamReader reader = ReaderFactory.get()
//			.getStreamReader(LinerOptions.get());
//		ParagraphSet ps = reader.readParagraphSet();

		ParagraphSet ps = new ParagraphSet();
		
        Hashtable<String, Chunker> chunkers = ChunkerFactory.get()
        	.createChunkers(LinerOptions.get().chunkersDescription);
        /*for (String chunkerDescription : LinerOptions.get().chunkersDescription){
        	Main.log(chunkerDescription);
        	//ChunkerFactory.get().createChunker(chunkerDescription);
        	Main.log("TRAINED");
        	Main.log("");
        }*/
        
        AttributeIndex attributeIndex = new AttributeIndex();
        attributeIndex.addAttribute("orth");
        attributeIndex.addAttribute("base");
        attributeIndex.addAttribute("ctag");
        
        for (Object templateName : TemplateFactory.get().getTemplateNames())
        	TemplateFactory.get().store(""+templateName, templateName+".tpl", attributeIndex);
        
        for (Chunker chunker : chunkers.values())
        	chunker.train(ps);
	}
		
}
