package g419.liner2.api.normalizer.rbn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Single normalization rule. Takes no normalization symbols into account (they are handled by RuleSet).
 * @see RuleSet
 */
public final class Rule {
    static private Logger log = LoggerFactory.getLogger(Rule.class);
    /**
     * Name of this rule.
     */
    final public String name;
    /**
     * Regex pattern that must match expression if we want to normalize it.
     * If we used some named patterns in JSON file, they will be replaced with real regexes before creating Rule
     * instance.
     * <strong>Remember that every named pattern from file will be capturing group!.</strong>
     */
    final public Pattern extractionPattern;
    /**
     * Normalizing clause. If there are substring of form "@group(x)" in it, they will be replaced with captured groups
     * while performing normalization.
     * <strong>Look out! If there are spaces in between parentheses in "@group(x)" (like "@group( 0)") substitution will
     * fail!</strong>
     */
    final public String normalize;

    /**
     * Package-visible constructor, meant to be used by RuleSetLoader only.
     * @see g419.liner2.api.normalizer.rbn.RuleSetLoader
     */
    Rule(String name, String extractionPattern, String normalize) {
        this.name = name;
        this.extractionPattern = Pattern.compile(extractionPattern);
        this.normalize = normalize;
        log.debug("Created rule '"+name+"' with extraction pattern '"+this.extractionPattern.pattern()+
                "' and normalization string '"+normalize+"'");
    }

    /**
     * Could this rule normalize given expression?
     */
    public boolean matches(String expression){
        return extractionPattern.matcher(expression).matches();
    }

    /**
     * Perform normalization of expression according to this rule.
     */
    public String normalize(String expression){
        Matcher matcher = extractionPattern.matcher(expression);
        if (!matcher.matches()) {
            RuntimeException up = new IllegalArgumentException(
                    "Expression "+expression+" cannot be normalized by rule "+name+
                            "because it is not matched by that rule."
            );
            throw up;
        }
        String result = normalize;
        for(int i=0; i<matcher.groupCount(); ++i){
            result = result.replaceAll("[@]group[(]"+i+"[)]", matcher.group(i+1));
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rule rule = (Rule) o;
        if (!extractionPattern.pattern().equals(rule.extractionPattern.pattern())) return false;
        if (!name.equals(rule.name)) return false;
        if (!normalize.equals(rule.normalize)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + extractionPattern.pattern().hashCode();
        result = 31 * result + normalize.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Rule{" +
                "name='" + name + '\'' +
                ", extractionPattern=" + extractionPattern +
                ", normalize='" + normalize + '\'' +
                '}';
    }

    /**
     * Getter for <em>name</em> attribute (it is final and public, but without this, some bean-based tools will
     * go nuts).
     */
    public String getName() {
        return name;
    }

    /**
     * Getter for <em>extractionPattern</em> attribute (it is final and public, but without this, some bean-based
     * tools will go nuts).
     */
    public Pattern getExtractionPattern() {
        return extractionPattern;
    }

    /**
     * Getter for <em>name</em> attribute (it is final and public, but without this, some bean-based tools
     * will go nuts).
     */
    public String getNormalize() {
        return normalize;
    }
}
