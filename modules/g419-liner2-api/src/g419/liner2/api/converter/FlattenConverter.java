package g419.liner2.api.converter;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by michal on 9/17/14.
 */
public class FlattenConverter extends Converter {

    ArrayList<String> categories;
    private Comparator<Annotation> flattenConparator;

    public FlattenConverter(String categoriesFile){
        try {
            parseCategories(categoriesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        flattenConparator = new Comparator<Annotation>() {
            public int compare(Annotation a, Annotation b) {
                if(Collections.disjoint(a.getTokens(), b.getTokens())){
                    return 0;
                }
                else {
                    if (a.getTokens().size() == b.getTokens().size()) {
                        return Integer.signum(categories.indexOf(a.getType()) - categories.indexOf(b.getType()));
                    }
                    return Integer.signum(b.getTokens().size() - a.getTokens().size());
                }
            }
        };

    }
    @Override
    public void finish(Document doc) {

    }

    @Override
    public void apply(Sentence sentence) {
        ArrayList<Annotation> toFlatten = new ArrayList<Annotation>();
        for(Annotation ann: sentence.getChunks()){
            if(categories.contains(ann.getType())){
                toFlatten.add(ann);
            }
        }
        for(Annotation annToRemove: flatten(toFlatten)){
            sentence.getChunks().remove(annToRemove);
        }
    }

    private HashSet<Annotation> flatten(ArrayList<Annotation> toFlatten){
        HashSet<Annotation> toRemove = new HashSet<Annotation>();
        for(Annotation ann: toFlatten){
            for(Annotation candidate: toFlatten){
                if(flattenConparator.compare(ann, candidate) == -1){
                    toRemove.add(candidate);
                }
            }
        }
        return toRemove;
    }

    private void parseCategories(String file) throws IOException {
        categories = new ArrayList<String>();
        BufferedReader br = new BufferedReader(new FileReader(file));

        try {
            categories.add(br.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            br.close();
        }
    }
}
