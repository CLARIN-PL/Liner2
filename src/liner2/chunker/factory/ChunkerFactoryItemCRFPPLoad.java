package liner2.chunker.factory;

import java.util.regex.Matcher;

import liner2.Main;
import liner2.chunker.CRFPPChunker;
import liner2.chunker.Chunker;


public class ChunkerFactoryItemCRFPPLoad extends ChunkerFactoryItem {

	public ChunkerFactoryItemCRFPPLoad() {
		super("crfpp-load:(.*)");
	}

	@Override
	public Chunker getChunker(String description) throws Exception {
       	Matcher matcherCRFPPload = this.pattern.matcher(description);
		if (matcherCRFPPload.find()){
			String model_filename = matcherCRFPPload.group(1); 
            Main.log("--> CRFPP Chunker deserialize from " + model_filename);
            
            CRFPPChunker chunker = new CRFPPChunker(1);
            chunker.deserialize(model_filename);

            return chunker;		
		}

		return null;
	}

}
