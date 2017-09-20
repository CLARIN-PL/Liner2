package g419.corpus.io.writer;


import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

/**
 * @author Michał Marcińczuk
 */
public class CsvGenericWriter extends AbstractMatrixWriter {

	private CSVPrinter writer = null;
	
    /**
     * 
     * @param os
     * @param relationName Relation name which is placed in the header after "@RELATION"
     * @param features
     * @throws IOException 
     */
    public CsvGenericWriter(OutputStream os) throws IOException{
		this.writer = new CSVPrinter(new OutputStreamWriter(os), CSVFormat.DEFAULT);
    }

    /**
     * 
     */
    public void flush() {
        try {
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 
     */
    public void close() {
        try {
            writer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

	@Override
	public void writeHeader(String name, List<String> headers) throws IOException {
		writer.printRecord(headers);
    }

	@Override
	public void writeRow(List<String> values) throws IOException {
		writer.printRecord(values);
	}
}
