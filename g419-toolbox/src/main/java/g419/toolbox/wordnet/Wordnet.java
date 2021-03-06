package g419.toolbox.wordnet;

import pl.wroc.pwr.ci.plwordnet.plugins.princetonadapter.da.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

public class Wordnet {


  HashMap<String, HashMap<String, PrincetonDataRaw>> data = new HashMap<String, HashMap<String, PrincetonDataRaw>>();
  HashMap<String, HashMap<String, PrincetonIndexRaw>> index = new HashMap<String, HashMap<String, PrincetonIndexRaw>>();

  String[][] poses = new String[][] {{"adj", "a"}, {"adv", "r"}, {"noun", "n"}, {"verb", "v"}};

  String wordnet_path;


  private static class WordnetHolder {
    private static HashMap<String, Wordnet> wordnets = new HashMap<>();

    private static synchronized void createNewWordnet(String path) {
      if (!wordnets.containsKey(path)) {
        wordnets.put(path, new Wordnet(path));
      }
    }

    public static Wordnet getWordnet(String path) {
      if (!wordnets.containsKey(path)) {
        createNewWordnet(path);
      }
      return wordnets.get(path);
    }

  }

  public static synchronized Wordnet getWordnet(String path) {
    return WordnetHolder.getWordnet(path);
  }

  /**
   * Tworzy obiekt na podstawie plików w formacie Princeton.
   *
   * @param path Ścieżka do katalogu z plikami w formacie Princeton.
   */
  private Wordnet(String path) {
    wordnet_path = path;
    try {
      if (!new File(path).exists()) {
        throw new FileNotFoundException("Invalid database directory: " + path);
      }
      for (String pos[] : poses) {
        String filename = path + File.separator + "index." + pos[0];
        if ((new File(filename)).exists()) {
          index.put(pos[1], readIndexFile(filename));
        }
      }
      for (String pos[] : poses) {
        String filename = path + File.separator + "data." + pos[0];
        if ((new File(filename)).exists()) {
          data.put(pos[1], readDataFile(filename, pos[1]));
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public HashMap<String, PrincetonIndexRaw> readIndexFile(String filename) throws IOException {
    HashMap<String, PrincetonIndexRaw> units = new HashMap<String, PrincetonIndexRaw>();

    BufferedReader r = new BufferedReader(new FileReader(filename));

    String line = null;
    while ((line = r.readLine()) != null) {
      // Skip lines with comments. Lines with comments starts with two spaces.
      if (line.startsWith("  ")) {
        continue;
      }
      // Parse line
      PrincetonIndexRaw raw = PrincetonParser.parseIndexLine(line);
      units.put(raw.lemma, raw);
    }
    r.close();
    return units;
  }

  public HashMap<String, PrincetonDataRaw> readDataFile(String filename, String pos) throws IOException {
    BufferedReader r = new BufferedReader(new FileReader(filename));
    HashMap<String, PrincetonDataRaw> data = new HashMap<String, PrincetonDataRaw>();

    String line = null;
    while ((line = r.readLine()) != null) {
      // Skip lines with comments. Lines with comments starts with two spaces.
      if (line.startsWith("  ")) {
        continue;
      }
      // Parse line
      PrincetonDataRaw d = PrincetonParser.parseDataLine(line);
      d.pos = pos;
      data.put(d.offset, d);
    }
    r.close();


    return data;
  }

  public ArrayList<PrincetonDataRaw> getSynsets(String word) {
    ArrayList<PrincetonDataRaw> synsets = new ArrayList<PrincetonDataRaw>();
    for (Entry<String, HashMap<String, PrincetonIndexRaw>> units : index.entrySet()) {
      if (units.getValue().containsKey(word)) {
        for (String offset : units.getValue().get(word).synset_offsets) {
          synsets.add(data.get(units.getKey()).get(offset));
        }
      }
    }
    return synsets;
  }

  /**
   * Zwraca synsety zawierające jednostkę leksykalną o lemacie word i sensie sense.
   *
   * @param word
   * @param sense
   * @return
   */
  public ArrayList<PrincetonDataRaw> getSynsets(String word, int sense) {
    ArrayList<PrincetonDataRaw> synsets = new ArrayList<PrincetonDataRaw>();
    for (Entry<String, HashMap<String, PrincetonIndexRaw>> units : index.entrySet()) {
      if (units.getValue().containsKey(word)) {
        if (units.getValue().get(word).synset_offsets.size() >= sense) {
          String offset = units.getValue().get(word).synset_offsets.get(sense - 1);
          synsets.add(data.get(units.getKey()).get(offset));
        }
      }
    }
    return synsets;
  }

  /**
   * @param word
   * @param sense
   * @return
   */
  public Set<String> getHyponymWords(String word, int sense) {
    Set<PrincetonDataRaw> visited = new HashSet<PrincetonDataRaw>();
    Set<String> words = new HashSet<String>();
    for (PrincetonDataRaw synset : this.getSynsets(word, sense)) {
      this.getHyponymWordsRecursive(synset, visited, words);
    }
    return words;
  }

  /**
   * Rekurencyjna funkcja tworząca listę słów będących należących do danego synsetu i wszystkich synsetów
   * będących hiponimami.
   *
   * @param synset
   * @param visited
   * @param words
   */
  private void getHyponymWordsRecursive(PrincetonDataRaw synset, Set<PrincetonDataRaw> visited, Set<String> words) {
    if (!visited.contains(synset)) {
      visited.add(synset);
      // Dodaj słowa z synsetu
      for (PrincetonDataLemmaRaw lemma : synset.lemmas) {
        words.add(lemma.lemma);
      }
      // Wywołanie rekurencyjne dla kolejnych poziomów zagnieżdżenia
      for (PrincetonDataRelationRaw rel : synset.relations) {
        if (rel.type.equals("~")) {
          this.getHyponymWordsRecursive(this.data.get(rel.pos).get(rel.offset), visited, words);
        }
      }
    }
  }


  public ArrayList<PrincetonDataRaw> getHypernyms(PrincetonDataRaw synset) {
    ArrayList<PrincetonDataRaw> hypernyms = new ArrayList<PrincetonDataRaw>();
    for (PrincetonDataRelationRaw rel : synset.relations) {
      if (rel.type.equals("@")) {
        hypernyms.add(data.get(rel.pos).get(rel.offset));
      }
    }
    return hypernyms;

  }

}