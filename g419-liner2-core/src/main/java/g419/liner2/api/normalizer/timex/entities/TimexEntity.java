package g419.liner2.api.normalizer.timex.entities;

import g419.liner2.api.normalizer.timex.constraints.EntityConstraint;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public interface TimexEntity extends Serializable {
    /**
     * Turn this into LVAL representation.
     * @return LVAL
     */
    String toTimex();

    /**
     * Fill all "x" in this date with non-"x" from another, more general date.
     * Filling should occur in-place.
     * @param general More general date
     * @return Copy of this date with fields filled
     */
    TimexEntity fill(TimexEntity general);

    /**
     * Are there any "x" in this date?
     * @return
     */
    boolean isFullySpecified();

    /**
     * Can it be considered as fully known? Not fully known case happens with "t"/"T", "MO"/"EV/...
     * Fully known date is always fully specified.
     * @return
     */
    boolean isFullyKnown();

    TimexEntity copy();

    /**
     * Should throw some exception if any of given constraints is unsatisfied with this entity.
     * @param constraints Collection of constraint instances
     */
    void assertConstraintsSatisfied(Collection<? extends EntityConstraint> constraints);
}
