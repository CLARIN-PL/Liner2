package g419.spatial.action;

import g419.lib.cli.action.Action;
import g419.spatial.io.SpatialPatternParser;
import g419.toolbox.sumo.Sumo;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.MissingResourceException;

public class ActionTest2 extends Action {
	
	public ActionTest2() {
		super("test2");
		this.setDescription("quick test");		
	}
	

	/**
	 * Parse action options
	 * @param arg0 The array with command line parameters
	 */
	@Override
	public void parseOptions(String[] args) throws Exception {
    }

	@Override
	public void run() throws Exception {
		
		String location = "/g419/spatial/resources/spatial_relation_patterns.txt";
		InputStream resource = this.getClass().getResourceAsStream(location);

        if (resource == null)
        {
            throw new MissingResourceException("Resource not found: " + location,
                    this.getClass().getName(), location);
        }
        
        SpatialPatternParser parser = new SpatialPatternParser(new InputStreamReader( resource ), new Sumo(false));
        parser.parse();
	}
	
}
