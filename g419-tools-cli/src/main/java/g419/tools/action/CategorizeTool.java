package g419.tools.action;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.schema.tagset.MappingNkjpToConllPos;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.liner2.core.tools.ValueComparator;
import g419.liner2.core.tools.parser.MaltParser;
import g419.liner2.core.tools.parser.MaltSentence;
import g419.toolbox.sumo.WordnetToSumo;
import g419.tools.maltfeature.DependencyPath;
import g419.tools.maltfeature.MaltPattern;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.DataFormatException;

/**
 * Created by michal on 2/12/15.
 */
public class CategorizeTool extends Action {

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
    HashMap<String, HashMap<String, HashMap<String, Integer>>> results = new HashMap<>();
    HashMap<String, HashMap<String, Integer>> categoryMatrix = new HashMap<>();
    HashMap<String, String> lemmatized_names = new HashMap<>();
    HashSet<String> nominativeNames = new HashSet<>();
    WordnetToSumo serdel = null;

    public CategorizeTool() {
        super("categorize");
        this.setDescription("Generuje macierz wystąpień cech dla określonych klas anotacji.");

        this.options.addOption(CommonOptions.getInputFileFormatOption());
        this.options.addOption(CommonOptions.getInputFileNameOption());
        this.options.addOption(CommonOptions.getOutputFileNameOption());
        this.options.addOption(CommonOptions.getFeaturesOption());
        this.options.addOption(CommonOptions.getModelFileOption());

        this.options.addOption(
        		Option.builder(OPTION_MALT).longOpt(OPTION_MALT_LONG).hasArg().argName("malt")
        		.desc("path to maltparser model").required().build());

        this.options.addOption(
        		Option.builder(OPTION_PATTERNS).longOpt(OPTION_PATTERNS_LONG).hasArg().argName("patterns")
        		.desc("path to file with patterns").required().build());

        this.options.addOption(
        		Option.builder(OPTION_SENTENCES).longOpt(OPTION_SENTENCES_LONG)
        		.desc("create additional output with sentences for all names").build());

        this.options.addOption(
        		Option.builder(OPTION_CATEGORY_MATRIX).hasArg().argName("matrix")
        		.desc("output for matrix with frequency by categories").build());


        this.options.addOption(
        		Option.builder(OPTION_CATEGORIZE).longOpt(OPTION_CATEGORIZE_LONG).hasArg().argName("categorize")
        		.desc("annotate names with new categories").build());

        this.options.addOption(
        		Option.builder(OPTION_LEMMATIZATION).longOpt(OPTION_LEMMATIZATION_LONG).hasArg().argName("lemmatization")
        		.desc("file with lematization of names").build());
        
        try {
			this.serdel = new WordnetToSumo();
		} catch (IOException | DataFormatException e) {
			Logger.getLogger(this.getClass()).error("Błąd wczytania WordnetToSumo: " + e.getMessage());
		}
    }

    @Override
    public void parseOptions(final CommandLine line) throws Exception {
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
        HashSet<String> allCategories = new HashSet<String>();
        Document ps = null;

        BufferedWriter sentenceWriter = null;
        if(getSentences && patterns_output != null){
            sentenceWriter= new BufferedWriter(new FileWriter(this.patterns_output.replace(".csv", "_sentences.csv")));
        }
        
        while ( (ps = reader.nextDocument()) != null ){
            for(Sentence sent: ps.getSentences()){
                MaltSentence maltSent = new MaltSentence(sent, MappingNkjpToConllPos.get());

                for (Annotation ann : maltSent.getAnnotations()) {
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
                    
                    boolean foundPatternForName = false;
                    name = name.toLowerCase();
                    if(lemma_count.containsKey(name)){
                        lemma_count.put(name, lemma_count.get(name) + 1);
                    }
                    else{
                        lemma_count.put(name, 1);
                    }

                    for (MaltPattern pattern : patterns) {
                    	List<DependencyPath> paths = pattern.match(maltSent, ann.getBegin() );
                    	for ( DependencyPath path : paths ){
                    		System.out.println(path.toString());
                    	}
                    	                    	
//                    	pattern.match(sentence, tokenIdx)
//                        boolean match = pattern.check(maltSent, nameIdx, pattern.getFirstNode());
//                        pattern.getResults().remove("name");
//                        if (match) {
//                            Map<String, Set<String>> conceptValues = new TreeMap<String, Set<String>>();
//                            String result = "";
//                            int conceptFound = 0;
//                            for (String label : pattern.getResults().keySet()) {
//                            	String value = maltSent.getSentence().getTokens().get(pattern.getResults().get(label)).getDisambTag().getBase();
//                                result += label + ":" + value + " ";
//                                
//                                Set<String> concepts = this.serdel.getConcept(value);
//                                if ( concepts != null && concepts.size() > 0 ){
//                                	conceptValues.put(label, concepts);
//                                	conceptFound++;
//                                }
//                                else{
//                                	conceptValues.put(label, new HashSet<String>(){{add(value);}});
//                                }
//                            }
//
//                            addResult(name, pattern.getPatternString(), result, ann.getType());
//                            allCategories.add(ann.getType());
//
//                            if(sentenceWriter != null){
//                                foundPatternForName = true;
//                                sentenceWriter.write(name + "\t" + result + "\t" + pattern.getPatternString() + "\t" + sent.toString() + "\n");
//                            }
//                            
//                            /**
//                             * Wygeneruj dopasowania z uwzględnieniem mapowania na sumo
//                             */
//                            if ( conceptFound > 0 ){
//                                for ( String conceptResult : this.generateCombinations(conceptValues) ){
//                                    addResult(name, pattern.getPatternString(), conceptResult, ann.getType());
//                                    allCategories.add(ann.getType());	                                	
//                                }
//                            }
//                            
//                        }
//                        pattern.clear();
                    }
                    if (sentenceWriter != null && !foundPatternForName) {
                        sentenceWriter.write(name + "\tNOT FOUND\tNOT FOUND\t" + sent.toString() + "\n");
                    }
                }
            }
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
        /**
         * Zapisz macierz wystąpień wzorców dla poszczególnych klas.
         */
        else if(category_matrix_file != null){
            BufferedWriter matrixWriter = new BufferedWriter(new FileWriter(this.category_matrix_file));
            // Wypisz nagłówek
            for(String category: allCategories){
                matrixWriter.write("\t" + category);
            }
            matrixWriter.write("\tSum\tScore\n");
            
            // Wypisz wiersze
            for(String pattval: categoryMatrix.keySet()){
                HashMap<String, Integer> pattvalResults = categoryMatrix.get(pattval);
                int patternInstancesCount = 0;
                int maxCount = 0;
                for(String category: allCategories){
                	int categoryCount = pattvalResults.containsKey(category) ? pattvalResults.get(category) : 0;
                	patternInstancesCount += categoryCount;
                	maxCount = Math.max(maxCount, categoryCount);
                }
            	if ( patternInstancesCount > 5 ){
            		double score = (float)maxCount / (float)patternInstancesCount * Math.log(patternInstancesCount);
	                matrixWriter.write(pattval);
	                for(String category: allCategories){
	                    matrixWriter.write("\t" + (pattvalResults.containsKey(category) ? pattvalResults.get(category) : 0));
	                }
	                matrixWriter.write("\t" + patternInstancesCount);
	                matrixWriter.write(String.format("\t%5.2f", score));
	                
	                matrixWriter.write("\n");
            	}
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
        Files.lines(Paths.get(file)).filter((line) -> !line.isEmpty()).forEach((patt) -> this.patterns.add(new MaltPattern(patt)));
    }

    /**
     * Generuje wszystkie możliwe kombinacje wartości dla listy atrybutów, np.
     * <code>
     *   values = { "X":("biały", "czarny"), "Y":("kot", "pies") }
     * </code>
     * Wygeneruje następujące napisy:
     * <code>
     *   X:biały Y:kot
     *   X:biały Y:pies
     *   X:czarny Y:kot
     *   X:czarny Y:pies
     * </code>     
     * @param names
     * @param values
     * @return
     */
    private List<String> generateCombinations(Map<String, Set<String>> values){
    	@SuppressWarnings("serial")
		List<String> lastList = new ArrayList<String>(){{add("");}};
    	for ( String name : values.keySet() ){
    		Set<String> nameValues = values.get(name);
    		List<String> newList = new ArrayList<String>();
    		for ( String str : lastList ){
    			for ( String value : nameValues ){
    				String strNew = String.format("%s %s:%s", str, name, value).trim();
    				newList.add(strNew);
    			}
    		}
    		lastList = newList;
    	}
    	return lastList;
    }

}
