package g419.corpus.io.writer;


import java.io.*;
import java.util.List;


public class WriterFactory {

	private static final WriterFactory factory = new WriterFactory();
	
	private WriterFactory(){}
	
	public static WriterFactory get(){
		return WriterFactory.factory; 
	}
	
	/**
	 * TODO
	 * @return
	 */
	public AbstractDocumentWriter getStreamWriter(String outputFile, String outputFormat) throws Exception {
        if (outputFormat.equals("tei")){
            return getTEIWriter(outputFile);
        }
        else if (outputFormat.startsWith("batch:")){
            String format = outputFormat.substring(6);
            return new BatchWriter(outputFile, format);
        }
        else{
            return getStreamWriter(getOutputStream(outputFile), outputFormat);
        }
	}

    public AnnotationArffWriter getArffAnnotationWriter(String outputFile, List<String> features) throws Exception {
        return new AnnotationArffWriter(getOutputStream(outputFile), features);
    }
	
	public AbstractDocumentWriter getStreamWriter(OutputStream out, String outputFormat) throws Exception {
        if (outputFormat.equals("ccl"))
			return new CclStreamWriter(out);
		else if (outputFormat.equals("iob"))
			return new IobStreamWriter(out);
		else if (outputFormat.equals("iob-tab"))
			return new IobTabStreamWriter(out);
		else if (outputFormat.equals("tuples"))
			return new TuplesStreamWriter(out);
		else if (outputFormat.equals("tokens"))
			return new TokensStreamWriter(out);
        else if (outputFormat.equals("arff"))
            return new ArffStreamWriter(out);
		else		
			throw new Exception("Output format " + outputFormat + " not recognized.");
	}

    public AbstractDocumentWriter getTEIWriter(String outputFolder) throws Exception{
        if(outputFolder == null){
            throw new FileNotFoundException("TEI format requires existing folder as a target (-t) parameter value)");
        }
        else if(!new File(outputFolder).exists()){
            throw new FileNotFoundException("Folder specified as target parameter does not exist:" + outputFolder);
        }
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
