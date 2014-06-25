package g419.liner2.daemon;

import g419.liner2.api.LinerOptions;

/**
 * Run the module. 
 * 
 * @author Michał Marcińczuk
 * @author Maciej Janicki
 */
public class Main {
    
    /**
     * Here the story begins.
     */
    public static void main(String[] args) throws Exception {

        DaemonOptions.getGlobal().parse(args);
		try {
			new DaemonThread().run();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
    }
    
}
