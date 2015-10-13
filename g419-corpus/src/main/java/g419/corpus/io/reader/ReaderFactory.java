package g419.corpus.io.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


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
	public AbstractDocumentReader getStreamReader(String inputFile, String inputFormat) throws Exception {
        if (inputFile == null){
		    return getStreamReader(
		    		"System.in",
		    		System.in,
		    		null,
		    		inputFormat);        	
        }
        else if (inputFormat.equals("tei")){
            return getTEIStreamReader(inputFile, inputFile);
        }
        else if (inputFormat.startsWith("batch:")){
            String format = inputFormat.substring(6);
            return new BatchReader(getInputStream(inputFile), (new File(inputFile)).getAbsoluteFile().getParent(), format);
        }
        else{
		    return getStreamReader(
		    		inputFile,
		    		getInputStream(inputFile),
		    		(new File(inputFile)).getParent(),
		    		inputFormat);
        }
	}

	public AbstractDocumentReader getStreamReader(String uri, InputStream in, String inputFormat) throws Exception {
		return getStreamReader(uri, in, "", inputFormat);
	}

	public AbstractDocumentReader getStreamReader(String uri, InputStream in, String root, String inputFormat) throws Exception {
		if (inputFormat.equals("ccl")){
			InputStream desc = null;
			try {
				desc = getInputStream(new File(root, uri.replace(".xml", ".ini")).getPath());
			} catch (Exception e){}
			return new CclSAXStreamReader(uri, in, desc, null);
		}
		else if (inputFormat.equals("ccl_rel")){
			InputStream rel = null;
			InputStream desc = null;
			try{
				rel = getInputStream(new File(root, uri.replace(root, "").replace(".xml", ".rel.xml")).getPath());
			}
			catch(Exception e){
				e.printStackTrace();
			}
			try {
				desc = getInputStream(new File(root, uri.replace(".xml", ".ini")).getPath());
			} catch (Exception e){}
			return new CclSAXStreamReader(uri, in, desc, rel);
		}
		else if (inputFormat.equals("ccl_relr")){
			InputStream rel = null;
			InputStream desc = null;
			try{
				rel = getInputStream(new File(root, uri.replace(".xml", ".rel_r")).getPath());
			} catch(Exception e){}
			try {
				desc = getInputStream(new File(root, uri.replace(".xml", ".ini")).getPath());
			} catch (Exception e){}
			return new CclSAXStreamReader(uri, in, desc, rel);
		}
		else if (inputFormat.equals("ccl_relcls")){
			InputStream rel = null;
			InputStream desc = null;
			try{
				rel = getInputStream(new File(root, uri.replace(".xml", ".rel_cls")).getPath());
			}catch(Exception e){}
			try {
				desc = getInputStream(new File(root, uri.replace(".xml", ".ini")).getPath());
			} catch (Exception e){}
			return new CclSAXStreamReader(uri, in, desc, rel);
		}
		else if (inputFormat.equals("iob"))
			return new IobStreamReader(in);
		else if (inputFormat.equals("plain"))
			return new PlainTextStreamReader(uri, in, "none");
		else if (inputFormat.equals("plain:maca"))
			return new PlainTextStreamReader(uri, in, "maca");
		else if (inputFormat.equals("plain:wcrft"))
			return new PlainTextStreamReader(uri, in, "wcrft");
		else
			throw new Exception("Input format " + inputFormat + " not recognized.");
	}

	/**
	 * Creates reader for a document in the TEI format -- 
	 * @param inputFolder
	 * @param docname
	 * @return
	 * @throws Exception
	 */
    public AbstractDocumentReader getTEIStreamReader(String inputFolder, String docname) throws Exception{    	
        InputStream annMorphosyntax = getInputStream(new File(inputFolder,"ann_morphosyntax.xml").getPath());
        InputStream annSegmentation = getInputStream(new File(inputFolder,"ann_segmentation.xml").getPath());
        InputStream annNamed = null; 
        InputStream annMentions = null;
        InputStream annChunks = null; 
        InputStream annCoreference = null; 
        InputStream annGroups = null; 
        InputStream annWords = null;
        InputStream annRelations = null;

        File fileNamed = new File(inputFolder,"ann_named.xml");
        if ( fileNamed.exists() ){
        	annNamed = getInputStream(fileNamed.getPath());
        }
        
        File fileMentions = new File(inputFolder,"ann_mentions.xml");
        if ( fileMentions.exists() ){
        	annMentions = getInputStream(fileMentions.getPath());
        }

        File fileChunks = new File(inputFolder,"ann_chunks.xml");
        if ( fileChunks.exists() ){
        	annChunks = getInputStream(fileChunks.getPath());
        }

        File fileCoreference = new File(inputFolder,"ann_coreference.xml");
        if ( fileCoreference.exists() ){
        	annCoreference = getInputStream(fileCoreference.getPath());
        }

        File fileWords = new File(inputFolder,"ann_words.xml");
        if ( fileWords.exists() ){
        	annWords = getInputStream(fileWords.getPath());
        }

        File fileGroups = new File(inputFolder,"ann_groups.xml");
        if ( fileGroups.exists() ){
        	annGroups = getInputStream(fileGroups.getPath());
        }

        File fileRelations = new File(inputFolder,"ann_relations.xml");
        if ( fileRelations.exists() ){
        	annRelations = getInputStream(fileRelations.getPath());
        }

        return new TEIStreamReader(inputFolder, annMorphosyntax, annSegmentation, annNamed, annMentions, annChunks,
        		annCoreference, annWords, annGroups, annRelations, docname);
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
