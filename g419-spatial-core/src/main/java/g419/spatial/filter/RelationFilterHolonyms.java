package g419.spatial.filter;

import g419.spatial.structure.SpatialExpression;
import g419.toolbox.wordnet.NamToWordnet;
import g419.toolbox.wordnet.Wordnet3;
import pl.wroc.pwr.ci.plwordnet.plugins.princetonadapter.da.PrincetonDataRaw;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Filtr sprawdza, czy przyimek występuje przed potencjalnych landmarkiem.
 * Celem filtru jest odrzucenie tych kandydatów, wygenerowanych głównie przez MaltParser,
 * dla których przyimek wystąpuje po landmarku.
 *
 * @author czuk
 */
public class RelationFilterHolonyms implements IRelationFilter {

  private Wordnet3 wordnet = null;
  private NamToWordnet nam2wordnet = null;


  public RelationFilterHolonyms(Wordnet3 wordnet, NamToWordnet nam2wordnet) throws IOException {
    this.wordnet = wordnet;
    this.nam2wordnet = nam2wordnet;
  }

  @Override
  public boolean pass(SpatialExpression relation) {

    /* Utwórz listę holonimów landmarka */
    Set<String> holonyms = new HashSet<String>();
    // Holonimy z dla kategorii jednostki
    for (PrincetonDataRaw synset : this.nam2wordnet.getSynsets(relation.getLandmark().getSpatialObject().getType())) {
      for (PrincetonDataRaw holonym : this.wordnet.getHolonyms(synset)) {
        holonyms.addAll(this.wordnet.getLexicalUnits(holonym));
      }
    }
    // Holonimy dla głowy, jeżeli nie ma kategorii jednostki
    if (holonyms.size() == 0) {
      for (PrincetonDataRaw synset : this.wordnet.getSynsets(relation.getLandmark().getSpatialObject().getHeadToken().getDisambTag().getBase())) {
        for (PrincetonDataRaw holonym : this.wordnet.getHolonyms(synset)) {
          holonyms.addAll(this.wordnet.getLexicalUnits(holonym));
        }
      }
    }
    holonyms.removeAll(this.nam2wordnet.getSynsets(relation.getLandmark().getSpatialObject().getType()));
    holonyms.removeAll(this.wordnet.getSynsets(relation.getLandmark().getSpatialObject().getHeadToken().getDisambTag().getBase()));

    // Jednostki trajectora
    Set<PrincetonDataRaw> synsets = this.nam2wordnet.getSynsets(
        relation.getTrajector().getSpatialObject().getType());

    //System.out.println(synsets);
    //System.out.println(holonyms);

    if (synsets.size() > 0) {
      Set<String> trajectors = new HashSet<String>();
      for (PrincetonDataRaw synset : synsets) {
        for (PrincetonDataRaw holonym : this.wordnet.getHolonyms(synset)) {
          trajectors.addAll(this.wordnet.getLexicalUnits(holonym));
        }
      }
      for (String word : trajectors) {
        if (holonyms.contains(word)) {
          //System.out.println("Contain: " + word);
          return false;
        }
      }
      return true;
    } else {
      return !holonyms.contains(
          relation.getTrajector().getSpatialObject().getHeadToken().getDisambTag().getBase());
    }
  }

}
