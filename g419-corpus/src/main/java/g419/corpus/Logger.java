package g419.corpus;

public class Logger {

    public static boolean verbose = false;
    public static boolean verboseDetails = false;

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
		if (verboseDetails || (!details && verbose) )
			System.out.println(text);
	}

}
