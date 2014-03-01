package liner2.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import liner2.LinerOptions;

import org.apache.commons.io.IOUtils;

public class ReaderFactory {

	private static final ReaderFactory factory = new ReaderFactory();
	
	private ReaderFactory(){
		
	}
	
	public static ReaderFactory get(){
		return ReaderFactory.factory; 
	}
	
	/**
	 * TODO
	 * @return
	 */
	public StreamReader getStreamReader(String inputFile, String inputFormat) throws Exception {
        if (inputFormat.equals("tei")){
            return getTEIStreamReader(inputFile);
        }
        else if (inputFile.equals("{CV-TRAIN}")){        	
        	return getStreamReader(
        			IOUtils.toInputStream(LinerOptions.getGlobal().getCvTrain()),
        			"",        			
        			inputFormat);
        }
        else{
		    return getStreamReader(getInputStream(inputFile),
		    		(new File(inputFile)).getParent(),
		    		inputFormat);
        }
	}

	public StreamReader getStreamReader(InputStream in, String inputFormat) throws Exception {
		return getStreamReader(in, "", inputFormat);
	}

	public StreamReader getStreamReader(InputStream in, String root, String inputFormat) throws Exception {
		if (inputFormat.equals("ccl"))
			return new CclSAXStreamReader(in);
		else if (inputFormat.equals("ccl-deprecated"))
			return new CclStreamReader(in);
		else if (inputFormat.equals("iob"))
			return new IobStreamReader(in);
		else if (inputFormat.equals("plain"))
			return new PlainTextStreamReader(in);
        else if (inputFormat.equals("ccl-batch"))
            return new CclBatchReader(in, root);
		else
			throw new Exception("Input format " + inputFormat + " not recognized.");
	}

    public StreamReader getTEIStreamReader(String inputFolder) throws Exception{
        InputStream annMorphosyntax = getInputStream(new File(inputFolder,"ann_morphosyntax.xml").getPath());
        InputStream annSegmentation = getInputStream(new File(inputFolder,"ann_segmentation.xml").getPath());
        InputStream annNamed = getInputStream(new File(inputFolder,"ann_named.xml").getPath());
        return new TEIStreamReader(annMorphosyntax, annSegmentation, annNamed);
    }
	
	private InputStream getInputStream(String inputFile) throws Exception {
		if ((inputFile == null) || (inputFile.isEmpty()))
			return System.in;
		else {
			try {
				return new FileInputStream(inputFile);
			} catch (IOException ex) {
				throw new Exception("Unable to read input file: " + inputFile);
			}
		}
	}
}
