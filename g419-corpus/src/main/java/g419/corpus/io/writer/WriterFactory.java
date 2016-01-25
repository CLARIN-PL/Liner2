package g419.corpus.io.writer;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
	 * TODO
	 * @return
	 */
	public AbstractDocumentWriter getStreamWriter(String outputFile, String outputFormat) throws Exception {
		boolean gzOutput = false;
		if ( outputFormat.endsWith(":gz") ){
			gzOutput = true;
			outputFormat = outputFormat.substring(0, outputFormat.length()-3);
		}
		
        if (outputFormat.equals("tei")){
            return getTEIWriter(outputFile, gzOutput);
        }
        else if (outputFormat.startsWith("batch:")){
            String format = outputFormat.substring(6);
            return new BatchWriter(outputFile, format);
        }
        else if("ccl_rel".equals(outputFormat)){
        	return getCclRelWriter(outputFile);
        }
        else{
            return getStreamWriter(getOutputStream(outputFile, false), outputFormat);
        }
	}

    public AnnotationArffWriter getArffAnnotationWriter(String outputFile, List<String> features) throws Exception {
        return new AnnotationArffWriter(getOutputStream(outputFile, false), features);
    }
	
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
	
	public AbstractDocumentWriter getCclRelWriter(String outputFile) throws FileNotFoundException{
		String outputXml = outputFile;
		String outputRelXml = outputFile.replace(".xml", ".rel.xml");		
		OutputStream out = new FileOutputStream(outputXml);
		OutputStream outRel = new FileOutputStream(outputRelXml);
		return new CclStreamWriter(out, outRel);
	}

    public AbstractDocumentWriter getTEIWriter(String outputFolder) throws Exception{
    	return this.getTEIWriter(outputFolder, false);
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
        OutputStream text = getOutputStream(
        		new File(outputFolder,"text.xml" + gzExt).getPath(), gz);
        OutputStream annSegmentation = getOutputStream(
        		new File(outputFolder,"ann_segmentation.xml" + gzExt).getPath(), gz);
        OutputStream annMorphosyntax = getOutputStream(
        		new File(outputFolder,"ann_morphosyntax.xml" + gzExt).getPath(), gz);
        OutputStream annNamed = getOutputStream(
        		new File(outputFolder,"ann_named.xml" + gzExt).getPath(), gz);
        OutputStream annMentions = getOutputStream(
        		new File(outputFolder,"ann_mentions.xml" + gzExt).getPath(), gz);
        OutputStream annChunks = getOutputStream(
        		new File(outputFolder,"ann_chunks.xml" + gzExt).getPath(), gz);
        OutputStream annCoreference = getOutputStream(
        		new File(outputFolder,"ann_coreference.xml" + gzExt).getPath(), gz);
        OutputStream annRelations = getOutputStream(
        		new File(outputFolder,"ann_relations.xml" + gzExt).getPath(), gz);
        return new TEIStreamWriter(text, annSegmentation, annMorphosyntax, annNamed, 
        		annMentions, annChunks, annCoreference, annRelations, new File(outputFolder).getName());
    }

	private OutputStream getOutputStream(String outputFile, boolean gz) throws Exception {
		if ((outputFile == null) || (outputFile.isEmpty()))
			return System.out;
		else {
			try {
				OutputStream output = new FileOutputStream(outputFile); 
				if ( gz ){
					output = new GZIPOutputStream(output);
				}
				return output;
			} catch (IOException ex) {
				throw new Exception("Unable to write output file: " + outputFile);
			}
		}
	}
}
