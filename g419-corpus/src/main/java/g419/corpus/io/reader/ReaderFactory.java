package g419.corpus.io.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;


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
		
		boolean gz = false;
		String intpuFormatNoGz = inputFormat;
		if ( inputFormat.endsWith(":gz") ){
			gz = true;
			intpuFormatNoGz = inputFormat.substring(0, inputFormat.length() - 3);
		}		
		
        if (inputFile == null){
		    return getStreamReader("System.in", System.in, null, intpuFormatNoGz, gz);        	
        }
        else if (intpuFormatNoGz.equals("tei")){
            return getTEIStreamReader(inputFile, inputFile, gz);
        }
        else if (intpuFormatNoGz.startsWith("batch:")){
            String format = inputFormat.substring(6);
            String root = new File(inputFile).getAbsoluteFile().getParent();
            return new BatchReader(new FileInputStream(inputFile), root, format);
        }
        else{
		    return getStreamReader(inputFile, new FileInputStream(inputFile), (new File(inputFile)).getParent(), intpuFormatNoGz, gz);
        }
	}

	/**
	 * 
	 * @param uri
	 * @param in
	 * @param inputFormat
	 * @return
	 * @throws Exception
	 */
	public AbstractDocumentReader getStreamReader(String uri, InputStream in, String inputFormat) throws Exception {
		return getStreamReader(uri, in, "", inputFormat);
	}

	/**
	 * 
	 * @param uri
	 * @param in
	 * @param root
	 * @param inputFormat
	 * @return
	 * @throws Exception
	 */
	public AbstractDocumentReader getStreamReader(String uri, InputStream in, String root, String inputFormat) throws Exception {
		boolean gz = false;
		if ( inputFormat.endsWith(":gz") ){
			gz = true;
			inputFormat = inputFormat.substring(0, inputFormat.length() - 3);
		}
		return this.getStreamReader(uri, in, root, inputFormat, gz);		
	}

	/**
	 * 
	 * @param uri
	 * @param in
	 * @param root
	 * @param inputFormat
	 * @param gz
	 * @return
	 * @throws Exception
	 */
	public AbstractDocumentReader getStreamReader(String uri, InputStream in, String root, String inputFormat, boolean gz) throws Exception {
		if ( gz ){
			in = new GZIPInputStream(in);
		}
		if (inputFormat.equals("ccl")){
			InputStream desc = getInputStream(root, uri.replace(".xml", ".ini"), gz);
			return new CclSAXStreamReader(uri, in, desc, null);
		}
		else if (inputFormat.equals("cclrel")){
			InputStream rel = getInputStream(root, uri.replace(root, "").replace(".xml", ".rel.xml"), gz);
			InputStream desc = getInputStream(root, uri.replace(".xml", ".ini"), gz);
			return new CclSAXStreamReader(uri, in, desc, rel);
		}
		else if (inputFormat.equals("cclrelr")){
			InputStream rel = getInputStream(root, uri.replace(".xml", ".rel_r"), gz);
			InputStream desc = getInputStream(root, uri.replace(".xml", ".ini"), gz);
			return new CclSAXStreamReader(uri, in, desc, rel);
		}
		else if (inputFormat.equals("cclrelcls")){
			InputStream rel = getInputStream(root, uri.replace(".xml", ".rel_cls"), gz);
			InputStream desc = getInputStream(root, uri.replace(".xml", ".ini"), gz);
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
	 * 
	 * @param inputFolder
	 * @param docname
	 * @return
	 * @throws Exception
	 */
    public AbstractDocumentReader getTEIStreamReader(String inputFolder, String docname) throws Exception{    	
    	return this.getTEIStreamReader(inputFolder, docname, false);
    }
    
	/**
	 * Creates reader for a document in the TEI format -- 
	 * @param inputFolder
	 * @param docname
	 * @return
	 * @throws Exception
	 */
    public AbstractDocumentReader getTEIStreamReader(String inputFolder, String docname, boolean gz) throws Exception{
        InputStream annMorphosyntax = this.getInputStream(inputFolder, "ann_morphosyntax.xml", gz);
        InputStream annSegmentation = this.getInputStream(inputFolder, "ann_segmentation.xml", gz);
        InputStream annNamed = this.getInputStream(inputFolder, "ann_named.xml", gz);
        InputStream annMentions = this.getInputStream(inputFolder, "ann_mentions.xml", gz);
        InputStream annChunks = this.getInputStream(inputFolder, "ann_chunks.xml", gz);
        InputStream annCoreference = this.getInputStream(inputFolder, "ann_coreference.xml", gz);
        InputStream annGroups = this.getInputStream(inputFolder, "ann_groups.xml", gz);
        InputStream annWords = this.getInputStream(inputFolder, "ann_words.xml", gz);
        InputStream annRelations = this.getInputStream(inputFolder, "ann_relations.xml", gz);
        InputStream annProps = this.getInputStream(inputFolder, "ann_props.xml", gz);

        return new TEIStreamReader(inputFolder, annMorphosyntax, annProps, annSegmentation, annNamed, annMentions, annChunks,
        		annCoreference, annWords, annGroups, annRelations, docname);
    }

    /**
     * Tworzy strumień do wczytania danych. 
     * Jeżeli inputFile jest null to strumień danych wczytywany jest z System.in.
     * Jeżeli inputFile jest nazwą pliku, który nie istnieje to zwracany jest null.
     * Wpp. dane wczytywane są z pliku. Jeżeli gz jest true, to strumień zostaje opakowany w GZIPStreamInput.
     * @param inputFile
     * @param gz
     * @return
     * @throws Exception
     */
	private InputStream getInputStream(String inputFolder, String inputFile, boolean gz) throws Exception {
		if ((inputFile == null) || (inputFile.isEmpty()))
			return System.in;
		else {
			try {
				if ( gz && !inputFile.endsWith(".gz") ){
					inputFile += ".gz";					 
				}
				File file = new File(inputFolder, inputFile);
				InputStream stream = null;
				if ( file.exists() ){
					stream = new FileInputStream(file);
					if ( gz ){
						stream = new GZIPInputStream(stream);
					}
					return stream;
				}
				else
					return null;
			} catch (IOException ex) {
				throw new Exception("Unable to read input file: " + inputFile);
			}
		}
	}
}
