package liner2.daemon;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;

public class LinerDaemon implements Daemon {

	@Override
	public void destroy() {
	}

	@Override
	public void init(DaemonContext context) {
		System.out.println("Daemon initialize.");
	}

	@Override
	public void start() {
		System.out.println("Started daemon.");
	}
	
	@Override
	public void stop() {
		System.out.println("Stopped daemon.");
	}
}
