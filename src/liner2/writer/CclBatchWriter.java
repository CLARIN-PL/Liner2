package liner2.writer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import liner2.structure.Document;

/**
 * 
 * @author Michał Marcińczuk
 *
 */
public class CclBatchWriter extends AbstractDocumentWriter {

	private String outputRoot = null;
	
	/**
	 * 
	 * @param outputUriRoot -- path that will be appended to the document URI
	 */
	public CclBatchWriter(String outputRoot) {
		this.outputRoot = outputRoot;
	}
	
	@Override
	public void flush() {
		/** Nothing to do for this writer. */
	}
	
	@Override
	public void close() {
		/** Nothing to do for this writer. */
	}

	@Override
	public void writeDocument(Document document){
		String name = document.getName();
		if ( name == null ){
			System.err.println("Error: Document name is not specified (null value)");
		}
		else{
			File uriRoot = new File(this.outputRoot);
			File file = new File(uriRoot, name);
			file.getParentFile().mkdirs();
			try {
				CclStreamWriter writer = new CclStreamWriter(new FileOutputStream(file));
				writer.writeDocument(document);
				writer.close();
			} catch (FileNotFoundException e) {
				System.err.println("Error: FileNotFoundException " + e.getMessage());
				e.printStackTrace();
			}
		}
				
	}	

}
