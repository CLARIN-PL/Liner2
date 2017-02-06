package g419.liner2.api.normalizer.lval;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by kotu on 12.01.17.
 */
public class LValRule {
    public String desc;
    public ArrayList<String> keys;
    public ArrayList<String> groups;
    public String match;
    public Map<String, String> map;
    public ArrayList<String> limit;
    public Map<String, String> value;
    public Pattern pattern;

}
