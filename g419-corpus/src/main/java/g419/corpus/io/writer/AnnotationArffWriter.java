package g419.corpus.io.writer;


import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

/**
 * Created by michal on 7/16/14.
 */
public class AnnotationArffWriter {

    private BufferedWriter ow;

    public AnnotationArffWriter(OutputStream os, List<String> features){
        this.ow = new BufferedWriter(new OutputStreamWriter(os));
        writeDocumentStart(features);
    }

    private void writeDocumentStart(List<String> features){
        try {
        for (String feature: features) {
            String line = "@attribute " + feature + " string";
                ow.write(line, 0, line.length());
                ow.newLine();
            }
            ow.newLine();
            String line = "@data";
            ow.write(line, 0, line.length());
            ow.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeAnnotation(String label, List<? extends Object> features) throws IOException {
        String line = "";
        for(Object feature: features){
            if (feature == null)
                feature = "?";
            else
                feature = "\'" + feature.toString().replace("\'", "\\\'") + "\'";
            line += (line.length() > 0 ? "," : "") + feature;
        }
        line += ","+label;
        ow.write(line, 0, line.length());
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
