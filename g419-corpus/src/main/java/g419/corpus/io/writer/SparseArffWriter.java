package g419.corpus.io.writer;


import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * Zapis do rzadkiego formatu ARFF.
 * https://weka.wikispaces.com/ARFF+%28stable+version%29
 * 
 * @author czuk
 *
 */
public class SparseArffWriter {

    private BufferedWriter ow = null;

    public SparseArffWriter(OutputStream os, String name, List<String> features, Set<String> classes){
        this.ow = new BufferedWriter(new OutputStreamWriter(os));
        writeDocumentStart(name, features, classes);
    }

    private void writeDocumentStart(String name, List<String> features, Set<String> classes){
        try {
        	ow.write(String.format("@relation %s", name));
        	ow.newLine();
        	ow.newLine();
	        for (String feature: features) {
	            String line = "@attribute " + feature.replace(" ", "_").replace("\"", "PARENTHESIS") + " Integer"; //" string";
	                ow.write(line, 0, line.length());
	                ow.newLine();
            }
	        String lineClass = "@attribute class {";
	        Iterator<String> it = classes.iterator();
	        while ( it.hasNext() ){
	        	lineClass += it.next();
	        	if ( it.hasNext() ){
	        		lineClass +=",";
	        	}
	        	
	        }
	        ow.write(lineClass + "}");
            ow.newLine();
            String line = "@data";
            ow.write(line, 0, line.length());
            ow.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Jeżeli cecha ma wartość null lub "0" to jest pomijana przy zapisie.
     * @param label
     * @param features
     * @throws IOException
     */
    public void writeInstance(String label, List<? extends Object> features) throws IOException {
        StringBuilder line = new StringBuilder();
        int featureIndex = 0;
        line.append("{");
        for(Object feature: features){
            if (feature == null || feature.toString().equals("0") ){
                feature = "?";
            }
            else{
                //feature = "\'" + feature.toString().replace("\'", "\\\'") + "\'";
                feature = feature.toString();
                line.append(String.format("%d %s, ", featureIndex, feature));
            }
            featureIndex++;
        }
        line.append(String.format("%d '%s'}, ", featureIndex, label));
        ow.write(line.toString());
        ow.newLine();
    }

    public void flush() {
        try {
            ow.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            ow.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
