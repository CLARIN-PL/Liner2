package g419.liner2.api.normalizer.rbn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Set of rules, capable of choosing and using proper rule for given expression and further normalizing with
 * normalizing symbols.
 */
public final class RuleSet {
    static private Logger log = LoggerFactory.getLogger(RuleSet.class);

    /**
     * Immutable map from rule names to rule objects. Represents "rules" section of JSON file.
     */
    final public Map<String, Rule> rules;
    /**
     * Immutable map from non-normalized symbols to their substitutions. Represents flattened "normalization" section of
     * JSON file (without value grouping).
     */
    final public Map<String, String> normalization;

    /**
     * Package visibility - use loader.load() instead.
     * @see g419.liner2.api.normalizer.rbn.RuleSetLoader#load
     */
    RuleSet(Map<String, Rule> rules, Map<String, String> normalization) {
        this.rules = Collections.unmodifiableMap(rules);
        this.normalization = Collections.unmodifiableMap(normalization);
    }

    /**
     * Perform normalization of given expression accorging to this set of rules.
     * @return Normalized expression, or null (if no rule matched it)
     * @throws java.lang.IllegalStateException If more than one rule matches this expression
     */
    public String normalize(String expression){
        Rule matchedRule=null;
        for (Rule rule: rules.values())
            if (rule.matches(expression))
                if (matchedRule==null)
                    matchedRule=rule;
                else {
                    RuntimeException up = new IllegalStateException(
                            "Expression "+expression+" was matched by more than one rule! It was already matched "+
                                "by rule "+matchedRule.name+" and now it was matched by "+rule.name+"!"
                    );
                    throw up;
                }
        if (matchedRule==null) {
            log.info("No rule matching expression '"+expression+"' was found! Returning null.");
            return null;
        }
        log.info("Normalizing expression '"+expression+"' with rule "+matchedRule.name);
        String normalized = matchedRule.normalize(expression);
        log.info("Result of rule usage: "+normalized);
        for (Map.Entry<String, String> entry: normalization.entrySet()){
            normalized = normalized.replaceAll(entry.getKey(), entry.getValue());
        }
        log.info("Result of values normalization: "+normalized);
        return normalized;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RuleSet ruleSet = (RuleSet) o;

        if (!normalization.equals(ruleSet.normalization)) return false;
        if (!rules.equals(ruleSet.rules)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = rules.hashCode();
        result = 31 * result + normalization.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "RuleSet{" +
                "rules=" + rules +
                ", normalization=" + normalization +
                '}';
    }

    /**
     * Getter for <em>rules</em> attribute (it is immutable, final and public, but without this, some bean-based tools
     * will go nuts).
     */
    public Map<String, Rule> getRules() {
        return rules;
    }

    /**
     * Getter for <em>normalization</em> attribute (it is immutable, final and public, but without this, some
     * bean-based tools will go nuts).
     */
    public Map<String, String> getNormalization() {
        return normalization;
    }
}
