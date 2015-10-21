package g419.corpus.io.writer;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
        else if("ccl_rel".equals(outputFormat)){
        	return getCclRelWriter(outputFile);
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
        else if (outputFormat.equals("conll"))
			return new ConllStreamWriter(out);
        else if (outputFormat.equals("zero_verb"))
			return new ZeroVerbWriter(out);
		else if (outputFormat.equals("iob-tab"))
			return new IobTabStreamWriter(out);
		else if (outputFormat.equals("tuples"))
			return new TuplesStreamWriter(out);
		else if (outputFormat.equals("json"))
			return new JSONStreamWriter(out);
		else if (outputFormat.equals("tokens"))
			return new TokensStreamWriter(out);
        else if (outputFormat.equals("arff"))
            return new ArffStreamWriter(out);
        else if (outputFormat.equals("verb_eval"))
            return new MinosVerbEvalWriter(out);
        else if (outputFormat.equals("simple_rel"))
        	return new SimpleRelationClusterSetWriter(out);
        else if (outputFormat.equals("relation-tuples"))
        	return new RelationTuplesWriter(out);
        else if (outputFormat.equals("conll"))
        	return new ConllStreamWriter(out);
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
        if(outputFolder == null){
            throw new FileNotFoundException("TEI format requires existing folder as a target (-t) parameter value)");
        }
        File folder = new File(outputFolder);
        if ( !folder.exists() ){
        	folder.mkdirs();
        }
        OutputStream text = getOutputStream(new File(outputFolder,"text.xml").getPath());
        OutputStream annSegmentation = getOutputStream(new File(outputFolder,"ann_segmentation.xml").getPath());
        OutputStream annMorphosyntax = getOutputStream(new File(outputFolder,"ann_morphosyntax.xml").getPath());
        OutputStream annNamed = getOutputStream(new File(outputFolder,"ann_named.xml").getPath());
        OutputStream annMentions = getOutputStream(new File(outputFolder,"ann_mentions.xml").getPath());
        OutputStream annChunks = getOutputStream(new File(outputFolder,"ann_chunks.xml").getPath());
        OutputStream annCoreference = getOutputStream(new File(outputFolder,"ann_coreference.xml").getPath());
        OutputStream annRelations = getOutputStream(new File(outputFolder,"ann_relations.xml").getPath());
        return new TEIStreamWriter(text, annSegmentation, annMorphosyntax, annNamed, 
        		annMentions, annChunks, annCoreference, annRelations, new File(outputFolder).getName());
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
