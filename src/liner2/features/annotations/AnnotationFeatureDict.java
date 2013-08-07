package liner2.features.annotations;

import liner2.structure.Annotation;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

/**
 * Created with IntelliJ IDEA.
 * User: michal
 * Date: 7/30/13
 * Time: 10:53 AM
 * To change this template use File | Settings | File Templates.
 */
public class AnnotationFeatureDict extends AnnotationFeature {


    private HashSet<String> entries;
    private String form;

    public AnnotationFeatureDict(String dictFile, String form){
        entries = new HashSet<String>();
        this.form = form;

        try{
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(dictFile));
                String line = br.readLine();
                while (line != null) {
                    entries.add(line.toLowerCase());
                    line = br.readLine();
                }
            } catch (FileNotFoundException e) {
              System.out.println("Dictionary file not found: "+dictFile);
            } finally {
                br.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public String generate(Annotation an) {
        String annotationText;
        if(form.equals("orth"))
            annotationText = an.getText();
        else if(form.equals("base"))
            an.getBaseText();
       return entries.contains(an.getText().toLowerCase())? "1": "0";
    }
}
