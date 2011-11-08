package liner2.action;

import liner2.tools.LinerServer;
import liner2.LinerOptions;
import liner2.Main;

/**
 * Daemon mode for liner2 WebService.
 * @author Maciej Janicki
 * 
 */
public class ActionDaemon extends Action{
	public void run() {
		try {
			new LinerServer().run();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
