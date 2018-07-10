package g419.liner2.daemon;

import g419.liner2.core.LinerOptions;

import java.util.HashMap;

/**
 * Created by michal on 6/25/14.
 */
public class DaemonOptions extends LinerOptions {

    static protected final DaemonOptions daemonOptions = new DaemonOptions();

    public HashMap<String, LinerOptions> models = null;
    public String defaultModel = null;

    public static DaemonOptions getGlobal(){
        return daemonOptions;
    }

}
