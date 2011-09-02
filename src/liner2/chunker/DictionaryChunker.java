package liner2.chunker;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;

import liner2.structure.Chunk;
import liner2.structure.Chunking;
import liner2.structure.Sentence;
import liner2.structure.Token;

import liner2.Main;

/**
 * @author Maciej Janicki
 * @author Michał Marcińczuk
 */

public class DictionaryChunker extends Chunker 
	implements DeserializableChunkerInterface, SerializableChunkerInterface {
	
//	private String modelFilename = null;
//	private static final long serialVersionUID = 472384;
	private HashMap<String, String> dictionary = null;
	
	public DictionaryChunker()	{

	}
	
	@Override
	public Chunking chunkSentence(Sentence sentence) {
		//System.out.println("chunkSentence");
		Chunking chunking = new Chunking(sentence);
		
		// pobierz zapis wszystkich tokenów
		ArrayList<String> tokenOrths = new ArrayList<String>();
		for (Token token : sentence.getTokens())
			tokenOrths.add(token.getFirstValue());
			
		// pomocnicza tablica, które tokeny należą już do jakiegoś chunka
		boolean[] marked = new boolean [sentence.getTokenNumber()];
		for (int i = 0; i < sentence.getTokenNumber(); i++)
			marked[i] = false;
			
		// nie sprawdzać pozycji, które są już ochunkowane!
		// iteruj po wszystkich n-gramach w zdaniu
		for (int n = sentence.getTokenNumber(); n > 0; n--)
			// po wszystkich pozycjach startowych
			for (int i = 0; i < sentence.getTokenNumber() - n + 1; i++) {
				boolean isMarked = false;
				StringBuilder nGram = new StringBuilder();
				// po wszystkich słowach należących do n-gramu
				for (int j = i; j < i + n; j++) {
					if (marked[j]) {
						isMarked = true;
						break;
					}
					nGram.append(" " + tokenOrths.get(j));
				}
				if (isMarked)
					continue;
				String nGramFinal = nGram.toString().trim();
				if (this.dictionary.containsKey(nGramFinal)) {
					chunking.addChunk(new Chunk(i, i + n - 1, this.dictionary.get(nGramFinal).toUpperCase(), sentence));
					for (int j = i; j < i + n; j++)
						marked[j] = true;
				}
				//System.out.println(nGram);
			}
		
		return chunking;
	}
	
	public void loadDictionary(String dictFile, String commonsFile) {
		try {
			BufferedReader commonsReader = new BufferedReader(new FileReader(commonsFile));
			HashSet<String> commons = new HashSet<String>();
			HashSet<String> ambigous = new HashSet<String>();
			this.dictionary = new HashMap<String, String>();
			
			String line = commonsReader.readLine();
			while (line != null) {
				commons.add(line);
				line = commonsReader.readLine();
			}
			commonsReader.close();
			
			BufferedReader dictReader = new BufferedReader(new FileReader(dictFile));
			line = dictReader.readLine();
			int added = 0, amb = 0, com = 0;
			while (line != null) {
				String[] content = line.split("\t");
				//System.out.println(""+i);
				if (content.length >= 2) {
					if ((commons.contains(content[1])) || (commons.contains(content[1].toLowerCase())))
					//if (binarySearch(content[1], this.commons) != -1)
						com++;
					else if (ambigous.contains(content[1]))
						amb++;
					else if (this.dictionary.containsKey(content[1])) {
						this.dictionary.remove(content[1]);
						ambigous.add(content[1]);
						amb += 2;
						added -= 1;
					}
					else {
//						System.out.println("Adding: " + content[0] + " : " + content[1]);
						this.dictionary.put(content[1], content[0]);
						added++;
					}
				}
				line = dictReader.readLine();
			}
			Main.log("Dictionary chunker compiled.", true);
			Main.log("Added: " + added, true);
			Main.log("Skipped: " + (amb+com) + " of which " + amb + " ambigous, " + com + " common.", true);
			dictReader.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
//	private int binarySearch(String what, ArrayList<String> where) {
//		int start = 0;
//		int end = where.size()-1;
//		while (start <= end) {
//			System.out.println(""+start+" - " + end);
//			int middle = (start + end) / 2;
//			int compare = what.compareTo(where.get(middle));
//			if (compare < 0)
//				end = middle - 1;
//			else if (compare > 0)
//				start = middle + 1;
//			else
//				return middle;
//		}
//		return -1;
//	}
//	
//	public void setModelFilename(String filename) {
//		this.modelFilename = filename;
//	}
	
    /**
     * Wczytuje chunker z modelu binarnego.
     * @param model_filename
     */
	@Override
	@SuppressWarnings("unchecked")
    public void deserialize(String filename){
    	try {
    		ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
   			this.dictionary = (HashMap<String, String>)in.readObject();
    		in.close();
    	} catch (ClassNotFoundException ex) {
    		ex.printStackTrace();
    	} catch (IOException ex) {
    		ex.printStackTrace();
    	}    	
    }

	@Override
	public void serialize(String filename) {
		// TODO Auto-generated method stub
		try {
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));
			out.writeObject(this.dictionary);
			out.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void close() {
		
	}	
	
}	
