package g419.corpus.io.writer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.FilenameUtils;

import g419.corpus.io.UnknownFormatException;

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
	        else if ( "cclrel".equals(outputFormatNoGz) ){
	        	return this.getCclRelWriter(outputFile, gzOutput, false);
	        }
	        else if ( "cclrel-disamb".equals(outputFormatNoGz) ){
	        	return this.getCclRelWriter(outputFile, gzOutput, true);
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
        if (outputFormat.equals("ccl-disamb"))
			return new CclStreamWriter(outWrapped, true);
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
		else if (outputFormat.equals("json-annotations"))
			return new JsonAnnotationsStreamWriter(outWrapped);
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
        else if (outputFormat.equals("tuples-relations"))
        	return new RelationTuplesWriter(outWrapped);
        else if (outputFormat.equals("conll"))
        	return new ConllStreamWriter(outWrapped);
        else if (outputFormat.equals("csv-relations"))
        	return new CsvRelationsWriter(outWrapped);
		else		
			throw new UnknownFormatException("Output format " + outputFormat + " not recognized.");
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
	public AbstractDocumentWriter getCclRelWriter(String outputFile, boolean gz, boolean disambOnly) throws Exception{
		OutputStream out = this.getOutputStreamGz(outputFile, gz);		
		String relFilename = FilenameUtils.getBaseName(outputFile) + ".rel." + FilenameUtils.getExtension(outputFile);
		String relPath = new File(new File(outputFile).getParentFile(), relFilename).getPath();
		System.out.println(relPath);
		OutputStream outRel = this.getOutputStreamGz(relPath, gz);
		return new CclStreamWriter(out, outRel, disambOnly);
	}

    public AbstractDocumentWriter getTEIWriter(String outputFolder, boolean gz) throws Exception{
        if(outputFolder == null){
            throw new FileNotFoundException("TEI format requires existing folder as a target (-t) parameter value)");
        }
        File folder = new File(outputFolder);
        if ( !folder.exists() ){
        	folder.mkdirs();
        }
        OutputStream text = getOutputStreamGz(
        		new File(outputFolder,"text.xml").getPath(), gz);
        OutputStream metadata = getOutputStreamGz(
        		new File(outputFolder,"metadata.xml").getPath(), gz);
        OutputStream annSegmentation = getOutputStreamGz(
        		new File(outputFolder,"ann_segmentation.xml").getPath(), gz);
        OutputStream annMorphosyntax = getOutputStreamGz(
        		new File(outputFolder,"ann_morphosyntax.xml").getPath(), gz);
        OutputStream annProps = getOutputStreamGz(
        		new File(outputFolder,"ann_props.xml").getPath(), gz);
        OutputStream annNamed = getOutputStreamGz(
        		new File(outputFolder,"ann_named.xml").getPath(), gz);
        OutputStream annMentions = getOutputStreamGz(
        		new File(outputFolder,"ann_mentions.xml").getPath(), gz);
        OutputStream annChunks = getOutputStreamGz(
        		new File(outputFolder,"ann_chunks.xml").getPath(), gz);
        OutputStream annCoreference = getOutputStreamGz(
        		new File(outputFolder,"ann_coreference.xml").getPath(), gz);
        OutputStream annRelations = getOutputStreamGz(
        		new File(outputFolder,"ann_relations.xml").getPath(), gz);
        return new TEIStreamWriter(text, metadata, annSegmentation, annMorphosyntax, annProps, annNamed, 
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
		OutputStream output = new FileOutputStream(outputFile + (gz ? ".gz" : "")); 
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
