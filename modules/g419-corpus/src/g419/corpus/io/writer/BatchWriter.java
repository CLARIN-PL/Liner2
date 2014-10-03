package g419.corpus.io.writer;

import g419.corpus.structure.Document;

import java.io.*;


/**
 * 
 * @author Michał Marcińczuk
 *
 */
public class BatchWriter extends AbstractDocumentWriter {

	private String outputIndex = null;
	private String format;
    private String extension;

	/**
	 * 
	 * @param outputIndex -- path that will be appended to the document URI
	 */
	public BatchWriter(String outputIndex, String format) {
		this.outputIndex = outputIndex;
        this.format = format;
        if(format.equals("ccl")){
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
		/** Nothing to do for this writer. */
	}

	@Override
	public void writeDocument(Document document){
		String name = document.getName();
		if ( name == null ){
			System.err.println("Error: Document name is not specified (null value)");
		}
		else{
			File index = new File(this.outputIndex);
            String rootDir = index.getAbsoluteFile().getParent();
            int dotIdx = name.lastIndexOf("."); // if input format is tei 'name' is directory, not file
            name = (dotIdx != -1 ? name.substring(0, dotIdx) : name)  + this.extension;
            File file = new File(rootDir, name);
			try {
                AbstractDocumentWriter writer;
                if (this.format.equals("tei")){
                    file.mkdirs();
                    writer = WriterFactory.get().getTEIWriter(file.getAbsolutePath());
                }
                else{
                    file.getParentFile().mkdirs();
                    writer = WriterFactory.get().getStreamWriter(new FileOutputStream(file), this.format);
                }
				writer.writeDocument(document);
				writer.close();
                BufferedWriter indexWriter = new BufferedWriter(new FileWriter(index, true));
                indexWriter.write(name+"\n");
                indexWriter.close();
			} catch (FileNotFoundException e) {
				System.err.println("Error: FileNotFoundException " + e.getMessage());
				e.printStackTrace();
			} catch (Exception e) {
                e.printStackTrace();
            }
        }
				
	}	

}
