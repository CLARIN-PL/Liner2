package liner2.writer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import liner2.writer.CclStreamWriter;
import liner2.writer.IobStreamWriter;

public class WriterFactory {

	private static final WriterFactory factory = new WriterFactory();
	
	private WriterFactory(){
		
	}
	
	public static WriterFactory get(){
		return WriterFactory.factory; 
	}
	
	/**
	 * TODO
	 * @return
	 */
	public StreamWriter getStreamWriter(String outputFile, String outputFormat) throws Exception {
		if (outputFormat.equals("ccl"))
			return new CclStreamWriter(getOutputStream(outputFile));
		else if (outputFormat.equals("iob"))
			return new IobStreamWriter(getOutputStream(outputFile));
		else		
			throw new Exception("Output format " + outputFormat + " not recognized.");
	}
	
	private OutputStream getOutputStream(String outputFile) throws Exception {
		if ((outputFile == null) || (outputFile.isEmpty()))
			return System.out;
		else {
			try {
				return new FileOutputStream(outputFile);
			} catch (IOException ex) {
				throw new Exception("Unable to write output file: " + outputFile);
			}
		}
	}
}
