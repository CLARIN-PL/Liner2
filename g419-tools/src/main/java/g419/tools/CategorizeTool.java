package g419.tools;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.lib.cli.CommonOptions;
import g419.liner2.api.tools.ValueComparator;
import g419.liner2.api.tools.parser.MaltParser;
import g419.liner2.api.tools.parser.MaltSentence;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.OptionBuilder;
import org.maltparser.MaltParserService;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;

/**
 * Created by michal on 2/12/15.
 */
public class CategorizeTool extends Tool{

    public static final String OPTION_MALT = "m";
    public static final String OPTION_MALT_LONG = "malt";

    public static final String OPTION_PATTERNS = "p";
    public static final String OPTION_PATTERNS_LONG = "patterns";

    public static final String OPTION_SENTENCES = "s";
    public static final String OPTION_SENTENCES_LONG = "sentences";

    public static final String OPTION_CATEGORY_MATRIX = "matrix";

    public static final String OPTION_CATEGORIZE = "c";
    public static final String OPTION_CATEGORIZE_LONG = "categorize";

    public static final String OPTION_LEMMATIZATION = "l";
    public static final String OPTION_LEMMATIZATION_LONG = "lemmatization";

    private String input_file = null;
    private String input_format = null;
    private String patterns_output = null;
    private String categorize_output = null;
    private File category_matrix_file = null;
    private boolean getSentences = false;
    private MaltParser malt;
    private HashMap<String, Integer> lemma_count = new HashMap<>();
    private ArrayList<MaltPattern> patterns = new ArrayList<>();
    private static Pattern relFrom = Pattern.compile("^--\\(([a-z_]+)\\)-->$");
    private static Pattern relTo = Pattern.compile("^<--\\(([a-z_]+)\\)--$");
    HashMap<String, HashMap<String, HashMap<String, Integer>>> results = new HashMap<>();
    HashMap<String, HashMap<String, Integer>> categoryMatrix = new HashMap<>();
    HashMap<String, String> lemmatized_names = new HashMap<>();
    HashSet<String> nominativeNames = new HashSet<>();

    public CategorizeTool() {
        super("categorize");
        this.setDescription("ToDo");

        this.options.addOption(CommonOptions.getInputFileFormatOption());
        this.options.addOption(CommonOptions.getInputFileNameOption());
        this.options.addOption(CommonOptions.getOutputFileNameOption());
        this.options.addOption(CommonOptions.getFeaturesOption());
        this.options.addOption(CommonOptions.getModelFileOption());

        OptionBuilder.withArgName("malt");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("path to maltparser model");
        OptionBuilder.withLongOpt(OPTION_MALT_LONG);
        OptionBuilder.isRequired();
        this.options.addOption(OptionBuilder.create(OPTION_MALT));

        OptionBuilder.withArgName("patterns");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("path to file with patterns");
        OptionBuilder.withLongOpt(OPTION_PATTERNS_LONG);
        this.options.addOption(OptionBuilder.create(OPTION_PATTERNS));

        OptionBuilder.withDescription("create additional output with sentences for all names");
        OptionBuilder.withLongOpt(OPTION_SENTENCES_LONG);
        this.options.addOption(OptionBuilder.create(OPTION_SENTENCES));

        OptionBuilder.withDescription("output for matrix with frequency by categories");
        OptionBuilder.withArgName("matrix");
        OptionBuilder.hasArg();
        this.options.addOption(OptionBuilder.create(OPTION_CATEGORY_MATRIX));

        OptionBuilder.withDescription("annotate names with new categories");
        OptionBuilder.withArgName("categorize");
        OptionBuilder.withLongOpt(OPTION_CATEGORIZE_LONG);
        OptionBuilder.hasArg();
        this.options.addOption(OptionBuilder.create(OPTION_CATEGORIZE));

        OptionBuilder.withDescription("file with lematization of names");
        OptionBuilder.withArgName("lemmatization");
        OptionBuilder.withLongOpt(OPTION_LEMMATIZATION_LONG);
        OptionBuilder.hasArg();
        this.options.addOption(OptionBuilder.create(OPTION_LEMMATIZATION));
    }

    @Override
    public void parseOptions(String[] args) throws Exception {
        CommandLine line = new GnuParser().parse(this.options, args);
        parseDefault(line);
        this.patterns_output = line.getOptionValue(CommonOptions.OPTION_OUTPUT_FILE);
        this.input_file = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
        this.input_format = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT, "ccl");
        if(line.hasOption(OPTION_CATEGORY_MATRIX)){
            this.category_matrix_file = new File(line.getOptionValue(OPTION_CATEGORY_MATRIX));
        }
        if(line.hasOption(OPTION_CATEGORIZE)){
            this.categorize_output = line.getOptionValue(OPTION_CATEGORIZE);
            if(this.category_matrix_file == null || !this.category_matrix_file.exists()){
                throw new DataFormatException("category matrix data set is required for categorization");
            }
            if(line.hasOption(OPTION_LEMMATIZATION)){
                loadLemmatizationResults(line.getOptionValue(OPTION_LEMMATIZATION));
            }
        }
        String modelPath = line.getOptionValue(OPTION_MALT);
        malt = new MaltParser(modelPath);
        getPatterns(line.getOptionValue(OPTION_PATTERNS));

        if(line.hasOption(OPTION_SENTENCES)){
            getSentences = true;
        }
    }

    private void loadLemmatizationResults(String file_path) throws DataFormatException, IOException {
        String noms_file = file_path.replace(".csv", "_nom.csv");
        String oth_file = file_path.replace(".csv", "_oth.csv");
        if(!(new File(noms_file).exists())){
            throw new DataFormatException("lemmatization file does not exist: " + noms_file);
        }
        if(!(new File(oth_file).exists())){
            throw new DataFormatException("lemmatization file does not exist: " + oth_file);
        }
        BufferedReader reader = new BufferedReader(new FileReader(noms_file));
        String line = reader.readLine();
        while(line != null){
            String[] nameData = line.split("\t");
            nominativeNames.add(nameData[0]);
            line = reader.readLine();
        }
        reader.close();

        reader = new BufferedReader(new FileReader(oth_file));
        line = reader.readLine();
        while(line != null){
            String[] nameData = line.split("\t");
            if(nameData[1].equals("BASE:YES")){
                lemmatized_names.put(nameData[0], nameData[2]);
            }
            line = reader.readLine();
        }
        reader.close();


    }

    @Override
    public void run() throws Exception {
        AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(this.input_file, this.input_format);
        Document ps = reader.nextDocument();
        HashSet<String> allCategories = new HashSet<>();

        BufferedWriter sentenceWriter = null;
        if(getSentences && patterns_output != null){
            sentenceWriter= new BufferedWriter(new FileWriter(this.patterns_output.replace(".csv", "_sentences.csv")));
        }
        while ( ps != null ){
            for(Sentence sent: ps.getSentences()){
                MaltSentence maltSent = new MaltSentence(sent, sent.getChunks());
                String [] parsedTokens =  malt.parseTokens(maltSent.getMaltData());
                String [][] splittedData = new String[parsedTokens.length][10];
                for(int i=0; i<parsedTokens.length; i++){
                    splittedData[i] = parsedTokens[i].split("\t");
                }

                wrapConjunctions(splittedData);

                HashSet<Annotation> anns = maltSent.getAnnotations();
                if (!anns.isEmpty()) {

                    for (Annotation ann : anns) {
                        String name = ann.getText();
                        if(categorize_output != null){
                            if(lemmatized_names.containsKey(name)){
                                name = lemmatized_names.get(name);
                            }
                            if(!nominativeNames.contains(name)) {
                                continue;
                            }
                        }
                        else{
                            name = ann.getBaseText();
                        }
                        int nameIdx = ann.getBegin();
                        boolean foundPatternForName = false;
                        name = name.toLowerCase();
                        if(lemma_count.containsKey(name)){
                            lemma_count.put(name, lemma_count.get(name) + 1);
                        }
                        else{
                            lemma_count.put(name, 1);
                        }

                        for (MaltPattern pattern : patterns) {
                            boolean match = pattern.check(splittedData, nameIdx, pattern.name);
                            pattern.results.remove("name");
                            if (match) {
                                String result = "";
                                for (String label : pattern.results.keySet()) {
                                    result += label + ":" + splittedData[pattern.results.get(label)][2] + " ";
                                }

                                addResult(name, pattern.pattString, result, ann.getType());
                                allCategories.add(ann.getType());

                                if(sentenceWriter != null){
                                    foundPatternForName = true;
                                    sentenceWriter.write(name + "\t" + result + "\t" + pattern.pattString + "\t" + sent.toString() + "\n");
                                }
                            }
                            pattern.clear();
                        }
                        if (sentenceWriter != null && !foundPatternForName) {
                            sentenceWriter.write(name + "\tNOT FOUND\tNOT FOUND\t" + sent.toString() + "\n");
                        }
                    }
                }
            }
            ps = reader.nextDocument();
        }
        if(getSentences){
            sentenceWriter.close();
        }
        reader.close();
        if(patterns_output != null){
            BufferedWriter dictWriter = new BufferedWriter(new FileWriter(this.patterns_output));
            for(String name: results.keySet()){
                HashMap<String, HashMap<String, Integer>> nameResults = results.get(name);
                for(String patt: nameResults.keySet()) {
                    dictWriter.write(name + "\t" + patt);
                    Map<String, Integer> pattResults = nameResults.get(patt);
                    Map<String, Integer> sortedResults = ValueComparator.sortByValues(pattResults, true);
                    for (String x : sortedResults.keySet()) {
                        dictWriter.write("\t" + x + "\t" + pattResults.get(x));
                    }
                    dictWriter.write("\n");
                }
            }
            dictWriter.close();
        }

        if(categorize_output != null) {
            BufferedReader matrixReader = new BufferedReader(new FileReader(this.category_matrix_file));
            String[] categories = matrixReader.readLine().trim().split("\t");
            TreeMap<String, ArrayList<Integer>> data = new TreeMap<>();
            String line = matrixReader.readLine();
            while(line != null){
                String[] rowData = line.split("\t");
                data.put(rowData[0], new ArrayList<>());
                for(int i=1 ; i< rowData.length; i++){
                    data.get(rowData[0]).add(Integer.parseInt(rowData[i]));
                }
                line = matrixReader.readLine();
            }
            ArrayList<String> rownames = new ArrayList<>(data.keySet());
            double[][] probabilityMatrix = new double[data.size()][categories.length];
            int row_nr = 0;
            for(ArrayList<Integer> patternVal: data.values()){
                double sum = patternVal.stream().mapToInt(Integer::intValue).sum();
                for(int i=0; i<categories.length; i++){
                    probabilityMatrix[row_nr][i] = sum != 0 ? patternVal.get(i) / sum : 0;
                }
                row_nr++;
            }
            TreeMap<String,String> results_by_nom = new TreeMap<>();
            TreeMap<String,String> results_by_orth = new TreeMap<>();

            BufferedWriter categoryWriter = new BufferedWriter(new FileWriter(this.categorize_output));
            reader = ReaderFactory.get().getStreamReader(this.input_file, this.input_format);
            ps = reader.nextDocument();
            HashSet<String> checkedNames = new HashSet<>();
            while(ps != null){
                for(Annotation ann: ps.getAnnotations()){
                    String text = ann.getText();
                    if(lemmatized_names.containsKey(text)){
                        text = lemmatized_names.get(text);
                    }
                    if(nominativeNames.contains(text)){
                        String lower_text = text.toLowerCase();
                        if(!checkedNames.contains(lower_text)){

                            categoryWriter.write(text + "\t" + lemma_count.get(lower_text));
                            checkedNames.add(lower_text);
                            HashMap<String, HashMap<String, Integer>> nameResults;
                            if(results.containsKey(lower_text)){
                                nameResults = results.get(lower_text);
                                double[][] nameMatrix = new double[probabilityMatrix.length][probabilityMatrix[0].length];
                                for(String pattern: nameResults.keySet()){
                                    HashMap<String, Integer> pattResults = nameResults.get(pattern);
                                    for(String values: pattResults.keySet()){
                                        if(rownames.contains(pattern + " " + values)){
                                            int row_idx = rownames.indexOf(pattern + " " + values);
                                            int count = pattResults.get(values);
                                            for(int i=0; i< categories.length; i++){
                                                nameMatrix[row_idx][i] = count * probabilityMatrix[row_idx][i];
                                            }
                                        }
                                    }
                                }
                                HashMap<String, Double> categoryResults = new HashMap<>();
                                for(int col_idx = 0; col_idx < categories.length; col_idx++){
                                    double sum = 0;
                                    for(int row_idx=0; row_idx < nameMatrix.length; row_idx++){
                                        sum+=nameMatrix[row_idx][col_idx];
                                    }
                                    categoryResults.put(categories[col_idx], sum);
                                }
                                if(Collections.max(categoryResults.values()) == 0){
                                    categoryWriter.write("\tZERO VALUES");
                                }
                                else{
                                    categoryResults = (HashMap<String, Double>) ValueComparator.sortByValues(categoryResults, true);
                                    results_by_nom.put(text, categoryResults.keySet().iterator().next());
                                    results_by_orth.put(ann.getText(), text);
                                    int count = 0;
                                    for(String category: categoryResults.keySet()){
                                        if(count < 3){
                                            categoryWriter.write("\t" + category + "\t" + categoryResults.get(category));
                                            count++;
                                        }
                                        else{
                                            break;
                                        }
                                    }
                                }



                            } else{
                                categoryWriter.write("\tNO PATTERNS");
                            }
                            categoryWriter.write("\n");
                        }
                        else if(results_by_nom.containsKey(text)){
                            results_by_orth.put(ann.getText(), text);
                        }
                    }


                }
                ps = reader.nextDocument();
            }
            categoryWriter.close();

            categoryWriter = new BufferedWriter(new FileWriter(this.categorize_output.replace(".csv", "_sorted_by_nom.csv")));
            for(String nom: results_by_nom.keySet()){
                categoryWriter.write(nom + "\t" + results_by_nom.get(nom) + "\n");
            }
            categoryWriter.close();

            categoryWriter = new BufferedWriter(new FileWriter(this.categorize_output.replace(".csv", "_sorted_by_orth.csv")));
            for(String orth: results_by_orth.keySet()){
                String nom = results_by_orth.get(orth);
                categoryWriter.write(orth + "\t" + nom + "\t" + results_by_nom.get(nom) + "\n");
            }
            categoryWriter.close();

        }
        else if(category_matrix_file != null){
            BufferedWriter matrixWriter = new BufferedWriter(new FileWriter(this.category_matrix_file));
            for(String category: allCategories){
                matrixWriter.write("\t" + category);
            }
            matrixWriter.write("\n");
            for(String pattval: categoryMatrix.keySet()){
                matrixWriter.write(pattval);
                HashMap<String, Integer> pattvalResults = categoryMatrix.get(pattval);
                for(String category: allCategories){
                    matrixWriter.write("\t" + (pattvalResults.containsKey(category) ? pattvalResults.get(category) : 0));
                }
                matrixWriter.write("\n");
            }
            matrixWriter.close();
        }
    }


    private void addResult(String name, String pattern, String result, String category){
        HashMap<String, HashMap<String, Integer>> nameResults;
        if(results.containsKey(name)){
            nameResults = results.get(name);
        }
        else{
            nameResults = new HashMap<>();
            results.put(name, nameResults);
        }

        HashMap<String, Integer> patternResults;
        if(nameResults.containsKey(pattern)){
            patternResults = nameResults.get(pattern);
        }
        else{
            patternResults = new HashMap<>();
            nameResults.put(pattern, patternResults);
        }

        if(patternResults.containsKey(result)){
            patternResults.put(result, patternResults.get(result) + 1);
        }
        else{
            patternResults.put(result, 1);
        }

        if(category_matrix_file != null){
            HashMap<String, Integer> patternvalResults;
            if(categoryMatrix.containsKey(pattern + " " + result)){
                patternvalResults = categoryMatrix.get(pattern + " " + result);
            }
            else{
                patternvalResults = new HashMap<>();
                categoryMatrix.put(pattern + " " + result, patternvalResults);
            }
            if(patternvalResults.containsKey(category)){
                patternvalResults.put(category, patternvalResults.get(category) + 1);
            }
            else{
                patternvalResults.put(category, 1);
            }

        }
    }

    private void getPatterns(String file) throws IOException {
        Files.lines(Paths.get(file)).filter((line) -> !line.isEmpty()).forEach((patt) -> patterns.add(new MaltPattern(patt)));
    }

    private void wrapConjunctions(String[][] sentenceData){
        for(int i=0; i<sentenceData.length; i++){
            if(sentenceData[i][3].equals("conj") && Integer.parseInt(sentenceData[i][8]) != 0){
                for(int j=0; j<sentenceData.length; j++){
                    if(sentenceData[j][9].equals("conjunct") && (Integer.parseInt(sentenceData[j][8]) - 1) == i){
                        sentenceData[j][9] = sentenceData[i][9];
                        sentenceData[j][8] = sentenceData[i][8];
                    }
                }
                sentenceData[i][9] = "deleted_rel";
            }
        }
    }

    private class MaltPattern {
        String pattString;
        Node name;
        ArrayList<Node> nodes = new ArrayList<>();
        ArrayList<Edge> edges = new ArrayList<>();
        HashMap<String, Integer> results = new HashMap<>();

        public MaltPattern(String pattern){
            parsePattern(pattern);
        }

        private void parsePattern(String pattern) {
            pattString = pattern;
            String[] elements = pattern.split("\\s+");
            int idx = 0;
            Node node = createNode(elements[0]);
            if(node.label != null && node.label.equals("name")){
                name = node;
            }
            nodes.add(node);
            while(idx != elements.length - 1){
                node = addRel(node, elements[idx+1], elements[idx + 2]);
                nodes.add(node);
                if(node.label != null && node.label.equals("name")){
                    name = node;
                }
                idx += 2;
            }
//            print();
        }

        private void print(){
            System.out.println("NAME NODE:" + name.printNode());
            for(Node n: nodes){
                System.out.println(n.printNode());
                for(Edge e: n.edges){
                    System.out.println(e.printEdge());
                }
                System.out.println("--------");
            }
            System.out.println("######");

        }

        private Node addRel(Node node, String rel, String element){
            Node newNode = createNode(element);
            Matcher matchFrom = relFrom.matcher(rel);
            Matcher matchTo = relTo.matcher(rel);
            Edge edge;
            if(matchFrom.find()){
                edge = new Edge(matchFrom.group(1), node, newNode);
            }
            else if(matchTo.find()){
                edge = new Edge(matchTo.group(1), newNode, node);
            }
            else{
//                System.out.println("NO MATCH");
                newNode = null;
                edge = null;
            }
            edges.add(edge);
            node.addEdge(edge);
            newNode.addEdge(edge);
            return newNode;
        }

        private Node createNode(String data){
            String[] nodeData = data.split(":");
            Node node;
            if(nodeData.length == 1){
                node = new Node(nodeData[0], null, null);
            }
            else if(nodeData.length == 2){
                node = new Node(null, nodeData[0], nodeData[1]);
            }
            else if(nodeData.length == 3){
                node = new Node(nodeData[0], nodeData[1], nodeData[2]);
            }
            else{
                node = null;
            }

            return node;
        }

        private boolean check(String[][] sentenceData, int tokenIdx, Node node){
            boolean nodeResult = node.check(sentenceData[tokenIdx], tokenIdx);
            if(!nodeResult){
                return false;
            }
            for(Edge edge: node.edges){
                    int edgeResult;
                    if(!edge.checkedFrom && node == edge.from){
                        edgeResult = edge.checkFrom(sentenceData, tokenIdx);
                        if(edgeResult == -1 || !check(sentenceData, edgeResult, edge.to)){
                            return false;
                        }
                    }
                    else if(!edge.checkedTo && node == edge.to){
                        edgeResult = edge.checkTo(sentenceData, tokenIdx);
                        if(edgeResult == -1 || !check(sentenceData, edgeResult, edge.from)){
                            return false;
                        }
                    }
            }
            return true;
        }

        public void clear() {
            for(Edge edge: edges){
                edge.checkedFrom = false;
                edge.checkedTo = false;
            }
            results = new HashMap<>();
        }

        private class Node{
            String label = null;
            String checkBy = null;
            String form = null;
            HashSet<Edge> edges = new HashSet<>();

            private Node(String label, String checkBy, String form){
                this.label = label;
                this.checkBy = checkBy;
                this.form = form;
            }

            private void addEdge(Edge edge){
                edges.add(edge);
            }

            private String printNode(){
                String pattern = "";
                pattern += "label: " + (label != null ? label : "-");
                pattern += " checkBy: " + (checkBy != null ? checkBy : "-");
                pattern += " form: " + (form != null ? form : "-");
                return pattern;
            }

            private boolean check(String[] tokenData, int tokenIdx){
                if(label != null){
                    results.put(label, tokenIdx);
                }
                if(form == null){
                    return true;
                }
                boolean match = true;
                if(checkBy == "base"){
                    match = form.equals(tokenData[2]);
                }
                else if(checkBy == "pos"){
                    match = form.equals(tokenData[3]);
                }
                return match;
            }
        }

        private class Edge{
            boolean checkedFrom = false;
            boolean checkedTo = false;
            String relation;
            Node from, to;

            private Edge(String relation, Node from, Node to){
                this.relation = relation;
                this.from = from;
                this.to = to;
            }

            private String printEdge(){
                return from.printNode() + " --(" + relation + ")--> " + to.printNode();
            }

            private int checkFrom(String[][] sentenceData, int fromIdx){
                checkedFrom =true;
                for(int toIdx=0; toIdx<sentenceData.length; toIdx++){
                    if(sentenceData[toIdx][9].equals(relation)){
                        if(Integer.parseInt(sentenceData[toIdx][8]) - 1 == fromIdx){
                            if(to.check(sentenceData[toIdx], toIdx)) {
                                return toIdx;
                            }
                        }
                    }
                }
                return -1;
            }

            private int checkTo(String[][] sentenceData, int toIdx){
                checkedTo =true;
                if(sentenceData[toIdx][9].equals(relation)){
                    int fromIdx = Integer.parseInt(sentenceData[toIdx][8]) - 1;
                    if(fromIdx == -1){
                        return -1;
                    }
                    if(from.check(sentenceData[fromIdx], fromIdx)){
                        return fromIdx;
                    }
                }
                return -1;

            }


        }
    }


}
