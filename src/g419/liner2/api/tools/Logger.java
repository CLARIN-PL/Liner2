package g419.liner2.api.tools;

import g419.liner2.api.LinerOptions;

public class Logger {

	/**
	 * Messages are print to std.out only with -verbose parameter.
	 * @param text
	 */
	public static void log(String text){ 
		Logger.log(text, false);
	}

	/**
	 * Messages are print to std.out only with -verbose or -verboseDetails parameter.
	 * @param text
	 * @param details
	 */
	public static void log(String text, boolean details){
		if (LinerOptions.getGlobal().verboseDetails || (!details && LinerOptions.getGlobal().verbose) )
			System.out.println(text);
	}

}
