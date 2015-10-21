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
import org.apache.commons.cli.OptionBuilder;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.DataFormatException;

/**
 * Created by michal on 2/17/15.
 */
public class ActionLemmatize extends Action {


    public static final String OPTION_WRAPPED = "w";
    public static final String OPTION_WRAPPED_LONG = "wrapped";

    public static final String OPTION_DISTANCE_MEASURE = "m";
    public static final String OPTION_DISTANCE_MEASURE_LONG = "measure";

    public static final String OPTION_SUFFIXES = "s";
    public static final String OPTION_SUFFIXES_LONG = "suffixes";

    private String input_file = null;
    private String input_format = null;
    private String output_file = null;
    private HashMap<String, HashMap<String, HashMap<Integer, String>>> wrappedAnnotations = new HashMap<>();
    private static DecimalFormat df = new DecimalFormat("#.###");
    private HashMap<String, HashMap<String, Integer>> annsTextForms = new HashMap<>();
    HashMap<String, LinkedHashMap<String, HashSet<String>>> bases = new HashMap<>();
    private String measure;
    private double distanceLimit = 0;

    private static ArrayList<LinkedHashSet<String>> suffixes;


    public ActionLemmatize(){
        super("lemmatize");
        this.setDescription("ToDo");
        this.options.addOption(CommonOptions.getInputFileFormatOption());
        this.options.addOption(CommonOptions.getInputFileNameOption());
        this.options.addOption(CommonOptions.getOutputFileNameOption());

        OptionBuilder.withArgName("wrapped");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("file with wrapped annotations");
        OptionBuilder.withLongOpt(OPTION_WRAPPED_LONG);
        this.options.addOption(OptionBuilder.create(OPTION_WRAPPED));

        OptionBuilder.withArgName("measure");
        OptionBuilder.isRequired();
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("name of measure for distance between tokens (dist1, dist2)");
        OptionBuilder.withLongOpt(OPTION_DISTANCE_MEASURE_LONG);
        this.options.addOption(OptionBuilder.create(OPTION_DISTANCE_MEASURE));

        OptionBuilder.withArgName("suffixes");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("suffix pairs for names inflection");
        OptionBuilder.withLongOpt(OPTION_SUFFIXES_LONG);
        this.options.addOption(OptionBuilder.create(OPTION_SUFFIXES));

    }
    @Override
    public void parseOptions(String[] args) throws Exception {
        CommandLine line = new GnuParser().parse(this.options, args);
        parseDefault(line);
        this.output_file = line.getOptionValue(CommonOptions.OPTION_OUTPUT_FILE);
        this.input_file = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
        this.input_format = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT, "ccl");
        if(line.hasOption(OPTION_WRAPPED)){
            parseWrappedAnnotations(line.getOptionValue(OPTION_WRAPPED));
        }

        measure = line.getOptionValue(OPTION_DISTANCE_MEASURE);
        if(measure.equals("dist1")) {
            distanceLimit = 0.2;
        } else if(measure.equals("dist2")) {
            distanceLimit = 0.2;
        } else if(measure.equals("dist3")) {
            distanceLimit = 0.1;
            if(line.hasOption(OPTION_SUFFIXES)){
                loadSuffixes(line.getOptionValue(OPTION_SUFFIXES));
            }
            else{
                throw new DataFormatException("Missing -suffixes (-s) argument for dist3 measure!");
            }
        } else{
            throw new DataFormatException("Invalind measure name: " + measure);
        }
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
        HashMap<String, Double> sentence_begining = new HashMap<>();
        int num_anns = 0;
        int num_wrapped = 0;
        while (ps != null) {
            num_anns += ps.getAnnotations().size();
            HashMap<String, HashMap <Integer, String>> docWrappedAnns = wrappedAnnotations.isEmpty() ? new HashMap<>() : wrappedAnnotations.get(ps.getName());
            HashMap<Sentence, AnnotationSet> chunkings = ps.getChunkings();
            for (Sentence sent : chunkings.keySet()) {
                HashMap <Integer, String> sentWrappedAnns = docWrappedAnns.containsKey(sent.getId()) ? docWrappedAnns.get(sent.getId()) : new HashMap<>();
                ArrayList<Token> sentTokens = sent.getTokens();
                for (Annotation ann : sent.getChunks()) {
                    String annCase = "nom";
                    for (int tokIdx : ann.getTokens()) {
                        String annClass = classFeat.generate(sentTokens.get(tokIdx), sent.getAttributeIndex());
                        if (annClass != null && annClass.equals("subst")) {
                            annCase = caseFeat.generate(sentTokens.get(tokIdx), sent.getAttributeIndex());
                            break;
                        }
                    }
                    String text;
                    if(sentWrappedAnns.containsKey(ann.getBegin())){
                        text = sentWrappedAnns.get(ann.getBegin());

                    }
                    else{
                        text = ann.getText();
                        ArrayList<Token> annTokens = new ArrayList<>();
                        for(int tokIdx: ann.getTokens()){
                            annTokens.add(sentTokens.get(tokIdx));
                        }
                        getBaseForms(text, annTokens);
                    }

                    if(sentWrappedAnns.containsKey(ann.getBegin())){
                        num_wrapped++;
                    }


                    String lowerText = text.toLowerCase();
                    if(ann.getBegin() == 0){
                        if(sentence_begining.containsKey(lowerText)){
                            sentence_begining.put(lowerText, sentence_begining.get(lowerText) + 1);
                        }
                        else{
                            sentence_begining.put(lowerText, 1.0);
                        }
                    }
                    HashMap<String, Integer> nameCases;
                    if(annsCases.containsKey(lowerText)){
                        nameCases = annsCases.get(lowerText);
                    }
                    else{
                        nameCases = new HashMap<>();
                        annsCases.put(lowerText, nameCases);
                    }

                    if (nameCases.containsKey(annCase)) {
                        nameCases.put(annCase, nameCases.get(annCase) + 1);
                    } else {
                        nameCases.put(annCase, 1);
                    }

                    //szukanie najczestszej formy w tekscie
                    HashMap<String, Integer> nameTextForms;
                    if(annsTextForms.containsKey(lowerText)){
                        nameTextForms = annsTextForms.get(lowerText);
                    }
                    else{
                        nameTextForms = new HashMap<>();
                        annsTextForms.put(lowerText, nameTextForms);
                    }

                    if (nameTextForms.containsKey(text)) {
                        nameTextForms.put(text, nameTextForms.get(text) + 1);
                    } else {
                        nameTextForms.put(text, 1);
                    }
                }
            }
            ps = reader.nextDocument();
        }

        reader.close();
        HashSet<String> toRemove = new HashSet<>();
        for(String name: annsCases.keySet()){
            if(sentence_begining.containsKey(name) && ( sentence_begining.get(name) / annsCases.get(name).values().stream().reduce(0, Integer::sum)) > 0.8){
//                System.out.println("REMOVING ON THE START OF SENTENCE: " + name + " MEASURE: " + (sentence_begining.get(name)) + " / " + annsCases.get(name).values().stream().reduce(0, Integer::sum)+ " = " + (sentence_begining.get(name) / annsCases.get(name).values().stream().reduce(0, Integer::sum)));
                toRemove.add(name);
            }
        }
        for(String name: toRemove){
            annsCases.remove(name);
        }

        return annsCases;
    }

    private void getBaseForms(String annText, ArrayList<Token> annTokens){
        String lowerAnnText = annText.toLowerCase();
        LinkedHashMap<String, HashSet<String>> annBases;
        if(bases.containsKey(lowerAnnText)){
            annBases = bases.get(lowerAnnText);
        }
        else{
            annBases = new LinkedHashMap<>();
            bases.put(lowerAnnText, annBases);
        }
        for(Token tok: annTokens){
            HashSet<String> tokBases;
            if(annBases.containsKey(tok.getOrth().toLowerCase())){
                tokBases = annBases.get(tok.getOrth().toLowerCase());
            }
            else{
                tokBases = new HashSet<>();
                annBases.put(tok.getOrth().toLowerCase(), tokBases);
            }
            tok.getTags().forEach(tag -> tokBases.add(tag.getCtag().equals("ign") ? "ign" : tag.getBase().toLowerCase()));
        }

    }

    private void chooseMostFrequentCases(HashMap<String, HashMap<String, Integer>> annsCases) throws IOException {
        TreeMap<String, String> nominativeNames = new TreeMap<>();
        for(String name: annsCases.keySet()){
            String output = "";
            HashMap<String, Integer> nameCases = annsCases.get(name);
            Map<String,Integer> sortedResults = ValueComparator.sortByValues(nameCases, true);
            String mostFrequentCase = (String) sortedResults.keySet().toArray()[0];
            if(mostFrequentCase.equals("nom")){
                output += getMostFrequentTextForm(name);
                for(String nameCase: sortedResults.keySet()){
                    output += "\t" + nameCase + "\t" + nameCases.get(nameCase);
                }
                output += "\n";
                nominativeNames.put(name, output);
            }

        }
        HashSet<String> otherNames = new HashSet<>(annsCases.keySet());
        getClosestNoms(nominativeNames, annsCases);
        otherNames.removeAll(nominativeNames.keySet());
        HashMap<String, HashMap<String, String>> nom_oth_pairs = getClosestForms(otherNames, nominativeNames.keySet());


        HashSet<String> lowCountNoms = new HashSet<>();
        for(String nom: nominativeNames.keySet()){
            int group_count = getNameCount(nom, annsCases);
            if(nom_oth_pairs.containsKey(nom)){
                for(String oth: nom_oth_pairs.get(nom).keySet()){
                    group_count += getNameCount(oth, annsCases);
                }
            }
            if(group_count < 10){
                lowCountNoms.add(nom);
                nom_oth_pairs.remove(nom);
            }
        }
        lowCountNoms.forEach(nominativeNames::remove);

        BufferedWriter nomWriter = new BufferedWriter(new FileWriter(this.output_file.replace(".csv", "_nom.csv")));
        for(String nom_output: nominativeNames.values()){
            nomWriter.write(nom_output);
        }
        nomWriter.close();

        BufferedWriter othWriter = new BufferedWriter(new FileWriter(this.output_file.replace(".csv", "_oth.csv")));
        for(HashMap<String, String> oth: nom_oth_pairs.values()){
            for(String oth_output: oth.values()){
                othWriter.write(oth_output);
            }
        }
        othWriter.close();
    }

    private int getNameCount(String name, HashMap<String, HashMap<String, Integer>> annsCases){
        return annsCases.get(name).values().stream().reduce(0, Integer::sum);
    }

    private void getClosestNoms(TreeMap<String, String> nomNames, HashMap<String, HashMap<String, Integer>> annsCases) throws IOException {
        BufferedWriter closestNomWriter = new BufferedWriter(new FileWriter(this.output_file.replace(".csv", "_closest_nom.csv")));
        HashSet<String> rejectedNoms = new HashSet<>();
        main:
        for(String name: nomNames.keySet()){
            HashSet<String> otherNoms = new HashSet<>(nomNames.keySet());
            otherNoms.remove(name);
            Map<String, Double> distances = getDistances(name, otherNoms);
            int name_count =  annsCases.get(name).values().stream().reduce(0, Integer::sum);
            if(!distances.isEmpty()){
                for(String oth_name: distances.keySet()){
                    int oth_name_count = annsCases.get(oth_name).values().stream().reduce(0, Integer::sum);

                    String[] name_tokens = name.split("\\s+");
                    String[] oth_name_tokens = oth_name.split("\\s+");
                    if(name_tokens.length == oth_name_tokens.length){
                        String full_match = "";
                        match_tokens:
                        for(int i=0; i< name_tokens.length; i++){
                            String name_token = name_tokens[i];
                            String oth_name_token = oth_name_tokens[i];
                            int lcp = longestCommonPrefix(name_token.toLowerCase(), oth_name_token.toLowerCase());
                            String name_suffix = name_token.substring(lcp).toLowerCase();
                            String oth_name_suffix = oth_name_token.substring(lcp).toLowerCase();
                            LinkedHashSet<String> namesSuffixes = new LinkedHashSet<String>(){{
                                add(name_suffix);
                                add(oth_name_suffix);}};
                            for(HashSet<String> suffixPair: suffixes){
                                if(suffixPair.equals(namesSuffixes)){
                                    if(suffixPair.iterator().next().equals(name_suffix)){
                                        if(full_match.isEmpty()){
                                            full_match = "name";
                                        }
                                        else if(full_match.equals("name")){
                                            continue;
                                        }
                                        else{
                                            full_match = "none";
                                            break match_tokens;
                                        }
                                    }
                                    else{
                                        if(full_match.isEmpty()){
                                            full_match = "oth_name";
                                        }
                                        else if(full_match.equals("oth_name")){
                                            continue;
                                        }
                                        else{
                                            full_match = "none";
                                            break match_tokens;
                                        }
                                    }
                                    break;
                                }
                            }

                        }
                        if(full_match.equals("name")){
                            rejectedNoms.add(name);
                            continue main;
                        }
                        else if(full_match.equals("oth_name")){
                            rejectedNoms.add(oth_name);
                            continue main;
                        }

                    }

                    if(distances.get(oth_name) < distanceLimit){
                        if (oth_name_count > name_count){
                            rejectedNoms.add(name);
                        }
                        else if(oth_name_count == name_count){
                            rejectedNoms.add( name.length() > oth_name.length() ? name : oth_name);
                        }
                    }
                }
            }

            else if(name_count < 5){
                rejectedNoms.add(name);
            }

            closestNomWriter.write(getMostFrequentTextForm(name));
            closestNomWriter.write("\t" + annsCases.get(name).values().stream().reduce(0, Integer::sum));
            closestNomWriter.write("\t" + annsCases.get(name).get("nom"));
            closestNomWriter.write("\tREPLACE:" + (rejectedNoms.contains(name) ? "YES" : "NO"));


            closestNomWriter.write(getClosestDistancesOutput(distances, 10)); //ostatecznie 1
            if(!distances.isEmpty()){
                String closestName = distances.keySet().iterator().next();
                closestNomWriter.write("\t" + annsCases.get(closestName).values().stream().reduce(0, Integer::sum));
                closestNomWriter.write("\t" + annsCases.get(closestName).get("nom"));
            }
            closestNomWriter.write("\n");
        }
        closestNomWriter.close();
        for(String rejected_nom: rejectedNoms){
            nomNames.remove(rejected_nom);
        }
    }

    private HashMap<String, HashMap<String,String>> getClosestForms(HashSet<String> otherNames, Set<String> nominativeNames) throws IOException {
        HashMap<String, HashMap<String,String>> nom_oth_pairs = new HashMap<>();

        for(String name: otherNames){
            String output = getMostFrequentTextForm(name);
//            Map<String, Double> distances = getDistances(name, nominativeNames).entrySet().stream()
//                    .filter(entry -> entry.getValue() < distanceLimit)
//                    .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
            Map<String, Double> distances = getDistances(name, nominativeNames);
            if(!distances.isEmpty()){ /// NOT FOUND BEDA TERAZ POMIJANE W WYNIKACH
                String bestMatch = distances.keySet().iterator().next();
                output += "\tBASE:" + (distances.get(bestMatch) < distanceLimit ? "YES" : "NO");
                output += getClosestDistancesOutput(distances, 10) + "\n"; //ostatecznie 5
                if(nom_oth_pairs.containsKey(bestMatch)){
                    nom_oth_pairs.get(bestMatch).put(name, output);
                }
                else{
                    HashMap<String,String> new_oth = new HashMap<>();
                    new_oth.put(name, output);
                    nom_oth_pairs.put(bestMatch, new_oth);
                }
            }

        }
        return nom_oth_pairs;
    }

    private Map<String, Double> getDistances(String name, Set<String> otherNames){
        Map<String, Double> nameDistances = new TreeMap<>();

        for(String nom: otherNames){
            double distance = countDistance(name, nom);
            if(distance != 1){
                if (distance < 0){
                    System.out.println("dist < 0" + name + " | " + nom);
                }
                nameDistances.put(nom, distance);
            }
        }
        return sortDistances(nameDistances);
    }

    private Map<String, Double> sortDistances(Map<String, Double> nameDistances){
        Map<String, Double> sortedDistances = ValueComparator.sortByValues(nameDistances, false);
        return sortedDistances;
    }

    private String getClosestDistancesOutput(Map<String, Double> nameDistances, int limit) throws IOException {
        StringBuilder sb = new StringBuilder();
        if(nameDistances.isEmpty()){
            sb.append("\tNOT FOUND");
        }
        else{

            int count = 0;
            for(String closestNom: nameDistances.keySet()) {
                if(count == limit) {
                    break;
                }
                else{
                    sb.append("\t" + getMostFrequentTextForm(closestNom) + "\t" + df.format(nameDistances.get(closestNom)));
                    count++;
                }
            }
        }
        return sb.toString();
    }

    private double countDistance(String name, String nom){
        double levenshteinSum = 0;
        double maxLenSum = 0;
        double maxLenMinusLcpSum = 0;
        double maxLenMinusLcpwiSum = 0;
        String[] nameTokens = name.split("\\s+");
        String[] nomTokens = nom.split("\\s+");
        ArrayList<HashSet<String>> nameBases = new ArrayList<>();
        bases.get(name).values().forEach(nameBases::add);
        ArrayList<HashSet<String>> nomBases = new ArrayList<>();
        bases.get(nom).values().forEach(nomBases::add);
        if(nameTokens.length != nomTokens.length){
            return 1;
        }
        else {
            for (int i = 0; i < nameTokens.length; i++) {
                if (!nameTokens[i].equals(nomTokens[i]) && toShortStem(nameTokens[i], nomTokens[i])) {
                    return 1;
                }
                else if(!(nameBases.get(i).contains("ign") || nomBases.get(i).contains("ign")) && Collections.disjoint(nameBases.get(i), nomBases.get(i))){
                    return 1;
                }
                else {
                    levenshteinSum += levenshteinDistance(nameTokens[i], nomTokens[i]);
                    int maxLen = Math.max(nameTokens[i].length(), nomTokens[i].length());
                    maxLenSum += maxLen;
                    maxLenMinusLcpSum += maxLen - longestCommonPrefix(nameTokens[i], nomTokens[i]);
                    maxLenMinusLcpwiSum += maxLen - longestCommonPrefixWithInflection(nameTokens[i], nomTokens[i]);
                }
            }
        }
        if (measure.equals("dist1")){
            return levenshteinSum / maxLenSum;
        }
        else if(measure.equals("dist2")){
            return (levenshteinSum / maxLenSum) * (maxLenMinusLcpSum / maxLenSum);
        }
        else if(measure.equals("dist3")){
            return (levenshteinSum / maxLenSum) * (maxLenMinusLcpwiSum / maxLenSum);
        }
        else{
            return 1; // unreacheble
        }
    }

    private int longestCommonPrefix(String a, String b){
        a = a.toLowerCase();
        b = b.toLowerCase();
        int lcp = 0;
        for(int i=0; i < Math.min(a.length(), b.length()); i++){
            if(a.charAt(i) != b.charAt(i)){
                break;
            }
            else{
                lcp++;
            }
        }
        return lcp;
    }

    private int longestCommonPrefixWithInflection(String a, String b){
        int lcp = longestCommonPrefix(a, b);
        LinkedHashSet<String> namesSuffixes = new LinkedHashSet<String>(){{
                                            add(a.substring(lcp).toLowerCase());
            add(b.substring(lcp).toLowerCase());}};
        for(HashSet<String> suffixPair: suffixes){
            if(suffixPair.equals(namesSuffixes)){
                return Math.max(a.length(), b.length());
            }
        }
        return lcp;
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
        Map<String,Integer> sortedResults = ValueComparator.sortByValues(nameTextForms, true);
        return (String) sortedResults.keySet().toArray()[0];
    }

    private void parseWrappedAnnotations(String wrapped_file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(wrapped_file));
        String line = reader.readLine();
        String annText = "";
        while(line != null){
            if (line.startsWith("ANNOTATION:")){
                HashMap<String, HashMap<Integer, String>> documentAnnotations;

                String[] annData = line.split("\t"); // doc_name, sent, ann_start, text
                annText = annData[4].toLowerCase();
                if(wrappedAnnotations.containsKey(annData[1])){
                    documentAnnotations = wrappedAnnotations.get(annData[1]);
                }
                else{
                    documentAnnotations = new HashMap<>();
                    wrappedAnnotations.put(annData[1], documentAnnotations);
                }

                HashMap<Integer, String> sentenceAnns;
                if(documentAnnotations.containsKey(annData[2])){
                    sentenceAnns = documentAnnotations.get(annData[2]);
                }
                else{
                    sentenceAnns = new HashMap<>();
                    documentAnnotations.put(annData[2], sentenceAnns);
                }
                sentenceAnns.put(Integer.parseInt(annData[3]), annData[4]);
            }
            else{
                String[] tokenBases = line.toLowerCase().split("\t");
                LinkedHashMap<String, HashSet<String>> annBases;
                if(bases.containsKey((annText))){
                    annBases = bases.get(annText);
                }
                else{
                    annBases = new LinkedHashMap<>();
                    bases.put(annText, annBases);
                }

                HashSet<String> tokBases;
                if(annBases.containsKey(tokenBases[0])){
                    tokBases = annBases.get(tokenBases[0]);
                }
                else{
                    tokBases = new HashSet<>();
                    annBases.put(tokenBases[0], tokBases);
                }
                for(int i=1; i< tokenBases.length; i++){
                    tokBases.add(tokenBases[i]);
                }
            }
            line = reader.readLine();
        }
        reader.close();
    }

    private void loadSuffixes(String file) throws IOException, DataFormatException {
        suffixes = new ArrayList<>();
        BufferedReader reader = Files.newBufferedReader(Paths.get(file));
        String line = reader.readLine();
        while(line != null){
            LinkedHashSet<String> pair = new LinkedHashSet<String>();
            for(String suffix: line.split("\\s+")){
                pair.add(!suffix.equals("-") ? suffix : "");
            }
            if(pair.size() != 2){
                throw new DataFormatException("Error parsing suffixes. Invalid pair: " + line);
            }
            suffixes.add(pair);
            line = reader.readLine();
        }
        reader.close();
    }

}
