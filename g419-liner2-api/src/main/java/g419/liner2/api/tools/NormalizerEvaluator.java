package g419.liner2.api.tools;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.liner2.api.normalizer.Normalizer;

import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NormalizerEvaluator {

    protected Map<Result, Map<String, Integer>> results;
    protected Map<String, Integer> totalPerType;
    protected Document currentDocument; //todo: its a dirty hack, making this thread-unsafe
    protected PrintWriter missPrinter;
    protected Set<String> metaKeys;

    public NormalizerEvaluator(PrintWriter missPrinter, Set<String> metaKeys) {
        this.missPrinter = missPrinter;
        this.metaKeys = metaKeys;
    }

    public void reset(){
        results = new HashMap<>();
        results.put(Result.TP, new HashMap<String, Integer>());
        results.put(Result.FP, new HashMap<String, Integer>());
        results.put(Result.FN, new HashMap<String, Integer>());
        totalPerType = new HashMap<>();
    }

    protected void println(Object... args){
        for (Object a: args)
            System.out.print(a.toString()+" ");
        System.out.println();
    }

    protected void saveResult(Result result, String type){
        Map<String, Integer> bucket = results.get(result);
        if (!bucket.containsKey(type))
            bucket.put(type, 0);
        bucket.put(type, bucket.get(type)+1);
        if (!totalPerType.containsKey(type))
            totalPerType.put(type, 0);
        totalPerType.put(type, totalPerType.get(type)+1);
    }

    protected void printResult(Result result, Annotation annotation, Map<String, String> expected){
        println( "\t", result.description, "for annotation type:", annotation.getType());
        println("\t\t", "to normalize:", annotation.getBaseText());
        println("\t\t", "expected:", expected);
        println("\t\t", "result:  ", annotation.getMetadata());
    }

    protected void printMiss(Result result, Annotation annotation, Map<String, String> expected){
        if (result!=Result.TP && missPrinter!=null){
            missPrinter.println(result.description);
            missPrinter.println("At document: "+currentDocument.getName());
            missPrinter.println("In sentence: " + annotation.getSentence().toString());
            missPrinter.println("Full text: "+annotation.getText());
            missPrinter.println("Base text: "+annotation.getBaseText());
            missPrinter.println("\tAnnotation type: "+annotation.getType());
            missPrinter.println("\tExpected: "+expected);
            missPrinter.println("\tResult:   "+annotation.getMetadata());
            missPrinter.println();
        }
    }


    public void evaluateDocument(Document document, Normalizer normalizer, ProcessingTimer timer){
        println("Evaluating on document", document.getName());
        normalizer.onNewDocument(document);
        if (currentDocument!=null)
            throw new IllegalStateException("Current document should be null when starting " +
                    "evaluation of new one. Did you use multithreading? " +
                    "(old document: "+currentDocument+", new document: "+document+")");
        currentDocument = document;
        for (Sentence s: document.getSentences())
            evaluateSentence(s, normalizer, timer);
        normalizer.onDocumentEnd(document);
        currentDocument = null;
        println();
        println();
        println("-----------------------------------------------------------");
    }



    protected void evaluateSentence(Sentence sentence, Normalizer normalizer, ProcessingTimer timer){
        println();
        println("Evaluating on sentence", "\""+sentence+"\"");
        normalizer.onNewSentence(sentence);
        List<Pattern> types = normalizer.getNormalizedChunkTypes();
//        annotationLoop:
        for (Annotation annotation: sentence.getAnnotations(types)) {
            normalizer.onNewAnnotation(annotation);
            String t = annotation.getType();
            typeLoop:
            for (Pattern type: types) {
                Matcher matcher = type.matcher(t);
                matcher.find();
                if (matcher.matches()) {
                    Map<String, String> original = annotation.getMetadata();
                    Map<String, String> expected = submap(original, metaKeys);
                    annotation.setMetadata(new HashMap<String, String>(original));
                    for (String k: metaKeys)
                        annotation.getMetadata().remove(k);
                    timer.stopTimer();
                    timer.startTimer("Normalizing");
                    try {
                        normalizer.normalize(annotation);
                    } catch (Exception ex){
                        System.err.println("Failed to normalize a sentence \"" + sentence+"\"");
                        ex.printStackTrace(System.err);
                    } finally {
                        timer.stopTimer();
                        timer.startTimer("Evaluation", false);
                    }
                    Map<String, String> results = submap(annotation.getMetadata(), metaKeys);
                    annotation.setMetadata(results);

                    Result result = Result.FN;
                    if (!results.isEmpty())
                        if (results.equals(expected))
                            result =  Result.TP;
                        else
                            result = Result.FP;

                    saveResult(result, annotation.getType());
                    printResult(result, annotation, expected);
                    printMiss(result, annotation, expected);
                    annotation.setMetadata(original);
//                    continue annotationLoop;
                    break typeLoop;
                }
            }
            normalizer.onAnnotationEnd(annotation);
        }
        normalizer.onSentenceEnd(sentence);
    }

    protected static <K, V> Map<K, V> submap(Map<K, V> map, Set<K> keys){
        Map<K, V> out = new HashMap<>(map);
        Set<K> toRemove = new HashSet<>(out.keySet());
        toRemove.removeAll(keys);
        for (K k: toRemove){
            out.remove(k);
        }
        return out;
    }

    static final String ROW_TEMPLATE = "%15s & %10d & %10f & %10d & %10f & %10d & %10f & %10d & %10f & %10f & %10f \\\\";
    static final String HEADER_TEMPLATE = ROW_TEMPLATE.replaceAll("[df]", "s");
    static final String[] HEADER = new String[] {
        "Annotation type", "TP", "TP rate", "FP", "FP rate", "FN", "FN rate", "Total", "Precision", "Recall", "F"
    };

    protected int withDefault(Result result, String type, int def){
        if (results.containsKey(result) && results.get(result).containsKey(type))
            return results.get(result).get(type);
        return def;
    }

    protected void printRow(String type, Map<String, Integer> totals){
        int total = totalPerType.get(type);
        int tp = withDefault(Result.TP, type, 0);
        int fp = withDefault(Result.FP, type, 0);
        int fn = withDefault(Result.FN, type, 0);
        totals.put("tp", totals.get("tp")+tp);
        totals.put("fp", totals.get("fp")+fp);
        totals.put("fn", totals.get("fn")+fn);
        totals.put("total", totals.get("total")+total);
        double tpRate = 1.0*tp/total;
        double fpRate = 1.0*fp/total;
        double fnRate = 1.0*fn/total;
        double precision = (tp+fp!=0) ? 1.0*tp/(tp+fp) : 0.0;
        double recall = (tp+fn!=0) ? 1.0*tp/(tp+fn) : 0.0;
        double f = (Math.abs(precision+recall)>0.0001 ? (2*precision*recall)/(precision+recall) : 0.0);
        println(String.format(
                    ROW_TEMPLATE,
                    type,
                    tp, tpRate,
                    fp, fpRate,
                    fn, fnRate,
                    total,
                    precision, recall,
                    f
                )
        );
    }

    protected void printTotal(Map<String, Integer> totals){
        int total = totals.get("total");
        int tp = totals.get("tp");
        int fp = totals.get("fp");
        int fn = totals.get("fn");
        double tpRate = 1.0*tp/total;
        double fpRate = 1.0*fp/total;
        double fnRate = 1.0*fn/total;
        double precision = (tp+fp!=0) ? 1.0*tp/(tp+fp) : 0.0;
        double recall = (tp+fn!=0) ? 1.0*tp/(tp+fn) : 0.0;
        double f = (Math.abs(precision+recall)>0.0001 ? (2*precision*recall)/(precision+recall) : 0.0);
        println(String.format(
                        ROW_TEMPLATE,
                        "TOTAL",
                        tp, tpRate,
                        fp, fpRate,
                        fn, fnRate,
                        total,
                        precision, recall,
                        f
                )
        );
    }

    public void printResults(){
        println(String.format(HEADER_TEMPLATE, HEADER));
        println("\\hline");
        Map<String, Integer> totals = new HashMap<>();
        totals.put("tp", 0);
        totals.put("fp", 0);
        totals.put("fn", 0);
        totals.put("total", 0);
        for (String type: getKeysAlphabetically()){
            printRow(type, totals);
        }
        println("\\hline");
        printTotal(totals);
        println();
    }

    protected List<String> getKeysAlphabetically(){
        List<String> out = new ArrayList<>(totalPerType.keySet());
        Collections.sort(out);
        return out;
    }
}
