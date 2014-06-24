package g419.liner2.daemon;

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
		try {
			new DaemonThread().run();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
    }
    
}
