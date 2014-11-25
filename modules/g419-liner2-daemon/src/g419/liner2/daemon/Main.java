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

        DaemonOptions options =  (DaemonOptions)DaemonOptions.getGlobal();
        options.parse(args);
		try {
            String db_type = options.getProperties().getProperty("db_type");
            if(db_type.equals("sql")){
                new SQLDaemonThread().run();
            }
            else if(db_type.equals("file")){
                new FilebasedDaemonThread().run();
            }
		} catch (Exception ex) {
			ex.printStackTrace();
		}
    }
    
}
