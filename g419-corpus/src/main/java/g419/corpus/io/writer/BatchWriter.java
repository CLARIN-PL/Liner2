package g419.corpus.io.writer;

import g419.corpus.structure.Document;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;


/**
 * 
 * @author Michał Marcińczuk
 *
 */
public class BatchWriter extends AbstractDocumentWriter {

	private String outputRootFolder = null;
	private String format;
    private String extension = "txt";
    private BufferedWriter indexWriter;
    private String gzExtension = "";

	/**
	 * 
	 * @param outputIndex -- path that will be appended to the document URI
	 */
	public BatchWriter(String outputIndex, String format) throws IOException {
        this.format = format;
        boolean gz = false;

        String outputFormatNoGz = format; 
		if ( format.endsWith(":gz") ){
			outputFormatNoGz = format.substring(0, format.length()-3);
			gz = true;
		}
		
        File index = new File(outputIndex);
        if ( !index.getParentFile().exists() ){
        	index.getParentFile().mkdirs();
        }
        this.outputRootFolder = index.getAbsoluteFile().getParent();
        indexWriter = new BufferedWriter(new FileWriter(index, false));
        if(outputFormatNoGz.startsWith("ccl") ){
            extension = ".xml";
        }
        else if(outputFormatNoGz.equals("iob")){
            extension = ".iob";
        }
        else if(outputFormatNoGz.equals("tuples")){
            extension = ".txt";
        }
        else if(outputFormatNoGz.equals("tokens")){
            extension = ".txt";
        }
        else if(outputFormatNoGz.equals("arff")){
            extension = ".arff";
        }
        else if(outputFormatNoGz.equals("tei")){
            extension = "";
        }
        else if(outputFormatNoGz.equals("json-frames")){
            extension = ".txt";
        }
        else if(outputFormatNoGz.equals("verb_eval")){
            extension = ".az";
        }
        
        if ( !"tei".equals(outputFormatNoGz) && gz ){
        	this.gzExtension += ".gz";
        }

	}
	
	@Override
	public void flush() {
		/** Nothing to do for this writer. */
	}
	
	@Override
	public void close() {
        try {
            indexWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	@Override
	public void writeDocument(Document document){
		String name = document.getName();
		if ( name == null ){
			System.err.println("Error: Document name is not specified (null value)");
		}
		else{
            int dotIdx = name.lastIndexOf("."); // if input format is tei 'name' is directory, not file
            name = (dotIdx != -1 ? name.substring(0, dotIdx) : name)  + this.extension;
            File file = new File(this.outputRootFolder, name);
            if ( !file.getParentFile().exists() ){
            	file.getParentFile().mkdirs();
            }
			try {
                AbstractDocumentWriter writer = WriterFactory.get().getStreamWriter(file.getAbsolutePath(), this.format);
				writer.writeDocument(document);
				writer.close();
                indexWriter.write(name + this.gzExtension + "\n");
                indexWriter.flush();
			} catch (FileNotFoundException e) {
				System.err.println("Error: FileNotFoundException " + e.getMessage());
				e.printStackTrace();
			} catch (Exception e) {
                e.printStackTrace();
            }
        }
				
	}	

}
