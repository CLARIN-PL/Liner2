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
            return new BatchReader(getInputStream(inputFile), (new File(inputFile)).getParent(), format);
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
			return new CclSAXStreamReader(uri, in, null);
		}
		else if (inputFormat.equals("ccl_rel")){
			InputStream rel = null;
			try{
				rel = getInputStream(new File(root, uri.replace(root, "").replace(".xml", ".rel.xml")).getPath());
			}
			catch(Exception e){
				e.printStackTrace();
			}
			return new CclSAXStreamReader(uri, in, rel);
		}
		else if (inputFormat.equals("ccl_relr")){
			InputStream rel = null;
			try{
				rel = getInputStream(new File(root, uri.replace(".xml", ".rel_r")).getPath());
			}
			catch(Exception e){}
			return new CclSAXStreamReader(uri, in, rel);
		}
		else if (inputFormat.equals("ccl_relcls")){
			InputStream rel = null;
			try{
				rel = getInputStream(new File(root, uri.replace(".xml", ".rel_cls")).getPath());
			}
			catch(Exception e){}
			return new CclSAXStreamReader(uri, in, rel);
		}
		else if (inputFormat.equals("iob"))
			return new IobStreamReader(in);
		else if (inputFormat.equals("plain"))
			return new PlainTextStreamReader(in, "none");
		else if (inputFormat.equals("plain:maca"))
			return new PlainTextStreamReader(in, "maca");
		else if (inputFormat.equals("plain:wcrft"))
			return new PlainTextStreamReader(in, "wcrft");
		else
			throw new Exception("Input format " + inputFormat + " not recognized.");
	}

    public AbstractDocumentReader getTEIStreamReader(String inputFolder, String docname) throws Exception{
        InputStream annMorphosyntax = getInputStream(new File(inputFolder,"ann_morphosyntax.xml").getPath());
        InputStream annSegmentation = getInputStream(new File(inputFolder,"ann_segmentation.xml").getPath());
        InputStream annNamed = null;//getInputStream(new File(inputFolder,"ann_named.xml").getPath());
//        InputStream annMentions = getInputStream(new File(inputFolder,"ann_mentions.xml").getPath());
//        InputStream annCoreference = getInputStream(new File(inputFolder,"ann_coreference.xml").getPath());
        return new TEIStreamReader(annMorphosyntax, annSegmentation, annNamed, null, null, docname);
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
