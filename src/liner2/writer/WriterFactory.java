package liner2.writer;

import java.io.*;

import liner2.tools.Template;
import liner2.writer.CclStreamWriter;
import liner2.writer.IobStreamWriter;
import liner2.writer.TokensStreamWriter;

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
        if (outputFormat.equals("tei")){
            return getTEIWriter(outputFile);
        }
        else{
            return getStreamWriter(getOutputStream(outputFile), outputFormat);
        }
	}

    public StreamWriter getArffWriter(OutputStream out, Template template){
        return new ArffStreamWriter(out, template);
    }

    public StreamWriter getArffWriter(String outputFile, Template template) throws Exception{
        return getArffWriter(getOutputStream(outputFile), template);
    }
	
	public StreamWriter getStreamWriter(OutputStream out, String outputFormat) throws Exception {
        if (outputFormat.equals("ccl"))
			return new CclStreamWriter(out);
		else if (outputFormat.equals("iob"))
			return new IobStreamWriter(out);
		else if (outputFormat.equals("tuples"))
			return new TuplesStreamWriter(out);
		else if (outputFormat.equals("tokens"))
			return new TokensStreamWriter(out);
        else if (outputFormat.equals("arff"))
            throw new Exception("In order to write to arff format use getArffWriter instead of getStreamWriter");
		else		
			throw new Exception("Output format " + outputFormat + " not recognized.");
	}

    public StreamWriter getTEIWriter(String outputFolder) throws Exception{
        OutputStream text = getOutputStream(new File(outputFolder,"text.xml").getPath());
        OutputStream annSegmentation = getOutputStream(new File(outputFolder,"ann_segmentation.xml").getPath());
        OutputStream annMorphosyntax = getOutputStream(new File(outputFolder,"ann_morphosyntax.xml").getPath());
        OutputStream annNamed = getOutputStream(new File(outputFolder,"ann_named.xml").getPath());
        return new TEIStreamWriter(text, annSegmentation, annMorphosyntax, annNamed, new File(outputFolder).getName());
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
