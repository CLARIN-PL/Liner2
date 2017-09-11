package g419.corpus.io.writer;


import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
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
    private List<String> attributeNames = new ArrayList<String>();
    private Map<String, ArffAttributeType> attributes = null;

    public SparseArffWriter(OutputStream os, String name, Map<String, ArffAttributeType> attributes, Set<String> classes){
        this.ow = new BufferedWriter(new OutputStreamWriter(os));
    	this.attributeNames.addAll(attributes.keySet());
    	this.attributes = attributes;
        writeDocumentStart(name, classes);
    }

    private void writeDocumentStart(String name, Set<String> classes){
        try {
        	ow.write(String.format("@relation %s", name));
        	ow.newLine();
        	ow.newLine();
	        for ( String attribute : attributeNames ) {
	            String line = "@attribute " + attribute.replace(" ", "_").replace("\"", "PARENTHESIS") + " " + attributes.get(attribute) ;
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
    public void writeInstance(String label, Map<String, ? extends Object> instanceAttributes) throws IOException {
        StringBuilder line = new StringBuilder();
        int featureIndex = 0;
        line.append("{");
        for( String attributeName : this.attributeNames ){
        	ArffAttributeType attributeType = this.attributes.get(attributeName);
        	Object value = instanceAttributes.get(attributeName);
        	String valueStr = "";
            if (value == null || value.equals("") || value.toString().equals("0") ){
            	valueStr = "?";
            } else {
            	valueStr = value.toString();
                if ( attributeType == ArffAttributeType.STRING ){
                	valueStr = String.format("'%s'", valueStr.replaceAll("'", "\'"));
                }
            }
            line.append(String.format("%d %s, ", featureIndex, valueStr));
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
