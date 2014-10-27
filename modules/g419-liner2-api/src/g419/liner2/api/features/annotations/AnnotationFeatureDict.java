package g419.liner2.api.features.annotations;


import g419.corpus.structure.Annotation;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.zip.DataFormatException;

/**
 * Created with IntelliJ IDEA.
 * User: michal
 * Date: 7/30/13
 * Time: 10:53 AM
 * To change this template use File | Settings | File Templates.
 */
public class AnnotationFeatureDict extends AnnotationAtomicFeature {

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
        String annotationText = null;
        if(form.equals("orth"))
            annotationText = an.getText();
        else if(form.equals("base"))
            annotationText = an.getBaseText();

        if(entries.contains(annotationText.toLowerCase())){
            return "E";
        }
        else{
            ArrayList<Token> sentenceTokens = an.getSentence().getTokens();
            for(int tokenIdx: an.getTokens()){
                String tokenText = sentenceTokens.get(tokenIdx).getAttributeValue(form).toLowerCase();
                if(entries.contains(tokenText)){
                    return "C";
                }
            }
            return "O";
        }

    }
}
