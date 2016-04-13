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

//    static final List<Rule> dateRules;
//    static final List<Rule> generalRules;
//    static final List<Rule> timeRules;

    static {
        logger = LoggerFactory.getLogger(RuleGlobalTimexNormalizer.class);
        dateRegex = Pattern.compile("(\\d\\d\\d\\d\\-\\d\\d\\-\\d\\d)");
//        dateRules = Arrays.<Rule>asList(
//                new DateRule1(),
//                new DateRule2(),
//                new DateRule3(),
//                new DateRule4(),
//                new DateRule5()
//        );
//        generalRules = Arrays.<Rule>asList(
//                new DateRule6(),
//                new DateRule7(),
//                new DateRule8(),
//                new DateRule9(),
//                new DateRule10(),
//                new DateRule11()
//        );
//        timeRules = Arrays.<Rule>asList(
//                new TimeRule1(),
//                new TimeRule2(),
//                new TimeRule3(),
//                new TimeRule4(),
//                new TimeRule5(),
//                new TimeRule6()
//        );
        rulesPerType = new HashMap<>();
        //if we use setWeekday instead of closestWeekday, we get 2 TP more (unless we use rule 10 and 11)
        rulesPerType.put("t3_date", Arrays.<Rule>asList(  //849/317/0
                new DateRule1(), //846/320/0
                new DateRule2(), //820/346/0
                new DateRule3(), //883/283/0
                new DateRule4(), //887/279/0
                new DateRule5(), //884/282/0
                new DateRule6(), //865/301/0
                new DateRule7(), //863/303/0
                new DateRule8(), //817/349/0
                new DateRule9(), //818/348/0
                new DateRule10(),//819/347/0
                new DateRule11() //819/347/0
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
//            List<Rule> rules = new ArrayList<>();
//            if (annotation.getType() == "t3_date")
//                rules.addAll(dateRules);
//            rules.addAll(generalRules);
//            if (annotation.getType() == "t3_time")
//                rules.addAll(dateRules);
            if (rules != null) {
                String result = null;
                String lval = annotation.getMetadata("lval");
                String baseText = annotation.getBaseText();
                if (lval != null) {
                    for (Rule rule : rules){
                        if (rule.matches(lval, baseText)){
//                            if (lval.equals("xxxx-12-10TEV"))
//                                throw new RuntimeException(""+rule.getClass()+" ; "+baseText+" ; "+lval);
                            result = rule.normalize(lval, baseText, previous, first, creationDate);
                            if (result!=null) {
//                                if (result.equals("2006-01-17"))
//                                    getClass();
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
//            first = previous = dateStr.trim();
        logger.debug("New document, creation = "+ creationDate);

    }
}
