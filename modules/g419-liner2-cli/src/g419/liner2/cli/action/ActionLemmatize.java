package g419.liner2.cli.action;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.*;
import g419.lib.cli.CommonOptions;
import g419.lib.cli.action.Action;
import g419.liner2.api.features.tokens.CaseFeature;
import g419.liner2.api.features.tokens.ClassFeature;
import g419.liner2.api.tools.ValueComparator;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by michal on 2/17/15.
 */
public class ActionLemmatize extends Action {

    private String input_file = null;
    private String input_format = null;
    private String output_file = null;
    private static DecimalFormat df = new DecimalFormat("#.###");
    private HashMap<String, HashMap<String, Integer>> annsTextForms = new HashMap<>();


    public ActionLemmatize(){
        super("lemmatize");
        this.setDescription("ToDo");
        this.options.addOption(CommonOptions.getInputFileFormatOption());
        this.options.addOption(CommonOptions.getInputFileNameOption());
        this.options.addOption(CommonOptions.getOutputFileNameOption());

    }
    @Override
    public void parseOptions(String[] args) throws Exception {
        CommandLine line = new GnuParser().parse(this.options, args);
        parseDefault(line);
        this.output_file = line.getOptionValue(CommonOptions.OPTION_OUTPUT_FILE);
        this.input_file = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
        this.input_format = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT, "ccl");

    }

    @Override
    public void run() throws Exception {
        chooseMostFrequentCases(getCases());


    }

    private HashMap<String, HashMap<String, Integer>> getCases() throws Exception {
        ClassFeature classFeat = new ClassFeature("class");
        CaseFeature caseFeat = new CaseFeature("case");
        AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(this.input_file, this.input_format);
        Document ps = reader.nextDocument();
        HashMap<String, HashMap<String, Integer>> annsCases = new HashMap<>();
        while (ps != null) {
            HashMap<Sentence, AnnotationSet> chunkings = ps.getChunkings();
            for (Sentence sent : chunkings.keySet()) {
                ArrayList<Token> sentTokens = sent.getTokens();
                for (Annotation ann : sent.getChunks()) {
                    String annCase = caseFeat.generate(sentTokens.get(ann.getBegin()), sent.getAttributeIndex());
                    for (int tokIdx : ann.getTokens()) {
                        String annClass = classFeat.generate(sentTokens.get(tokIdx), sent.getAttributeIndex());
                        if (annClass != null && annClass.equals("subst")) {
                            annCase = caseFeat.generate(sentTokens.get(tokIdx), sent.getAttributeIndex());
                            break;
                        }
                    }
                    if(annCase != null){
                        String text = ann.getText().toLowerCase();
                        HashMap<String, Integer> nameCases;
                        if(annsCases.containsKey(text)){
                            nameCases = annsCases.get(text);
                        }
                        else{
                            nameCases = new HashMap<>();
                            annsCases.put(text, nameCases);
                        }

                        if (nameCases.containsKey(annCase)) {
                            nameCases.put(annCase, nameCases.get(annCase) + 1);
                        } else {
                            nameCases.put(annCase, 1);
                        }

                        //szukanie najczestszej formy w tekscie
                        HashMap<String, Integer> nameTextForms;
                        if(annsTextForms.containsKey(text)){
                            nameTextForms = annsTextForms.get(text);
                        }
                        else{
                            nameTextForms = new HashMap<>();
                            annsTextForms.put(text, nameTextForms);
                        }

                        if (nameTextForms.containsKey(ann.getText())) {
                            nameTextForms.put(ann.getText(), nameTextForms.get(ann.getText()) + 1);
                        } else {
                            nameTextForms.put(ann.getText(), 1);
                        }

                    }
                }
            }
            ps = reader.nextDocument();
        }
        reader.close();
        return annsCases;
    }

    private void chooseMostFrequentCases(HashMap<String, HashMap<String, Integer>> annsCases) throws IOException {
        BufferedWriter nomWriter = new BufferedWriter(new FileWriter(this.output_file.replace(".csv", "_nom.csv")));
        HashSet<String> nominativeNames = new HashSet<>();
        HashSet<String> otherNames = new HashSet<>();
        for(String name: annsCases.keySet()){
            if(name.equals("acc") || name.equals("nom")){
                System.out.println("WTF?");
            }
            HashMap<String, Integer> nameCases = annsCases.get(name);
            Map<String,Integer> sortedResults = ValueComparator.sortByValues(nameCases, true);
            String mostFrequentCase = (String) sortedResults.keySet().toArray()[0];
            if(mostFrequentCase.equals("nom")){
                nomWriter.write(getMostFrequentTextForm(name));
                for(String nameCase: sortedResults.keySet()){
                    nomWriter.write("\t" + nameCase + "\t" + nameCases.get(nameCase));
                }
                nomWriter.write("\n");
                nominativeNames.add(name);
            }
            else{
                otherNames.add(name);
            }

        }
        nomWriter.close();
        getClosestForms(otherNames, nominativeNames, 5);
    }

    private void getClosestForms(HashSet<String> otherNames, HashSet<String> nominativeNames, int limit) throws IOException {
        BufferedWriter othWriter = new BufferedWriter(new FileWriter(this.output_file.replace(".csv", "_oth.csv")));
        for(String name: otherNames){
            Map<String, Double> nameDistances = new TreeMap<>();
            String[] nameTokens = name.split("\\s+");
            for(String nom: nominativeNames){
                String[] nomTokens = nom.split("\\s+");
                double distance = countDistance(nameTokens, nomTokens);
                if(distance > 0){
                    nameDistances.put(nom, distance);
                }
            }
            othWriter.write(getMostFrequentTextForm(name));
            if(nameDistances.isEmpty()){
                othWriter.write("\tNOT FOUND");
            }
            else{
                Map<String, Double> sortedDistances = ValueComparator.sortByValues(nameDistances, false);
                int count = 0;
                for(String closestNom: sortedDistances.keySet()) {
                    if(count == limit) {
                        break;
                    }
                    else{
                        othWriter.write("\t" + getMostFrequentTextForm(closestNom) + "\t" + df.format(nameDistances.get(closestNom)));
                    }
                }
            }
            othWriter.write("\n");
        }
        othWriter.close();
    }

    private double countDistance(String[] name, String[] nom){
        double levenshteinSum = 0;
        double maxSum = 0;
        if(name.length != nom.length){
            return 0;
        }
        else {
            for (int i = 0; i < name.length; i++) {
                if (!name[i].equals(nom[i]) && toShortStem(name[i], nom[i])) {
                    return 0;
                }else {
                    levenshteinSum += levenshteinDistance(name[i], nom[i]);
                    maxSum += Math.max(name[i].length(), nom[i].length());
                }
            }
        }
        return levenshteinSum / maxSum;
    }

    private int levenshteinDistance(String a, String b) {
        a = a.toLowerCase();
        b = b.toLowerCase();
        // i == 0
        int [] costs = new int [b.length() + 1];
        for (int j = 0; j < costs.length; j++)
            costs[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            // j == 0; nw = lev(i - 1, j)
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= b.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]), a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[b.length()];
    }

    private boolean toShortStem(String a, String b){
        return  !String.format("%1$-" + 3 + "s", a).substring(0,3).equals(String.format("%1$-" + 3 + "s", b).substring(0,3));
    }

    private String getMostFrequentTextForm(String name){
        HashMap<String, Integer> nameTextForms = annsTextForms.get(name);
        Map<String,Integer> sortedResults = ValueComparator.sortByValues(nameTextForms, false);
        return (String) sortedResults.keySet().toArray()[0];
    }

}
