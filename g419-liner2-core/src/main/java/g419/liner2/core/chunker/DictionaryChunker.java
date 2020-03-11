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

  static HashMap<String, HashMap<String, String>> loadedDicts = new HashMap<>();
  static HashMap<String, HashSet<String>> loadedCommons = new HashMap<>();

  private HashMap<String, String> dictionary = null;
  private HashSet<String> commons = null;
  private ArrayList<String> types = null;

  public DictionaryChunker(final ArrayList<String> types) {
    this.types = types;
    dictionary = new HashMap<>();
    commons = new HashSet<>();
  }

  private AnnotationSet chunkSentence(final Sentence sentence) {
    final AnnotationSet chunking = new AnnotationSet(sentence);
    final List<Token> tokens = sentence.getTokens();
    final int sentenceLength = sentence.getTokenNumber();
    if (sentenceLength > 500) {
      return chunking;
    }
    // [długość] -> początek => n-gram
    final ArrayList<HashMap<Integer, String>> nGrams =
        new ArrayList<>();

    // wygeneruj unigramy
    nGrams.add(new HashMap<>());
    for (int i = 0; i < sentenceLength; i++) {
      nGrams.get(0).put(i, tokens.get(i).getOrth());
    }
    // wygeneruj n-gramy
    for (int n = 1; n < sentenceLength; n++) {
      nGrams.add(new HashMap<>());
      for (int j = 0; j < sentenceLength - n; j++) {
        nGrams.get(n).put(j,
            nGrams.get(n - 1).get(j) + " " + tokens.get(j + n).getOrth());
      }
    }

    // chunkuj (poczynając od najdłuższych n-gramów)
    for (int n = sentenceLength - 1; n >= 0; n--) {
      for (int i = 0; i < sentenceLength - n; i++) {
        final int idx = i;

        // jeśli danego n-gramu nie ma w tablicy, to kontynuuj
        if (nGrams.get(n).get(idx) == null) {
          continue;
        }

        // jeśli znaleziono w słowniku
        if (dictionary.containsKey(nGrams.get(n).get(idx))) {

          // odrzuć, jeśli base jest nazwą pospolitą
          if ((n == 0) && (commons.contains(sentence.getAttributeIndex()
              .getAttributeValue(tokens.get(i), "base")))) {
            continue;
          }

          final String chunkType = dictionary.get(nGrams.get(n).get(idx));

          // odrzuć, jeśli ten typ nie ma być brany pod uwagę
          if ((types != null) && (!types.contains(chunkType))) {
            continue;
          }

          // dodaj chunk
          chunking.addChunk(new Annotation(i, i + n,
              dictionary.get(nGrams.get(n).get(idx)), sentence));

          // usuń z tablicy wszystkie krótsze n-gramy, które zahaczają o to miejsce
          // j - dł. odrzucanego n-gramu - 1, k - pozycja startowa
          for (int j = n; j >= 0; j--) {
            for (int k = i - j; k <= i + n; k++) {
              nGrams.get(j).remove(k);
            }
          }
        }
      }
    }

    return chunking;
  }

  public void loadDictionary(final String dictFile, final String commonsFile) {
    final String dictName = new File(dictFile).getName();
    if (loadedDicts.containsKey(dictName)) {
      dictionary = loadedDicts.get(dictName);
      commons = loadedCommons.get(dictName);
    } else {
      try {
        final BufferedReader commonsReader = new BufferedReader(new FileReader(commonsFile));
        final HashSet<String> ambigous = new HashSet<>();

        String line = commonsReader.readLine();
        while (line != null) {
          commons.add(line.trim());
          line = commonsReader.readLine();
        }
        commonsReader.close();

        final BufferedReader dictReader = new BufferedReader(new FileReader(dictFile));
        line = dictReader.readLine();
        final Pattern pattern = Pattern.compile("\\d+");
        int added = 0, amb = 0, com = 0;
        while (line != null) {
          final String[] content = line.split("\t");
          if (content.length >= 2) {
            final Matcher m = pattern.matcher(content[1]);
            if ((commons.contains(content[1])) || (commons.contains(content[1].toLowerCase()))) {
              com++;
            } else if (ambigous.contains(content[1])) {
              amb++;
            } else if (m.matches()) {
              com++;
            } else if ((dictionary.containsKey(content[1])) &&
                (!dictionary.get(content[1]).equals(content[0]))) {
              dictionary.remove(content[1]);
              ambigous.add(content[1]);
              amb += 2;
              added -= 1;
            } else {
              content[1] = content[1].replaceAll("\\.", " \\. ").replaceAll("\\-", " \\- ")
                  .replaceAll("\\'", " \\' ").replaceAll("\\\"", " \\\" ")
                  .replaceAll(" +", " ").trim();
              dictionary.put(content[1], content[0]);
              added++;
            }
          }
          line = dictReader.readLine();
        }
        ConsolePrinter.log("Dictionary chunker compiled.", true);
        ConsolePrinter.log("Added: " + added, true);
        ConsolePrinter.log("Skipped: " + (amb + com) + " of which " + amb + " ambigous, " + com + " common.", true);
        dictReader.close();
      } catch (final IOException ex) {
        ex.printStackTrace();
      }
      loadedDicts.put(dictName, dictionary);
      loadedCommons.put(dictName, commons);
    }
  }

  public void addEntry(final String name, final String channel) {
    dictionary.put(name, channel);
  }

  public void removeEntry(final String name) {
    dictionary.remove(name);
  }

  public boolean hasName(final String name) {
    return dictionary.containsKey(name);
  }

  public String getChannel(final String name) {
    return dictionary.get(name);
  }


  /**
   * Wczytuje chunker z modelu binarnego.
   *
   * @param filename
   */
  @Override
  @SuppressWarnings("unchecked")
  public void deserialize(final String filename) {
    final String dictName = new File(filename).getName();
    if (loadedDicts.containsKey(dictName)) {
      dictionary = loadedDicts.get(dictName);
      commons = loadedCommons.get(dictName);
    } else {
      try {
        final ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
        dictionary = (HashMap<String, String>) in.readObject();
        commons = (HashSet<String>) in.readObject();
        in.close();
      } catch (final ClassNotFoundException ex) {
        ex.printStackTrace();
      } catch (final IOException ex) {
        ex.printStackTrace();
      }
      loadedDicts.put(dictName, dictionary);
      loadedCommons.put(dictName, commons);
    }
  }

  @Override
  public void serialize(final String filename) {
    try {
      final ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));
      out.writeObject(dictionary);
      out.writeObject(commons);
      out.close();
    } catch (final IOException ex) {
      ex.printStackTrace();
    }
  }

  @Override
  public void close() {

  }

  @Override
  public HashMap<Sentence, AnnotationSet> chunk(final Document ps) {
    final HashMap<Sentence, AnnotationSet> chunkings = new HashMap<>();
    for (final Paragraph paragraph : ps.getParagraphs()) {
      for (final Sentence sentence : paragraph.getSentences()) {
        chunkings.put(sentence, chunkSentence(sentence));
      }
    }
    return chunkings;
  }

}	
