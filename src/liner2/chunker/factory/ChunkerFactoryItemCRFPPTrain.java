package liner2.chunker.factory;

import java.util.regex.Matcher;

import liner2.LinerOptions;
import liner2.Main;
import liner2.chunker.CRFPPChunker;
import liner2.chunker.Chunker;
import liner2.chunker.TrainableChunkerInterface;
import liner2.reader.ReaderFactory;
import liner2.reader.StreamReader;

public class ChunkerFactoryItemCRFPPTrain extends ChunkerFactoryItem {

	public ChunkerFactoryItemCRFPPTrain() {
		super("crfpp-train:p=([1-6]):template=(.*):iob=(.*?)(:model=(.*))?");
	}

	@Override
	public Chunker getChunker(String description) throws Exception {

       	Matcher matcherCRFPP = this.pattern.matcher(description);
		if (matcherCRFPP.find()){
            Main.log("--> CRFPP Chunker train");

            int threads = Integer.parseInt(matcherCRFPP.group(1));
            String template_filename = matcherCRFPP.group(2);
            String iob_filename = matcherCRFPP.group(3);
            
            String model_filename = "crf_model.bin";
            if ( matcherCRFPP.group(4) != null )
            	model_filename = matcherCRFPP.group(5);

            CRFPPChunker chunker = new CRFPPChunker(threads);
            chunker.setTemplateFilename(template_filename);
            chunker.setModelFilename(model_filename);

            Main.log("--> Training on file=" + iob_filename);            
            StreamReader reader = ReaderFactory.get().getStreamReader(LinerOptions.get());
            ((TrainableChunkerInterface)chunker).train(reader.readParagraphSet());
                        
            return chunker;		
		}
		else		
			return null;
	}

}
