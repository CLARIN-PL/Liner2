package g419.liner2.api.normalizer.timex.constraints;

import g419.liner2.api.normalizer.timex.entities.TimexEntity;

public class IsFullySpecified<T extends TimexEntity> implements EntityConstraint<T> {
    @Override
    public boolean isSatisfied(T entity) {
        return entity.isFullySpecified();
    }

    @Override
    public String getDescription() {
        return "Entity should be fully specified";
    }
}
