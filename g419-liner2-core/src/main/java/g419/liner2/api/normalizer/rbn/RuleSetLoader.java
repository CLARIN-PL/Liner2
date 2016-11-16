package g419.liner2.api.normalizer.rbn;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;

/**
 * Singleton used for loading RuleSets.
 * It is stateless (so singleton makes sense) and capable of loading rule set from path, File object or reader.
 *
 * Non-public methods are static for the sake of efficiency.
 */
public final class RuleSetLoader {
    static private RuleSetLoader instance;

    private RuleSetLoader() {}

    /**
     * And Lord said... let there be singleton!
     * @return singleton instance
     */
    public static RuleSetLoader getInstance(){
        if (instance==null)
            instance = new RuleSetLoader();
        return instance;
    }

    /**
     * No sanity check is performed - if file was malformed, you'll be sorry.
     * @param reader Reader of JSON file with normalization rules
     * @return RuleSet object
     * @throws java.io.FileNotFoundException when included file doesn't exist
     */
    protected RuleSet load(File baseDir, Reader reader) throws FileNotFoundException {
        JSONObject parsed = (JSONObject) JSONValue.parse(reader);
        Map<String, Rule> rules = new LinkedHashMap<String, Rule>();
        Map<String, String> normalization = new LinkedHashMap<String, String>();
        doInclude(baseDir, parsed, rules, normalization);
        String regexEscape = (String) parsed.get("regexEscape");
        Map<String, String> patterns = getPatterns(parsed, regexEscape);
        loadRules(rules, parsed, patterns, regexEscape);
        loadNormalization(normalization, parsed);
        return new RuleSet(rules, normalization);
    }

    /**
     * @param path Absolute path to loaded file. Will be used while including other files
     * @see #load(java.io.File, java.io.Reader)
     * @throws java.io.FileNotFoundException when given path doesn't exist
     */
    public RuleSet load(String path) throws FileNotFoundException {
        return load(new File(path));
    }

    /**
     * @see #load(java.io.File, java.io.Reader)
     * @throws java.io.FileNotFoundException when given File doesn't exist
     */
    public RuleSet load(File file) throws FileNotFoundException {
        return load(file.getParentFile().getAbsoluteFile(), new BufferedReader(new FileReader(file)));
    }

    private void doInclude(File baseDir, JSONObject parsed, Map<String, Rule> rules, Map<String, String> normalization) throws FileNotFoundException {
        JSONArray includes = (JSONArray) parsed.get("include");
        if (includes!=null){
            for (Object path: includes) {
                RuleSet included = load(new File(baseDir, (String) path));
                rules.putAll(included.rules);
                normalization.putAll(included.normalization);
            }
        }
    }

    static private String escaped(String raw, String regexEscape){
        if (regexEscape!=null)
            return raw.replaceAll(regexEscape, Matcher.quoteReplacement("\\"));
        return raw;
    }

    /**
     * I know it's fugly. It is meant to do simple task, not be reusable.
     */
    static private Map<String, String> getPatterns(JSONObject parsed, String regexEscape){
        Map<String, String> out = new HashMap<String, String>();
        Map patterns = (JSONObject) parsed.get("patterns");
        if (patterns!=null){
            for (Object entry: patterns.entrySet()){
                String key = (String)(((Map.Entry) entry).getKey());
                Object value = ((Map.Entry) entry).getValue();
                if (value instanceof String){           // explicit pattern
                    out.put(key, escaped((String) value, regexEscape));
                } else if (value instanceof List){      // pattern as List
                    out.put(key, escaped(join((List) value, "|"), regexEscape));
                } else if (value instanceof Map){       // group of patterns
                    for (Object entry2: ((Map)value).entrySet()) {
                        String key2 = (String) (((Map.Entry) entry2).getKey());
                        Object value2 = ((Map.Entry) entry2).getValue();
                        if (value2 instanceof String) {
                            out.put(key2, escaped((String) value2, regexEscape));
                        } else if (value2 instanceof List) {
                            out.put(key2, escaped(join((List) value2, "|"), regexEscape));
                        }
                    }
                }
            }
        }
        return out;
    }

    static private void loadRules(Map<String, Rule> rules, JSONObject parsed, Map<String, String> patterns, String regexEscape){
        ArrayList<JSONObject> rulesDef = (JSONArray) parsed.get("rules");
        if (rulesDef!=null){
            for (JSONObject obj: rulesDef){
                for (Object entry : obj.entrySet()){
                    String name = (String)(((Map.Entry) entry).getKey());
                    Map value = (JSONObject)((Map.Entry) entry).getValue();
                    String extract = escaped((String)value.get("extract"), regexEscape);
                    String normalize = (String)value.get("normalize");
                    rules.put(name, new Rule(name, injectPatterns(extract, patterns), normalize));
                }
            }
        }
    }

    static private void loadNormalization(Map<String, String> normalization, JSONObject parsed){
        //Map normalizationDef = (JSONObject) parsed.get("normalization");
        ArrayList<JSONObject> normalizationDef = (JSONArray) parsed.get("normalization");
        if (normalizationDef!=null){
            for (JSONObject obj: normalizationDef) {
                for (Object entry : obj.entrySet()) {
                    String key = (String) (((Map.Entry) entry).getKey());
                    Object value = ((Map.Entry) entry).getValue();
                    if (value instanceof String) {           // explicit pattern
                        normalization.put(key, (String) value);
                    } else if (value instanceof Map) {       // group of patterns
                        for (Object entry2 : ((Map) value).entrySet()) {
                            String key2 = (String) (((Map.Entry) entry2).getKey());
                            String value2 = (String) ((Map.Entry) entry2).getValue();
                            normalization.put(key2, (String) value2);
                        }
                    }
                }
            }
        }
    }

    static private String injectPatterns(String extract, Map<String, String> patterns){
        String out = extract;
        for (Map.Entry<String, String> entry: patterns.entrySet()){
            out = out.replaceAll("[$]"+entry.getKey(), Matcher.quoteReplacement("("+entry.getValue()+")"));
        }
        return out;
    }

    static private String join(List l, String s){
        Iterator it = l.iterator();
        if (it.hasNext()) {
            String out = it.next().toString();
            while (it.hasNext()){
                out += s+it.next().toString();
            }
            return out;
        }
        return "";
    }
}
