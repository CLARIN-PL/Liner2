package g419.spatial.structure;

import java.util.Set;

/**
 * Klasa reprezentuje schemat zaimkowego wyrażenia przestrzennego.
 * Schemat definiuje ograniczenia semantyczne dla trajestora i landmarka w zależności
 * od przyimka przestrzennego.
 *
 * @author czuk
 */
public class SpatialRelationSchema {

    String name;
    String cas;
    Set<String> indicators;
    Set<String> landmarkConcepts;
    Set<String> trajectorConcepts;

    public SpatialRelationSchema(final String name, final String cas, final Set<String> indicators, final Set<String> trajectorConcepts, final Set<String> landmarkConcepts) {
        this.name = name;
        this.cas = cas;
        this.indicators = indicators;
        this.landmarkConcepts = landmarkConcepts;
        this.trajectorConcepts = trajectorConcepts;
    }

    public String getName() {
        return this.name;
    }

    public String getCase() {
        return this.cas;
    }

    public Set<String> getIndicators() {
        return indicators;
    }

    public void setIndicator(final Set<String> indicators) {
        this.indicators = indicators;
    }

    public Set<String> getLandmarkConcepts() {
        return landmarkConcepts;
    }

    public void setLandmarkConcepts(final Set<String> landmarkConcepts) {
        this.landmarkConcepts = landmarkConcepts;
    }

    public Set<String> getTrajectorConcepts() {
        return trajectorConcepts;
    }

    public void setTrajectorConcepts(final Set<String> trajectorConcepts) {
        this.trajectorConcepts = trajectorConcepts;
    }

    @Override
    public String toString() {
        return String.format("[%s|%s|%s]",
                String.join(",", this.indicators),
                String.join(",", this.trajectorConcepts),
                String.join(",", this.landmarkConcepts));
    }

}
