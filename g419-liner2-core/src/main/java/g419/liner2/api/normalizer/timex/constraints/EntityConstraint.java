package g419.liner2.api.normalizer.timex.constraints;

import g419.liner2.api.normalizer.timex.entities.TimexEntity;

public interface EntityConstraint<T extends TimexEntity> {
    boolean isSatisfied(T entity);
    String getDescription();
}
