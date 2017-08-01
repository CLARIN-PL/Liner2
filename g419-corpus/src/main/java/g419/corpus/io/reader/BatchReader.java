package g419.corpus.io.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import g419.corpus.io.DataFormatException;
import g419.corpus.structure.Document;
import g419.corpus.structure.TokenAttributeIndex;


/**
 *
 */
public class BatchReader extends AbstractDocumentReader {

    private TokenAttributeIndex attributeIndex;
    private int fileIndex=0;
    private List<String> files = new ArrayList<String>();
    private File root = null;
    private String format = null;

    /**
     * 
     * @param is — the stream contains relative or absolute paths to ccl files,
     * @param root — absolute path to a root for absolute paths from the stream,
     * @throws DataFormatException
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    public BatchReader(InputStream is, String root, String format) throws DataFormatException, IOException{
    	this.root = new File(root);
        this.format = format;
        this.attributeIndex = new TokenAttributeIndex();
        this.attributeIndex.addAttribute("orth");
        this.attributeIndex.addAttribute("base");
        this.attributeIndex.addAttribute("ctag");
        
        BufferedReader ir = new BufferedReader(new InputStreamReader(is));
        while (true) {
            String line;
            try {
                line = ir.readLine();
            } catch (IOException ex) {
                throw new DataFormatException("I/O error.");
            }
            if(line == null){
                break;
            }
            String name = line.trim().split(";")[0];
            String cclFile = name;
            if (cclFile.length() == 0){
                break;
            }
            
            if (!cclFile.startsWith("/")){
            	cclFile = new File(this.root, cclFile).getAbsolutePath();
            }

            if (!new File(cclFile).exists()){
            	System.err.println("File not found while reading batch: " + cclFile);
            }
            else{
            	this.files.add(name);
            }
        }
        ir.close();
    }
    
    @Override
    public Document nextDocument() throws Exception {
    	while ( this.fileIndex < this.files.size() ){
        	String name = this.files.get(this.fileIndex++);
            String path;
            if(name.startsWith("/")) {
                path = name;
                File tmp = new File(path);
                name = tmp.getName();
            }
            else {
                path = new File(this.root, name).getAbsolutePath();
            }
    		try{
    			Logger.getLogger(this.getClass()).info(String.format("Reading %d from %d: %s", this.fileIndex,  this.files.size(), path));
	            AbstractDocumentReader reader = null;
	            reader = ReaderFactory.get().getStreamReader(path, this.format);
	    		Document document = reader.nextDocument();
	    		if ( name.endsWith(".gz") ){
	    			name = name.substring(0, name.length()-3);
	    		}
	    		document.setName(FilenameUtils.removeExtension(name));
	            reader.close();		            
	    		return document;
    		}
    		catch(Exception ex){
    			Logger.getLogger(this.getClass()).error("Błąd odczytu pliku: " + path, ex);
    		}
    	}
    	return null;
    }
    
    @Override
    protected TokenAttributeIndex getAttributeIndex() {
        return this.attributeIndex;
    }

    @Override
    public void close() throws DataFormatException {
    }

}
