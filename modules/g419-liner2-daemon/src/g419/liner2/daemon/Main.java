package g419.liner2.daemon;

import g419.liner2.api.tools.ParameterException;

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
		try {
            options.parse(args);
            String db_type = options.getProperties().getProperty("db_type");
            if(db_type.equals("sql")){
                new SQLDaemonThread().run();
            }
            else if(db_type.equals("file")){
                new FilebasedDaemonThread().run();
            }
            else{
                throw new ParameterException("Invalid db_type: " + db_type);
            }
		} catch (Exception ex) {
            System.out.println(ex);
            System.out.println("\n----------------------------------------HELP----------------------------------------");
            ((DaemonOptions) DaemonOptions.getGlobal()).printModes();
		}
    }
    
}
