package g419.liner2.api.normalizer;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.liner2.api.normalizer.global_rules.Rule;
import g419.liner2.api.normalizer.global_rules.date.*;
import g419.liner2.api.normalizer.global_rules.time.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RuleGlobalTimexNormalizer extends NormalizingChunker {

    static final Logger logger;
    static final Pattern dateRegex;
    static final Map<String, List<Rule>> rulesPerType;


    static {
        logger = LoggerFactory.getLogger(RuleGlobalTimexNormalizer.class);
        dateRegex = Pattern.compile("(\\d\\d\\d\\d\\-\\d\\d\\-\\d\\d)");
        rulesPerType = new HashMap<>();
        //if we use setWeekday instead of closestWeekday, we get 2 TP more (unless we use rule 10 and 11)
        rulesPerType.put("t3_date", Arrays.<Rule>asList(
                new DateRule1(),
                new DateRule2(),
                new DateRule3(),
                new DateRule4(),
                new DateRule5(),
                new DateRule6(),
                new DateRule7(),
                new DateRule8(),
                new DateRule9(),
                new DateRule10(),
                new DateRule11()
        ));
        rulesPerType.put("t3_time", Arrays.<Rule>asList(
                new TimeRule1(),
                new TimeRule2(),
                new TimeRule3(),
                new TimeRule4(),
                new TimeRule5(),
                new TimeRule6()
        ));
    }

    public String previous = null;
    public String first = null;
    public String creationDate = null;

    public RuleGlobalTimexNormalizer(List<Pattern> normalizedChunkTypes) {
        super(normalizedChunkTypes);
    }

    @Override
    public void normalize(Annotation annotation) {
        if (shouldNormalize(annotation)) {
            List<Rule> rules = rulesPerType.get(annotation.getType());
            if (rules != null) {
                String result = null;
                String lval = annotation.getMetadata("lval");
                String baseText = annotation.getBaseText();
                if (lval != null) {
                    for (Rule rule : rules){
                        if (rule.matches(lval, baseText)){
                            result = rule.normalize(lval, baseText, previous, first, creationDate);
                            if (result!=null) {
                                annotation.setMetadata("val", result);
                                break;
                            }
                        }
                    }
                    if (result == null)
                        annotation.setMetadata("val", lval);
                    update(annotation.getMetadata("val"));
                }
            }
            else {
                String result = null;
                String lval = annotation.getMetadata("lval");
                String baseText = annotation.getBaseText();
                if (lval != null) {
                    annotation.setMetadata("val", lval);
                }
            }
        }
    }

    protected void update(String val){
        if (val != null) {
            Matcher matcher = dateRegex.matcher(val);
            if (matcher.find()) {//it was search() in python version, though I think it was a mistake
                previous = matcher.group();
                if (first==null)
                    first = previous;
            }
        }
    }

    @Override
    public void onNewDocument(Document document){
        String dateStr = document.getDocumentDescriptor().getDescription().get("date");
        if (dateStr != null)
            creationDate = dateStr;
        logger.debug("New document, creation = "+ creationDate);

    }
}
