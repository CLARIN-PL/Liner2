package g419.tools.action;

import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.tools.utils.Counter;
import org.apache.commons.cli.CommandLine;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FeatureFilter extends Action {
	
	private String inputFilename = null;	
	private String outputFilename = null;
	private Map<String, Counter> features = new HashMap<String, Counter>();
	
	public FeatureFilter() {
		super("feature-filter");
		this.setDescription("przetwarza plik z cechami tokenów i filtruje cechy zgodnie z określonymi warunkami");
        this.options.addOption(CommonOptions.getInputFileNameOption());
        this.options.addOption(CommonOptions.getOutputFileNameOption());
	}
	
	/**
	 * Parse action options
	 * @param line The array with command line parameters
	 */
	@Override
	public void parseOptions(final CommandLine line) throws Exception {
        this.inputFilename = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE_LONG);
        this.outputFilename = line.getOptionValue(CommonOptions.OPTION_OUTPUT_FILE_LONG);
    }

	@Override
	public void run() throws Exception {
		
		int minCount = 5;
		
		BufferedReader reader = new BufferedReader(new FileReader(this.inputFilename));
		String line = null;
		int n=0;
		while ((line=reader.readLine())!=null){
			if ( n++%10000 == 0 ){
				Logger.getLogger(getClass()).info(String.format("Linie %d ...", n));
			}
			line = line.trim();
			if ( line.length() == 0 ){
				continue;
			}
			String[] cols = line.split(" ");
			for ( int i=0; i<cols.length-1; i++){
				String feature = cols[i];
				Counter c = this.features.get(feature);
				if ( c == null ){
					c = new Counter();
					this.features.put(feature, c);
				}
				c.increment();
			}
		}
		reader.close();
		
		Set<String> featuresToRemove = new HashSet<String>();
		for ( String feature : this.features.keySet() ){
			if ( this.features.get(feature).getValue() < minCount ){
				featuresToRemove.add(feature);
			}			
		}
		
		System.out.println(String.format("Number of features: %d", this.features.size()));
		System.out.println(String.format("Features with count<%d: %d", minCount, featuresToRemove.size()));
		System.out.println(String.format("Features to keep: %d", this.features.size()-featuresToRemove.size()));
		
		if ( this.outputFilename != null ){
			reader = new BufferedReader(new FileReader(this.inputFilename));
			BufferedWriter writer = new BufferedWriter(new FileWriter(this.outputFilename));
			while ((line=reader.readLine())!=null){
				if ( n++%10000 == 0 ){
					Logger.getLogger(getClass()).info(String.format("Linie %d ...", n));
				}
				line = line.trim();
				if ( line.length() == 0 ){
					writer.write('\n');
					continue;
				}
				String[] cols = line.split(" ");
				for ( int i=0; i<cols.length-1; i++){
					String feature = cols[i];
					if ( !featuresToRemove.contains(feature) ){
						writer.write(feature);
						writer.write(' ');
					}
				}
				writer.write(cols[cols.length-1]);
				writer.write('\n');
			}
			reader.close();
			writer.close();
		}
	}

}
