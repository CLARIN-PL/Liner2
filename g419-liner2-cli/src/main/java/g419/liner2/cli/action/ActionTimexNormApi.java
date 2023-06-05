package g419.liner2.cli.action;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hellokaton.blade.Blade;
import com.hellokaton.blade.annotation.request.Body;
import com.hellokaton.blade.annotation.route.POST;
import com.hellokaton.blade.mvc.http.Response;
import g419.lib.cli.Action;
import g419.liner2.core.normalizer.lval.LValRuleCompiledContainer;
import g419.liner2.core.normalizer.lval.LValRuleContainer;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * Normalize a raw temporal expressions
 *
 * @author Michał Marcińczuk
 */
public class ActionTimexNormApi extends Action {

  private int port = 9000;

  private Map<String, LValRuleCompiledContainer> normalizers = Maps.newHashMap();

  public ActionTimexNormApi() {
    super("timex-norm-api");
    this.setDescription("run a RESTful API for timex normalization");

    this.options.addOption(Option.builder("p").longOpt("port")
              .desc("port number to listen to").hasArgs().build());

    LValRuleCompiledContainer normDate =
            LValRuleContainer.load(getClass().getClassLoader().getResourceAsStream("timex/rules-lval/rules.json"));
    LValRuleCompiledContainer normDuration =
            LValRuleContainer.load(getClass().getClassLoader().getResourceAsStream("timex/rules-lval/rules-duration.json"));

    normalizers.put("t3_date", normDate);
    normalizers.put("t3_time", normDate);
    normalizers.put("t3_duration", normDuration);
  }

  @Override
  public void parseOptions(final CommandLine line) throws Exception {
    this.port = Integer.parseInt(line.getOptionValue("port", "9000"));
  }

  public String normalize(Timex timex){
      String error = null;
      String lval = null;
      String bases = timex.text;
      String type = timex.type;

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

      return json.toString();
  }

  public class Timex {
    private String type;
    private String text;
  }

  /**
   * Module entry function.
   */
  @Override
  public void run() throws Exception {
    Gson g = new Gson();
    Blade.create().listen(this.port).post("/timexnorm", ctx -> {
      Timex timex = g.fromJson(ctx.bodyToString(), Timex.class);
      ctx.response().json(this.normalize(timex));
    }).start();
  }

}
