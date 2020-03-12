package g419.toolbox.wordnet;

import com.google.common.collect.Maps;
import com.google.common.io.Files;
import g419.toolbox.files.Unzip;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import pl.wroc.pwr.ci.plwordnet.plugins.princetonadapter.da.*;

/**
 * Następca Wordnet i Wordnet2.
 * TODO wymagana integracja Wordnet, Wordnet2 i Wordnet3
 *
 * @author czuk
 */
public class Wordnet3 {
  HashMap<String, HashMap<String, PrincetonDataRaw>> data = Maps.newHashMap();
  HashMap<String, HashMap<String, PrincetonIndexRaw>> index = Maps.newHashMap();

  public final static String REL_HYPERNYM = "@";
  public final static String REL_HYPONYM = "~";
  public final static String REL_MERONYM = "%";
  public final static String REL_HOLONYM = "#";

  String[][] poses = new String[][] {{"adj", "a"}, {"adv", "r"}, {"noun", "n"}, {"verb", "v"}};

  String wordnet_path;

  public Wordnet3() throws IOException {
    final String zipFilename = "plwordnet_2_3_pwn_format.zip";
    final File tmpFolder = Files.createTempDir();
    try {
      final File tmpZipFilename = new File(tmpFolder, zipFilename);
      final InputStream zip = getClass().getResourceAsStream("/plwordnet23pwn.zip");
      FileUtils.copyInputStreamToFile(zip, tmpZipFilename);
      Unzip.unzip(tmpZipFilename.toString(), tmpFolder.toString());
      load(new File(tmpFolder, "plwordnet_2_3_pwn_format").toString());
    } catch (final IOException ex) {
      throw ex;
    } finally {
      FileUtils.deleteDirectory(tmpFolder);
    }
  }

  public Wordnet3(final String path) {
    load(path);
  }

  private void load(final String path) {
    wordnet_path = path;
    try {
      if (!new File(path).exists()) {
        throw new FileNotFoundException("Invalid database directory: " + path);
      }
      for (final String[] pos : poses) {
        final String filename = path + File.separator + "index." + pos[0];
        if ((new File(filename)).exists()) {
          index.put(pos[1], readIndexFile(filename));
        }
      }
      for (final String[] pos : poses) {
        final String filename = path + File.separator + "data." + pos[0];
        if ((new File(filename)).exists()) {
          data.put(pos[1], readDataFile(filename, pos[1]));
        }
      }
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }


  public List<PrincetonDataRaw> getSynsets() {
    return data.values().stream()
        .map(HashMap::values)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

  public HashMap<String, PrincetonIndexRaw> readIndexFile(final String filename) throws IOException {
    final HashMap<String, PrincetonIndexRaw> units = new HashMap<>();

    final BufferedReader r = new BufferedReader(new FileReader(filename));

    String line = null;
    while ((line = r.readLine()) != null) {
      if (line.startsWith("  ")) {
        continue;
      }
      final PrincetonIndexRaw raw = PrincetonParser.parseIndexLine(line);
      units.put(raw.lemma, raw);
    }
    r.close();
    return units;
  }

  public HashMap<String, PrincetonDataRaw> readDataFile(final String filename,
                                                        final String pos) throws IOException {
    final BufferedReader r = new BufferedReader(new FileReader(filename));
    final HashMap<String, PrincetonDataRaw> data = new HashMap<>();

    String line = null;
    while ((line = r.readLine()) != null) {
      // Skip lines with comments. Lines with comments starts with two spaces.
      if (line.startsWith("  ")) {
        continue;
      }
      // Parse line
      final PrincetonDataRaw d = PrincetonParser.parseDataLine(line);
      d.pos = pos;
      data.put(d.offset, d);
    }
    r.close();


    return data;
  }

  public List<String> getLexicalUnits(final PrincetonDataRaw synset) {
    final List<String> lemmas = new ArrayList<>();
    for (final PrincetonDataLemmaRaw lemma : synset.lemmas) {
      lemmas.add(lemma.lemma);
    }
    return lemmas;
  }

  public Set<PrincetonDataRaw> getDirectSynsets(final PrincetonDataRaw synset,
                                                final String relation) {
    final Set<PrincetonDataRaw> synstens = new HashSet<>();
    for (final PrincetonDataRelationRaw rel : synset.relations) {
      if (rel.type.startsWith(relation)) {
        final PrincetonDataRaw direct = data.get(rel.pos).get(rel.offset);
        if (direct != null) {
          synstens.add(direct);
        }
      }
    }
    return synstens;

  }

  public Set<PrincetonDataRaw> getAllSynsets(final PrincetonDataRaw synset,
                                             final String relation) {
    final Set<PrincetonDataRaw> synsets = new HashSet<>();
    getAllSynsets(synset, relation, synsets);
    return synsets;

  }

  public void getAllSynsets(final PrincetonDataRaw synset,
                            final String relation,
                            final Set<PrincetonDataRaw> synsets) {
    for (final PrincetonDataRaw synsetByRel : getDirectSynsets(synset, relation)) {
      if (!synsets.contains(synsetByRel)) {
        synsets.add(synsetByRel);
        getAllSynsets(synsetByRel, relation, synsets);
      }
    }
  }

  public List<PrincetonDataRaw> getSynsets(final String word) {
    final ArrayList<PrincetonDataRaw> synsets = new ArrayList<>();
    for (final Entry<String, HashMap<String, PrincetonIndexRaw>> units : index.entrySet()) {
      if (units.getValue().containsKey(word)) {
        for (final String offset : units.getValue().get(word).synset_offsets) {
          synsets.add(data.get(units.getKey()).get(offset));
        }
      }
    }
    return synsets;
  }

  public List<PrincetonDataRaw> getSynsets(final String word, final int sense) {
    final ArrayList<PrincetonDataRaw> synsets = new ArrayList<>();
    for (final Entry<String, HashMap<String, PrincetonIndexRaw>> units : index.entrySet()) {
      if (units.getValue().containsKey(word)) {
        if (units.getValue().get(word).synset_offsets.size() >= sense) {
          final String offset = units.getValue().get(word).synset_offsets.get(sense - 1);
          synsets.add(data.get(units.getKey()).get(offset));
        }
      }
    }
    return synsets;
  }

  public Set<PrincetonDataRaw> getDirectHypernyms(final PrincetonDataRaw synset) {
    return getDirectSynsets(synset, Wordnet3.REL_HYPERNYM);
  }

  public Set<PrincetonDataRaw> getDirectHyponyms(final PrincetonDataRaw synset) {
    return getDirectSynsets(synset, Wordnet3.REL_HYPONYM);
  }

  public Set<PrincetonDataRaw> getDirectHolonyms(final PrincetonDataRaw synset) {
    return getDirectSynsets(synset, Wordnet3.REL_HOLONYM);
  }

  public Set<PrincetonDataRaw> getDirectMeronyms(final PrincetonDataRaw synset) {
    return getDirectSynsets(synset, Wordnet3.REL_MERONYM);
  }

  public Set<PrincetonDataRaw> getAllHolonyms(final PrincetonDataRaw synset) {
    return getAllSynsets(synset, Wordnet3.REL_HOLONYM);
  }

  /**
   * Zwraca wszystkie (bezpośrednie i pośrednie holonimy danego synsetu).
   * Holonim bezpośredni, to synset połączony relacją holonimii z danym synsetem.
   * Holonim pośredni to:
   * a) holonim holonimu danego synsetu i kolejne poziomy,
   * b) holonim każdego hiperonimu synsetu,
   * c)
   *
   * @param synset
   */
  public void getHolonyms(final PrincetonDataRaw synset,
                          final Set<PrincetonDataRaw> holonyms,
                          final boolean takeHypernyms,
                          final boolean takeHyponyms) {
    final Set<PrincetonDataRaw> directHypernyms = getDirectHypernyms(synset);
    final Set<PrincetonDataRaw> directHolonyms = getDirectHolonyms(synset);

    for (final PrincetonDataRaw synsetLinked : directHolonyms) {
      if (!holonyms.contains(synsetLinked) && synsetLinked.domain.equals(synset.domain)) {
        holonyms.add(synsetLinked);
        //for ( PrincetonDataRaw)
        getHolonyms(synsetLinked, holonyms, true, true);
        if (takeHyponyms) {
          for (final PrincetonDataRaw hyponym : getDirectHyponyms(synsetLinked)) {
            if (!(holonyms.contains(hyponym)) && hyponym.domain.equals(synset.domain)) {
              holonyms.add(hyponym);
              getHolonyms(hyponym, holonyms, false, true);
            }
          }
        }
      }
    }

    if (takeHypernyms) {
      for (final PrincetonDataRaw synsetLinked : directHypernyms) {
        if (!holonyms.contains(synsetLinked) && synsetLinked.domain.equals(synset.domain)) {
          holonyms.add(synsetLinked);
          getHolonyms(synsetLinked, holonyms, true, false);
        }
      }
    }
  }


  public Set<PrincetonDataRaw> getHolonyms(final PrincetonDataRaw synset) {
    final Set<PrincetonDataRaw> holonyms = new HashSet<>();
    getHolonyms(synset, holonyms, true, true);
    return holonyms;
  }


}