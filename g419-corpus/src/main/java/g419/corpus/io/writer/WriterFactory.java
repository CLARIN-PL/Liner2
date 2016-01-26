package g419.corpus.io.writer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.GZIPOutputStream;

public class WriterFactory {

	private static final WriterFactory factory = new WriterFactory();
	
	private WriterFactory(){}
	
	public static WriterFactory get(){
		return WriterFactory.factory; 
	}

	/**
	 * Tworzy piśnik dla określonego formatu outputFormat i lokalizacji outputFile.
	 * Jeżeli outputFile jest nullem, to strumieniem wyjściowym jest System.out.
	 * 
	 * @param outputFile
	 * @param outputFormat
	 * @return
	 * @throws Exception
	 */
	public AbstractDocumentWriter getStreamWriter(String outputFile, String outputFormat) throws Exception {
		if (outputFormat.startsWith("batch:")){
            String format = outputFormat.substring(6);
            return new BatchWriter(outputFile, format);
        }
		else{
			boolean gzOutput = false;
			String outputFormatNoGz = outputFormat; 
			if ( outputFormat.endsWith(":gz") ){
				gzOutput = true;
				outputFormatNoGz = outputFormat.substring(0, outputFormat.length()-3);
			}
			
	        if ( "tei".equals(outputFormatNoGz) ){
	            return this.getTEIWriter(outputFile, gzOutput);
	        }
	        else if ( "ccl_rel".equals(outputFormatNoGz) ){
	        	return this.getCclRelWriter(outputFile, gzOutput);
	        }
	        else{
	            return this.getStreamWriter(this.getOutputStreamFileOrOut(outputFile), outputFormat);
	        }			
		}		
	}

    /**
     * Zwraca piśnik dla formatu outputFormat. Dane zapisywane są do strumieia out.
     * @param out
     * @param outputFormat
     * @return
     * @throws Exception
     */
	public AbstractDocumentWriter getStreamWriter(OutputStream out, String outputFormat) throws Exception {
		OutputStream outWrapped = out;
		if ( outputFormat.endsWith(":gz") ){
			outWrapped = new GZIPOutputStream(out);
			outputFormat = outputFormat.substring(0, outputFormat.length() - 3);
		}
		 
        if (outputFormat.equals("ccl"))
			return new CclStreamWriter(outWrapped);
        else if (outputFormat.equals("iob"))
			return new IobStreamWriter(outWrapped);
        else if (outputFormat.equals("conll"))
			return new ConllStreamWriter(outWrapped);
        else if (outputFormat.equals("zero_verb"))
			return new ZeroVerbWriter(outWrapped);
		else if (outputFormat.equals("iob-tab"))
			return new IobTabStreamWriter(outWrapped);
		else if (outputFormat.equals("tuples"))
			return new TuplesStreamWriter(outWrapped);
		else if (outputFormat.equals("json"))
			return new JsonStreamWriter(outWrapped);
		else if (outputFormat.equals("json-frames"))
			return new JsonFramesStreamWriter(outWrapped);
		else if (outputFormat.equals("tokens"))
			return new TokensStreamWriter(outWrapped);
        else if (outputFormat.equals("arff"))
            return new ArffStreamWriter(outWrapped);
        else if (outputFormat.equals("verb_eval"))
            return new MinosVerbEvalWriter(outWrapped);
        else if (outputFormat.equals("simple_rel"))
        	return new SimpleRelationClusterSetWriter(outWrapped);
        else if (outputFormat.equals("relation-tuples"))
        	return new RelationTuplesWriter(outWrapped);
        else if (outputFormat.equals("conll"))
        	return new ConllStreamWriter(outWrapped);
		else		
			throw new Exception("Output format " + outputFormat + " not recognized.");
	}

	/**
	 * Zwraca piśnik do zapisu danych w formacie Arff.
	 * @param outputFile
	 * @param features
	 * @return
	 * @throws Exception
	 */
    public AnnotationArffWriter getArffAnnotationWriter(String outputFile, List<String> features) throws Exception {
        return new AnnotationArffWriter(getOutputStreamGz(outputFile, false), features);
    }
	
    /**
     * Zwraca piśnik do formatu ccl. Dane zapisywane są do dwóch plików: .xml i .rel.xml.
     * @param outputFile
     * @param gz
     * @return
     * @throws Exception
     */
	public AbstractDocumentWriter getCclRelWriter(String outputFile, boolean gz) throws Exception{
		OutputStream out = this.getOutputStreamGz(outputFile, gz);
		OutputStream outRel = this.getOutputStreamGz(outputFile.replace(".xml", ".rel.xml"), gz);
		return new CclStreamWriter(out, outRel);
	}

    public AbstractDocumentWriter getTEIWriter(String outputFolder, boolean gz) throws Exception{
        if(outputFolder == null){
            throw new FileNotFoundException("TEI format requires existing folder as a target (-t) parameter value)");
        }
        File folder = new File(outputFolder);
        if ( !folder.exists() ){
        	folder.mkdirs();
        }
        String gzExt = gz ? ".gz" : "";
        OutputStream text = getOutputStreamGz(
        		new File(outputFolder,"text.xml" + gzExt).getPath(), gz);
        OutputStream annSegmentation = getOutputStreamGz(
        		new File(outputFolder,"ann_segmentation.xml" + gzExt).getPath(), gz);
        OutputStream annMorphosyntax = getOutputStreamGz(
        		new File(outputFolder,"ann_morphosyntax.xml" + gzExt).getPath(), gz);
        OutputStream annNamed = getOutputStreamGz(
        		new File(outputFolder,"ann_named.xml" + gzExt).getPath(), gz);
        OutputStream annMentions = getOutputStreamGz(
        		new File(outputFolder,"ann_mentions.xml" + gzExt).getPath(), gz);
        OutputStream annChunks = getOutputStreamGz(
        		new File(outputFolder,"ann_chunks.xml" + gzExt).getPath(), gz);
        OutputStream annCoreference = getOutputStreamGz(
        		new File(outputFolder,"ann_coreference.xml" + gzExt).getPath(), gz);
        OutputStream annRelations = getOutputStreamGz(
        		new File(outputFolder,"ann_relations.xml" + gzExt).getPath(), gz);
        return new TEIStreamWriter(text, annSegmentation, annMorphosyntax, annNamed, 
        		annMentions, annChunks, annCoreference, annRelations, new File(outputFolder).getName());
    }

    /**
     * Tworzy strumień do zapisu danych. Jeżeli gz jest true, to strumień zostaje opakowany w obiekt GZIPOutputStream.
     * @param outputFile
     * @param gz
     * @return
     * @throws Exception
     */
	private OutputStream getOutputStreamGz(String outputFile, boolean gz) throws Exception {
		OutputStream output = new FileOutputStream(outputFile); 
		if ( gz ){
			output = new GZIPOutputStream(output);
		}
		return output;
	}
	
	/**
	 * Zwraca strumień do zapisu danych w zależności od wartości outputFile.
	 * @param outputFile nazwa pliku lub null
	 * @return System.out jeżeli outputFile jest nullem, wpp obiekt FileOutputStream dla outputFile.
	 * @throws FileNotFoundException
	 */
	private OutputStream getOutputStreamFileOrOut(String outputFile) throws FileNotFoundException{
		if ((outputFile == null) || (outputFile.isEmpty()))
			return System.out;
		else {
			return new FileOutputStream(outputFile);
		}
	}
}
