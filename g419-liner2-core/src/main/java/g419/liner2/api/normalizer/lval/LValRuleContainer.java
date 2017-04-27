package g419.liner2.api.normalizer.lval;


import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import g419.corpus.structure.Annotation;
import org.apache.commons.lang3.StringUtils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by kotu on 12.01.17.
 */
public class LValRuleContainer {
    public Map<String, List<String>> keys;
    public Map<String, Map<String, String>> maps;
    public List<LValRule> rules;

    public void prepareRules() {
        for (LValRule rule : rules) {
            String match = new String(rule.match);
            for (Map.Entry<String, List<String>> keyEntry : keys.entrySet())
                match = match.replaceAll("\\$" + keyEntry.getKey(), StringUtils.join(keyEntry.getValue(), "|")).replaceAll("%", "\\\\");
            rule.pattern = Pattern.compile(match);
        }
    }

    public static LValRuleContainer load(String path){
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        JsonReader jsonReader = new JsonReader(fileReader);
        GsonBuilder builder = new GsonBuilder();
        LValRuleContainer ruleContainer = builder.create().fromJson(jsonReader, LValRuleContainer.class);
        ruleContainer.prepareRules();
        return ruleContainer;
    }


    public String getLVal(Annotation annotation){
        String annBase = annotation.getSimpleBaseText();
        String annType = annotation.getType();
        List<LValRule> rulesUsed = new LinkedList<>();
        //ostateczne wartości year, month, day, hour
        Map<String, String> globalValues = new HashMap<>();
        for (LValRule rule : rules){
            if (rule.limit != null && !rule.limit.contains(annType))
                continue;
            //czy reguła na ciągu base'ów jest spełniona
            Matcher match = rule.pattern.matcher(annBase);
            if (match.find() && rule.checkLemmaTags(annotation)){
                //zapisz użytą regułę
                rulesUsed.add(rule);
                Map<String, String> matchDict = new HashMap<>();
                //pobierz lokalne matche
                for (String group : rule.groups) {
                    matchDict.put(group, match.group(group));
                }
                //zmapuj lokalne matche
                for (Map.Entry<String, String> ruleMapEntry : rule.map.entrySet()) {
                    String matchKey = ruleMapEntry.getKey();
                    String mapName = ruleMapEntry.getValue();
                    matchDict.put(matchKey, this.maps.get(mapName).get(matchDict.get(matchKey)));
                }
                //wstaw ostateczne wartości do docelowego słownika
                for (Map.Entry<String, String> ruleValueEntry : rule.value.entrySet()) {
                    String valueKey = ruleValueEntry.getKey();
                    String value = new String(ruleValueEntry.getValue());
                    //zmodyfikuj szablon wartości w oparciu o lokalne matche
                    for (Map.Entry<String, String> matchDictEntry : matchDict.entrySet()) {
                        String matchKey = matchDictEntry.getKey();
                        String matchValue = matchDictEntry.getValue();
                        value = value.replaceAll("\\$" + matchKey, matchValue);
                    }
                    //wstaw do globalnego słownika ostateczny value (lub nadpisz)
                    globalValues.put(valueKey, value);
                }
            }
        }
        String lval = "None";
        Map<String, String> matches = globalValues;
        if (!annType.equals("t3_duration")) {
            if (!matches.isEmpty()) {
                lval = "";
                if (matches.containsKey("year") && !matches.get("year").equals("null")) { //"null" is not a mistake!
                    lval += matches.get("year");
                    if (matches.containsKey("month") && !matches.get("month").equals("null")) {
                        String month = matches.get("month");
                        if (StringUtils.isNumeric(month))
                            month = StringUtils.leftPad(month, 2, "0");
                        lval += "-" + month;
                        if (matches.containsKey("day") && !matches.get("day").equals("null")) {
                            String day = matches.get("day");
                            if ((StringUtils.isNumeric(month) || month.equals("xx")) && StringUtils.isNumeric(day))
                                day = StringUtils.leftPad(day, 2, "0");
                            lval += "-" + day;
                        }
                    }
                } else {
                    if (matches.containsKey("month") && !matches.get("month").equals("null")) {
                        lval = "xxxx";
                        String month = matches.get("month");
                        if (StringUtils.isNumeric(month))
                            month = StringUtils.leftPad(month, 2, "0");
                        lval += "-" + month;
                        if (matches.containsKey("day") && !matches.get("day").equals("null")) {
                            String day = matches.get("day");
                            if ((StringUtils.isNumeric(month) || month.equals("xx")) && StringUtils.isNumeric(day))
                                day = StringUtils.leftPad(day, 2, "0");
                            lval += "-" + day;
                        }
                    } else {
                        if (matches.containsKey("day") && !matches.get("day").equals("null")) {
                            String day = matches.get("day");
                            if (StringUtils.isNumeric(day))
                                day = StringUtils.leftPad(day, 2, "0");
                            lval += day;
                        }
                    }

                }
                if (matches.containsKey("part"))
                    lval += "-" + matches.get("part");
                if (matches.containsKey("hour")) {
                    if (lval.isEmpty())
                        lval = "xxxx-xx-xx";
                    else if (!matches.containsKey("day"))
                        lval += "-xx";

                    String hour = matches.get("hour");
                    if (!hour.equals("null")) {
                        if (StringUtils.isNumeric(hour)) {
                            hour = StringUtils.leftPad(hour, 2, "0");
                            if (Integer.parseInt(hour) < 13 && !matches.containsKey("separator"))
                                lval += "t" + hour;
                            else {
                                if (matches.containsKey("separator") && Integer.parseInt(hour) < 13 && matches.get("separator").equals("EV"))
                                    lval += "T" + (Integer.parseInt(hour) + 12);
                                else
                                    lval += "T" + hour;
                            }
                        } else if (hour.equals("xx"))
                            lval += "t" + hour;
                        else
                            lval += "T" + hour;
                        if (matches.containsKey("minute")) {
                            String minute = matches.get("minute");
                            minute = StringUtils.leftPad(minute, 2, "0");
                            lval += ":" + minute;
                            if (matches.containsKey("second")) {
                                String second = matches.get("second");
                                second = StringUtils.leftPad(second, 2, "0");
                                lval += ":" + second;
                            }
                        }
                    }
                } else if (matches.containsKey("minute")) {
                    if (lval.isEmpty())
                        lval = "xxxx-xx-xxtxx";
                    String minute = matches.get("minute");
                    minute = StringUtils.leftPad(minute, 2, "0");
                    lval += ":" + minute;
                    if (matches.containsKey("second")) {
                        String second = matches.get("second");
                        second = StringUtils.leftPad(second, 2, "0");
                        lval += ":" + second;
                    }
                }

            }
        }
        else {
            lval = "";
            if (!matches.isEmpty()) {
                lval += "P";
                if (matches.containsKey("millenium")){
                    lval += matches.get("millenium") + "ML";
                }
                if (matches.containsKey("century")){
                    lval += matches.get("century") + "CE";
                }
                if (matches.containsKey("year")){
                    lval += matches.get("year") + "Y";
                }
                if (matches.containsKey("month")){
                    lval += matches.get("month") + "M";
                }
                if (matches.containsKey("week")){
                    lval += matches.get("week") + "W";
                }
                if (matches.containsKey("day")){
                    lval += matches.get("day") + "D";
                }
                if (matches.containsKey("evening")){
                    lval += matches.get("evening") + "EV";
                }
                if (matches.containsKey("time")){
                    lval += "T";
                }
                if (matches.containsKey("hour")){
                    lval += matches.get("hour") + "H";
                }
                if (matches.containsKey("minute")){
                    lval += matches.get("minute") + "M";
                }
                if (matches.containsKey("second")){
                    lval += matches.get("second") + "S";
                }


            }
            else {
                lval = "VAGUE";
            }
        }
        return lval;
    }


}
