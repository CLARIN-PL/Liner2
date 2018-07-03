package g419.spatial.filter;

import g419.corpus.schema.kpwr.KpwrWsd;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Tag;
import g419.spatial.io.CsvSpatialSchemeParser;
import g419.spatial.structure.SpatialExpression;
import g419.spatial.structure.SpatialRelationSchema;
import g419.spatial.structure.SpatialRelationSchemaMatcher;
import g419.toolbox.sumo.NamToSumo;
import g419.toolbox.sumo.Sumo;
import g419.toolbox.sumo.WordnetToSumo;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.zip.DataFormatException;

public class RelationFilterSemanticPattern implements IRelationFilter {

    WordnetToSumo wts = null;
    Sumo sumo = new Sumo(false);
    SpatialRelationSchemaMatcher patternMatcher = null;
    NamToSumo namToSumo = new NamToSumo();

    public RelationFilterSemanticPattern() throws IOException {
        try {
            this.wts = new WordnetToSumo();
            this.patternMatcher = this.getPatternMatcher();
        } catch (final DataFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * @return
     * @throws IOException
     */
    private SpatialRelationSchemaMatcher getPatternMatcher() throws IOException {
        final String location = "/g419/spatial/resources/spatial_schemes.csv";
        final boolean general = true;

        try (final InputStream resource = this.getClass().getResourceAsStream(location)) {
            if (resource == null) {
                throw new MissingResourceException("Resource not found: " + location, this.getClass().getName(), location);
            } else {
                return (new CsvSpatialSchemeParser(new InputStreamReader(resource), new Sumo(false), general)).parse();
            }
        }
    }

    @Override
    public boolean pass(final SpatialExpression relation) {
        return match(relation).size() > 0;
    }

    /**
     * @param relation
     * @return
     */
    public List<SpatialRelationSchema> match(final SpatialExpression relation) {
        relation.getLandmarkConcepts().addAll(this.getAnnotationConcepts(relation.getLandmark().getSpatialObject()));
        relation.getTrajectorConcepts().addAll(this.getAnnotationConcepts(relation.getTrajector().getSpatialObject()));

        final List<SpatialRelationSchema> matching = this.patternMatcher.matchAll(relation);
        relation.getSchemas().addAll(matching);

        return matching;
    }

    /**
     * Zwraca zbiór pojęć SUMO dla wskazanej anotacji.
     *
     * @param an
     * @return
     */
    public Set<String> getAnnotationConcepts(final Annotation an) {
        final Set<String> allConcepts = new HashSet<>();
        final String synsetId = an.getHeadToken().getProps().get(KpwrWsd.TOKEN_PROP_SYNSET_ID);
        if (synsetId != null) {
            final Set<String> wsdConcepts = this.wts.getSynsetConcepts(synsetId);
            if (wsdConcepts != null) {
                allConcepts.addAll(wsdConcepts);
            }
        } else {
            /* Pojęcia SUMO po lematach głowy anotacji */
            /* ... wszystkie interpretacje */
//			for ( Tag tag : an.getHeadToken().getTags() ){
//				Set<String> lemmaConcepts = this.wts.getLemmaConcepts(tag.getBase());
//				if ( lemmaConcepts != null ){
//					allConcepts.addAll( lemmaConcepts );
//				}
//			}
            /* ... tylko tagi oznaczone jako disamb */
            for (final Tag tag : an.getHeadToken().getDisambTags()) {
                final Set<String> lemmaConcepts = this.wts.getLemmaConcepts(tag.getBase());
                if (lemmaConcepts != null) {
                    allConcepts.addAll(lemmaConcepts);
                }
            }
        }

        /* Pojęcia SUMO po kategorii anotacji */
        final Set<String> typeConcepts = this.namToSumo.getConcept(an.getType());
        if (typeConcepts != null) {
            allConcepts.addAll(typeConcepts);
        }

        return allConcepts;
    }

    /**
     * @return
     */
    public Sumo getSumo() {
        return this.sumo;
    }
}
