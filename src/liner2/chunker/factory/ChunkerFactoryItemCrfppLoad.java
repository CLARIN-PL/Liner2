package liner2.chunker.factory;

import java.io.File;
import java.io.FileReader;
import java.util.regex.Matcher;

import org.ini4j.Ini;

import liner2.Main;
import liner2.chunker.CrfppChunker;
import liner2.chunker.Chunker;


public class ChunkerFactoryItemCrfppLoad extends ChunkerFactoryItem {

	public ChunkerFactoryItemCrfppLoad() {
		super("crfpp-load:(.*)");
	}

	@Override
	public Chunker getChunker(String description, ChunkerManager cm) throws Exception {
       	Matcher matcherCRFPPload = this.pattern.matcher(description);
		if (matcherCRFPPload.find()){
			String iniPath = matcherCRFPPload.group(1);
            String iniDir = new File(iniPath).getParent();

            Ini ini = new Ini(new FileReader(iniPath));
            Ini.Section main = ini.get("main");
            String store = main.get("store").replace("{INI_PATH}", iniDir);
			
            Main.log("--> CRFPP Chunker deserialize from " + store);
            
            CrfppChunker chunker = new CrfppChunker();
            chunker.deserialize(store);

            return chunker;		
		}

		return null;
	}

}
