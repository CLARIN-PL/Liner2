package g419.liner2.api.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.log4j.Logger;

import com.google.common.io.Files;

import g419.corpus.schema.kpwr.KpwrNer;

/**
 * 
 * 
 * @author Michał Marcińczuk
 *
 */
public class NeLemmatizer {

	Map<String, String> lemmas = new HashMap<String, String>();
	
	/** Parts of person names, i.e. first and last names. */
	Map<String, String> lemmasPerson = new HashMap<String, String>();
	
	/**
	 * 
	 * @param path path to a file with lemmatization dictionary. 
	 * Each line should contain category, form and lemma separated with tabs.
	 * @throws IOException 
	 */
	public NeLemmatizer(String path) throws IOException{
		
		InputStream stream = null;
		BufferedReader reader = null;
		IOException exception = null;
		try{
			stream = new FileInputStream(path);
			if ( path.endsWith(".gz") ){		
				stream = new GZIPInputStream(stream);
			}
			reader = new BufferedReader(new InputStreamReader(stream));
			String line = null;
			while ( (line = reader.readLine()) != null ){
				line = line.trim();
				if ( !line.startsWith("#") && line.length() > 0 ){
					String cols[] = line.split("\t");
					if ( cols.length != 3 ){
						Logger.getLogger(this.getClass()).error("Incorrect line format: " + line);
					} else {
						this.lemmas.put(cols[1].toLowerCase(), cols[2]);
						this.lemmas.put(cols[2].toLowerCase(), cols[2]);
					}
					
					if ( KpwrNer.NER_LIV_PERSON.equals(cols[0]) ){
						String formParts[] = cols[1].split(" ");
						String baseParts[] = cols[2].split(" ");
						if ( formParts.length == baseParts.length ){
							for ( int i=0; i<formParts.length; i++ ){
								this.lemmasPerson.put(formParts[i].toLowerCase(), baseParts[i]);
								this.lemmasPerson.put(baseParts[i].toLowerCase(), baseParts[i]);
							}
						}
						this.lemmasPerson.put(cols[1].toLowerCase(), cols[2]);
						this.lemmasPerson.put(cols[2].toLowerCase(), cols[2]);
					}
				}
			}
		} catch (IOException ex){
			exception = ex;
		} finally {
			if ( reader != null ){
				reader.close();
			}else if ( stream != null ){
				stream.close();
			}			
		}
		if ( exception != null ){
			throw exception;
		}
		
	}
	
	public String lemmatize(String name){
		return this.lemmas.get(name.toLowerCase());
	}
	
	public String lemmatizePersonName(String name){
		return this.lemmasPerson.get(name.toLowerCase());
	}
}
