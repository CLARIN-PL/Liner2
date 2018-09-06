package g419.spatial.action;

import g419.lib.cli.Action;
import g419.spatial.io.CsvSpatialSchemeParser;
import g419.toolbox.sumo.Sumo;
import org.apache.commons.cli.CommandLine;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.MissingResourceException;

public class ActionSemanticPatterns extends Action {
	
	public ActionSemanticPatterns() {
		super("semantic-patterns");
		this.setDescription("parse file with semantic patterns");		
	}
	

	/**
	 * Parse action options
	 * @param arg0 The array with command line parameters
	 */
	@Override
	public void parseOptions(final CommandLine line) throws Exception {
    }

	@Override
	public void run() throws Exception {
		
		String location = "/g419/spatial/resources/spatial_schemes.csv";
		InputStream resource = this.getClass().getResourceAsStream(location);

        if (resource == null)
        {
            throw new MissingResourceException("Resource not found: " + location,
                    this.getClass().getName(), location);
        }
        
        System.out.println("=== Ogół ===");
        (new CsvSpatialSchemeParser(new InputStreamReader( resource ), new Sumo(false))).parse();

		resource = this.getClass().getResourceAsStream(location);
        System.out.println("=== Prototyp ===");
        (new CsvSpatialSchemeParser(new InputStreamReader( resource ), new Sumo(false), false)).parse();
	}
	
}
