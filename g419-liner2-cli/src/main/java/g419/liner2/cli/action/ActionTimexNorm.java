package g419.liner2.cli.action;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import g419.lib.cli.Action;
import g419.lib.cli.ParameterException;
import g419.liner2.core.normalizer.lval.LValRuleContainer;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;

/**
 * Normalize a raw temporal expressions
 *
 * @author Michał Marcińczuk
 */
public class ActionTimexNorm extends Action {

  private String bases = null;

  private String type = null;

  private Map<String, LValRuleContainer> normalizers = Maps.newHashMap();

  private static final String PARAM_LEMMAS = "b";
  private static final String PARAM_CATEGORY = "t";

  public ActionTimexNorm() {
    super("timex-norm");
    this.setDescription("normalize a raw temporal expressions");

    LValRuleContainer normDate =
            LValRuleContainer.load(getClass().getClassLoader().getResourceAsStream("timex/rules-lval/rules.json"));
    LValRuleContainer normDuration =
            LValRuleContainer.load(getClass().getClassLoader().getResourceAsStream("timex/rules-lval/rules-duration.json"));

    normalizers.put("t3_date", normDate);
    normalizers.put("t3_time", normDate);
    normalizers.put("t3_duration", normDuration);
  }

  public static Option getLemmasOption() {
    return Option.builder(PARAM_LEMMAS)
            .longOpt("bases").required()
            .hasArg().argName("TEXT").desc("space-separated base forms of timex words").build();
  }

  public static Option getCategoryOption() {
    return Option.builder(PARAM_CATEGORY)
            .longOpt("type").required()
            .hasArg().argName("NAME").desc("TIMEX type (t3_time, t3_date or t3_duration)").build();
  }

  @Override
  public void parseOptions(final CommandLine line) throws Exception {
//    bases = line.getOptionValue(PARAM_LEMMAS);
//    type = line.getOptionValue(PARAM_CATEGORY);
  }

  /**
   * Module entry function.
   */
  @Override
  public void run() throws Exception {
    BufferedReader input_reader = new BufferedReader(new InputStreamReader(System.in));

    String line;
    while ((line = input_reader.readLine()) != null) {
      String[] parts = line.split(":", 2);
      String bases = null;
      String type = null;
      String error = null;
      String lval = null;

      if (parts.length == 2){
        type = parts[0];
        bases = parts[1];
      }

      if (bases == null) {
        error = "Base form are missing";
      }else if (type == null){
        error = "TIMEX type is missing";
      }else if (!normalizers.containsKey(type)){
        error = "Unknown TIMEX type.";
      }else{
        lval = normalizers.get(type).getLVal(bases, type);
      }

      JsonObject json = new JsonObject();
      json.addProperty("input", line);
      if (error != null){
        json.addProperty("error", error);
      }
      if (bases != null){
        json.addProperty("bases", bases);
      }
      if (type != null){
        json.addProperty("type", type);
      }
      if (lval != null){
        json.addProperty("lval", lval);
      }

      System.out.println(json.toString());

    }
    input_reader.close();
  }

}
