package g419.liner2.core.chunker;


import g419.corpus.ConsolePrinter;
import g419.corpus.structure.*;
import g419.liner2.core.chunker.interfaces.DeserializableChunkerInterface;
import g419.liner2.core.chunker.interfaces.SerializableChunkerInterface;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



/**
 * @author Maciej Janicki
 * @author Michał Marcińczuk
 */

public class DictionaryChunker extends Chunker 
	implements DeserializableChunkerInterface, SerializableChunkerInterface {

    static HashMap<String, HashMap<String, String>> loadedDicts = new HashMap<String, HashMap<String, String>>();
    static HashMap<String, HashSet<String>> loadedCommons = new HashMap<String, HashSet<String>>();
	
	private HashMap<String, String> dictionary = null;
	private HashSet<String> commons = null;
	private ArrayList<String> types = null;
	
	public DictionaryChunker(ArrayList<String> types) {
		this.types = types;
		this.dictionary = new HashMap<String, String>();
		this.commons = new HashSet<String>();
	}
	
	private AnnotationSet chunkSentence(Sentence sentence) {
		AnnotationSet chunking = new AnnotationSet(sentence);
		List<Token> tokens = sentence.getTokens();
		int sentenceLength = sentence.getTokenNumber();
		if(sentenceLength > 500) return chunking;
		// [długość] -> początek => n-gram
		ArrayList<HashMap<Integer, String>> nGrams = 
			new ArrayList<HashMap<Integer, String>>();
		
		// wygeneruj unigramy
		nGrams.add(new HashMap<Integer, String>());		
		for (int i = 0; i < sentenceLength; i++)
			nGrams.get(0).put(new Integer(i), tokens.get(i).getOrth());
		// wygeneruj n-gramy
		for (int n = 1; n < sentenceLength; n++) {
			nGrams.add(new HashMap<Integer, String>());
			for (int j = 0; j < sentenceLength - n; j++)
				nGrams.get(n).put(new Integer(j), 
					nGrams.get(n-1).get(j) + " " + tokens.get(j+n).getOrth());
		}
		
		// chunkuj (poczynając od najdłuższych n-gramów)
		for (int n = sentenceLength - 1; n >= 0; n--) {
			for (int i = 0; i < sentenceLength - n; i++) {
				int idx = new Integer(i);
				
				// jeśli danego n-gramu nie ma w tablicy, to kontynuuj
				if (nGrams.get(n).get(idx) == null)
					continue;
				
				// jeśli znaleziono w słowniku
				if (this.dictionary.containsKey(nGrams.get(n).get(idx))) {
					
					// odrzuć, jeśli base jest nazwą pospolitą
					if ((n == 0) && (this.commons.contains(sentence.getAttributeIndex()
						.getAttributeValue(tokens.get(i), "base")))) {
						continue;
					}
					
					String chunkType = this.dictionary.get(nGrams.get(n).get(idx));
					
					// odrzuć, jeśli ten typ nie ma być brany pod uwagę
					if ((this.types != null) && (!this.types.contains(chunkType)))
						continue;
					
					// dodaj chunk
					chunking.addChunk(new Annotation(i, i + n, 	
						this.dictionary.get(nGrams.get(n).get(idx)), sentence));
					
					// usuń z tablicy wszystkie krótsze n-gramy, które zahaczają o to miejsce
					// j - dł. odrzucanego n-gramu - 1, k - pozycja startowa
					for (int j = n; j >= 0; j--)
						for (int k = i-j; k <= i+n; k++)
							nGrams.get(j).remove(new Integer(k));
				}
			}
		}
		
		return chunking;
	}
	
	public void loadDictionary(String dictFile, String commonsFile) {
        String dictName = new File(dictFile).getName();
        if(loadedDicts.containsKey(dictName)){
            this.dictionary = loadedDicts.get(dictName);
            this.commons = loadedCommons.get(dictName);
        }
        else {
            try {
                BufferedReader commonsReader = new BufferedReader(new FileReader(commonsFile));
                HashSet<String> ambigous = new HashSet<String>();

                String line = commonsReader.readLine();
                while (line != null) {
                    this.commons.add(line.trim());
                    line = commonsReader.readLine();
                }
                commonsReader.close();

                BufferedReader dictReader = new BufferedReader(new FileReader(dictFile));
                line = dictReader.readLine();
                Pattern pattern = Pattern.compile("\\d+");
                int added = 0, amb = 0, com = 0;
                while (line != null) {
                    String[] content = line.split("\t");
                    if (content.length >= 2) {
                        Matcher m = pattern.matcher(content[1]);
                        if ((commons.contains(content[1])) || (commons.contains(content[1].toLowerCase())))
                            com++;
                        else if (ambigous.contains(content[1]))
                            amb++;
                        else if (m.matches())
                            com++;
                        else if ((this.dictionary.containsKey(content[1])) &&
                                (!this.dictionary.get(content[1]).equals(content[0]))) {
                            this.dictionary.remove(content[1]);
                            ambigous.add(content[1]);
                            amb += 2;
                            added -= 1;
                        } else {
                            content[1] = content[1].replaceAll("\\.", " \\. ").replaceAll("\\-", " \\- ")
                                    .replaceAll("\\'", " \\' ").replaceAll("\\\"", " \\\" ")
                                    .replaceAll(" +", " ").trim();
                            this.dictionary.put(content[1], content[0]);
                            added++;
                        }
                    }
                    line = dictReader.readLine();
                }
                ConsolePrinter.log("Dictionary chunker compiled.", true);
                ConsolePrinter.log("Added: " + added, true);
                ConsolePrinter.log("Skipped: " + (amb + com) + " of which " + amb + " ambigous, " + com + " common.", true);
                dictReader.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            loadedDicts.put(dictName, this.dictionary);
            loadedCommons.put(dictName, this.commons);
        }
	}
	
	public void addEntry(String name, String channel){
        this.dictionary.put(name, channel);
	}

    public void removeEntry(String name){
        this.dictionary.remove(name);
    }

    public boolean hasName(String name){
        return this.dictionary.containsKey(name);
    }

    public String getChannel(String name){
        return this.dictionary.get(name);
    }

	
    /**
     * Wczytuje chunker z modelu binarnego.
     * @param filename
     */
	@Override
	@SuppressWarnings("unchecked")
    public void deserialize(String filename){
        String dictName = new File(filename).getName();
        if(loadedDicts.containsKey(dictName)){
            this.dictionary = loadedDicts.get(dictName);
            this.commons = loadedCommons.get(dictName);
        }
        else{
            try {
                ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
                this.dictionary = (HashMap<String, String>)in.readObject();
                this.commons = (HashSet<String>)in.readObject();
                in.close();
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            loadedDicts.put(dictName, this.dictionary);
            loadedCommons.put(dictName, this.commons);
        }
    }

	@Override
	public void serialize(String filename) {
		try {
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));
			out.writeObject(this.dictionary);
			out.writeObject(this.commons);
			out.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void close() {
		
	}

	@Override
	public HashMap<Sentence, AnnotationSet> chunk(Document ps) {
		HashMap<Sentence, AnnotationSet> chunkings = new HashMap<Sentence, AnnotationSet>();
		for ( Paragraph paragraph : ps.getParagraphs() )
			for (Sentence sentence : paragraph.getSentences())
				chunkings.put(sentence, this.chunkSentence(sentence));
		return chunkings;
	}
	
}	
