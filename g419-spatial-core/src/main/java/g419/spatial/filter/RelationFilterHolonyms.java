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


  public RelationFilterHolonyms(final Wordnet3 wordnet, final NamToWordnet nam2wordnet) throws IOException {
    this.wordnet = wordnet;
    this.nam2wordnet = nam2wordnet;
  }

  @Override
  public boolean pass(final SpatialExpression relation) {

    /* Utwórz listę holonimów landmarka */
    final Set<String> holonyms = new HashSet<>();
    // Holonimy z dla kategorii jednostki
    for (final PrincetonDataRaw synset : nam2wordnet.getSynsets(relation.getLandmark().getSpatialObject().getType())) {
      for (final PrincetonDataRaw holonym : wordnet.getHolonyms(synset)) {
        holonyms.addAll(wordnet.getLexicalUnits(holonym));
      }
    }
    // Holonimy dla głowy, jeżeli nie ma kategorii jednostki
    if (holonyms.size() == 0) {
      for (final PrincetonDataRaw synset : wordnet.getSynsets(relation.getLandmark().getSpatialObject().getHeadToken().getDisambTag().getBase())) {
        for (final PrincetonDataRaw holonym : wordnet.getHolonyms(synset)) {
          holonyms.addAll(wordnet.getLexicalUnits(holonym));
        }
      }
    }
    holonyms.removeAll(nam2wordnet.getSynsets(relation.getLandmark().getSpatialObject().getType()));
    holonyms.removeAll(wordnet.getSynsets(relation.getLandmark().getSpatialObject().getHeadToken().getDisambTag().getBase()));

    // Jednostki trajectora
    final Set<PrincetonDataRaw> synsets = nam2wordnet.getSynsets(
        relation.getTrajector().getSpatialObject().getType());

    //System.out.println(synsets);
    //System.out.println(holonyms);

    if (synsets.size() > 0) {
      final Set<String> trajectors = new HashSet<>();
      for (final PrincetonDataRaw synset : synsets) {
        for (final PrincetonDataRaw holonym : wordnet.getHolonyms(synset)) {
          trajectors.addAll(wordnet.getLexicalUnits(holonym));
        }
      }
      for (final String word : trajectors) {
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
