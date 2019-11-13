package g419.liner2.core.tools;

import g419.corpus.schema.kpwr.KpwrNer;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Token;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.zip.GZIPInputStream;

/**
 * @author Michał Marcińczuk
 */
public class NeLemmatizer {

  Map<String, String> lemmas = new HashMap<String, String>();

  /**
   * Parts of person names, i.e. first and last names.
   */
  Map<String, String> lemmasPerson = new HashMap<String, String>();

  /**
   * Maps lemmas in singular to plural form.
   */
  Map<String, String> lemmasSingularToPlural = new HashMap<String, String>();

  /**
   * @param nelexicon path to a file with lemmatization dictionary.
   *                  Each line should contain category, form and lemma separated with tabs.
   * @throws IOException
   */
  public NeLemmatizer(String nelexicon, String morfeusz) throws IOException {

    InputStream stream = null;
    BufferedReader reader = null;
    IOException exception = null;
    String line = null;
    try {
      // Wczytaj NELexicon
      if (nelexicon != null) {
        stream = new FileInputStream(nelexicon);
        if (nelexicon.endsWith(".gz")) {
          stream = new GZIPInputStream(stream);
        }
        reader = new BufferedReader(new InputStreamReader(stream));
        while ((line = reader.readLine()) != null) {
          line = line.trim();
          if (!line.startsWith("#") && line.length() > 0) {
            String cols[] = line.split("\t");
            if (cols.length != 3) {
              Logger.getLogger(this.getClass()).error("Incorrect line format: " + line);
            } else {
              this.lemmas.put(cols[1].toLowerCase(), cols[2]);
              this.lemmas.put(cols[2].toLowerCase(), cols[2]);
            }

            if (KpwrNer.NER_LIV_PERSON.equals(cols[0])) {
              String formParts[] = cols[1].split(" ");
              String baseParts[] = cols[2].split(" ");
              if (formParts.length == baseParts.length) {
                for (int i = 0; i < formParts.length; i++) {
                  this.lemmasPerson.put(formParts[i].toLowerCase(), baseParts[i]);
                  this.lemmasPerson.put(baseParts[i].toLowerCase(), baseParts[i]);
                }
              }
              this.lemmasPerson.put(cols[1].toLowerCase(), cols[2]);
              this.lemmasPerson.put(cols[2].toLowerCase(), cols[2]);
            }
          }
        }
      }

      // Wczytaj Morfeusza
      if (morfeusz != null) {
        stream = new FileInputStream(morfeusz);
        if (morfeusz.endsWith(".gz")) {
          stream = new GZIPInputStream(stream);
        }
        reader = new BufferedReader(new InputStreamReader(stream));
        while ((line = reader.readLine()) != null) {
          line = line.trim();
          String cols[] = line.split("\t");
          if (cols.length > 2) {
            String form = cols[0];
            String base = cols[1].split(":")[0];
            this.lemmasSingularToPlural.put(base, form);
          }
        }
      }
    } catch (IOException ex) {
      exception = ex;
    } finally {
      if (reader != null) {
        reader.close();
      } else if (stream != null) {
        stream.close();
      }
    }
    if (exception != null) {
      throw exception;
    }

  }

  /**
   * @param an
   * @return
   */
  public String lemmatize(Annotation an) {
    return this.lemmas.get(an.getText().toLowerCase());
  }

  /**
   * @param an
   * @return
   */
  public String lemmatizePersonName(Annotation an) {
    // W pierwszej kolejności lematyzuj z wykorzystaniem słownika
    String lemma = null;
    Token tokenFirst = an.getSentence().getTokens().get(an.getBegin());
    if (an.getTokens().size() == 1 && tokenFirst.getDisambTag().getCtag().contains(":pl:")) {
      String base = tokenFirst.getDisambTag().getBase();
      lemma = this.lemmasSingularToPlural.get(base);
    }
    if (lemma == null) {
      lemma = this.lemmasPerson.get(an.getText().toLowerCase());
    }
    if (lemma == null) {
      // Jeżeli lematyzacja ze słownikiem nie powiodła się,
      // to sprawdź, czy pierwszy człon nazwy jest w mianowniku.
      // Jeżeli jest, to zwróć całą nazwę jako lemat
      Token token = an.getSentence().getTokens().get(an.getBegin());
      if ("nom".equals(token.getDisambTag().getCase())) {
        lemma = an.getText();
      }
    }
    if (lemma == null && an.getTokens().size() > 1) {
      // Ostatnim krokiem jest lematyzacja każdego członu osobno
      StringJoiner sj = new StringJoiner(" ");
      for (int index : an.getTokens()) {
        Annotation a = new Annotation(index, an.getType(), an.getSentence());
        String l = this.lemmatizePersonName(a);
        if (l == null) {
          l = a.getText();
        }
        sj.add(l);
      }
      lemma = sj.toString();
    }
    return lemma;
  }
}
