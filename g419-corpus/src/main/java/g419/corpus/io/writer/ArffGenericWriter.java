package g419.corpus.io.writer;


import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

/**
 * @author Michał Krautforst
 */
public class ArffGenericWriter extends AbstractMatrixWriter {

    private BufferedWriter ow;

    /**
     * 
     * @param os
     * @param relationName Relation name which is placed in the header after "@RELATION"
     * @param features
     */
    public ArffGenericWriter(OutputStream os){
        this.ow = new BufferedWriter(new OutputStreamWriter(os));
    }

    /**
     * 
     */
    public void flush() {
        try {
            ow.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 
     */
    public void close() {
        try {
            ow.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

	@Override
	public void writeHeader(String name, List<String> headers) {
        try {
        	ow.write(String.format("@RELATION %s", name));
        	ow.newLine();        	
	        for (String feature: headers) {
	            ow.write(String.format("@ATTRIBUTE %s STRING", feature));
	            ow.newLine();
            }
	        ow.write(String.format("@ATTRIBUTE category STRING"));
            ow.newLine();
            ow.newLine();
            ow.write("@DATA");
            ow.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }	}

	@Override
	public void writeRow(List<String> values) throws IOException {
        String line = "";
        for(Object feature: values){
            if (feature == null){
                feature = "?";
            } else {
                feature = "\'" + feature.toString().replace("\'", "\\\'") + "\'";
            }
            line += (line.length() > 0 ? "," : "") + feature;
        }
        ow.write(line, 0, line.length());
        ow.newLine();
	}
}
