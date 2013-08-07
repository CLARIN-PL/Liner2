package liner2.reader;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;

import liner2.reader.CclStreamReader;
import liner2.reader.IobStreamReader;

import liner2.LinerOptions;

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
		return getStreamReader(getInputStream(inputFile), inputFormat);
	}
	
	public StreamReader getStreamReader(InputStream in, String inputFormat) throws Exception {
		if (inputFormat.equals("ccl"))
			return new CclSAXStreamReader(in);
		else if (inputFormat.equals("ccl-deprecated"))
			return new CclStreamReader(in);
		else if (inputFormat.equals("iob"))
			return new IobStreamReader(in);
		else if (inputFormat.equals("plain"))
			return new PlainTextStreamReader(in);
        else if (inputFormat.equals("ccl-batch"))
            return new CclBatchReader(in);
		else
			throw new Exception("Input format " + inputFormat + " not recognized.");
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
