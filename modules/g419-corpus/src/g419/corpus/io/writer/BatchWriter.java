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
    private String extension;
    private BufferedWriter indexWriter;

	/**
	 * 
	 * @param outputIndex -- path that will be appended to the document URI
	 */
	public BatchWriter(String outputIndex, String format) throws IOException {
        File index = new File(outputIndex);
        this.outputRootFolder = index.getAbsoluteFile().getParent();
        indexWriter = new BufferedWriter(new FileWriter(index, false));
        this.format = format;
        if(format.equals("ccl") || format.equals("ccl_rel")){
            extension = ".xml";
        }
        else if(format.equals("iob")){
            extension = ".iob";
        }
        else if(format.equals("tuples")){
            extension = ".txt";
        }
        else if(format.equals("tokens")){
            extension = ".txt";
        }
        else if(format.equals("arff")){
            extension = ".arff";
        }
        else if(format.equals("tei")){
            extension = "";
        }
        else if(format.equals("verb_eval")){
            extension = ".az";
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
			try {
                AbstractDocumentWriter writer;
                if (this.format.equals("tei")){
                    file.mkdirs();
                    writer = WriterFactory.get().getTEIWriter(file.getAbsolutePath());
                }
                else if( this.format.equals("ccl_rel")){
                	file.getParentFile().mkdirs();
                	writer = WriterFactory.get().getCclRelWriter(file.getAbsolutePath());
                }
                else{
                    file.getParentFile().mkdirs();
                    writer = WriterFactory.get().getStreamWriter(new FileOutputStream(file), this.format);
                }
				writer.writeDocument(document);
				writer.close();
                indexWriter.write(name+"\n");
			} catch (FileNotFoundException e) {
				System.err.println("Error: FileNotFoundException " + e.getMessage());
				e.printStackTrace();
			} catch (Exception e) {
                e.printStackTrace();
            }
        }
				
	}	

}
