package g419.liner2.api.normalizer.timex.constraints;

import g419.liner2.api.normalizer.timex.entities.TimexEntity;

public class IsOfClass<T extends TimexEntity> implements EntityConstraint<T>{
    final protected Class<? extends T> wantedClass;

    public IsOfClass(Class<? extends T> wantedClass) {
        this.wantedClass = wantedClass;
    }

    @Override
    public boolean isSatisfied(T entity) {
        return wantedClass.isInstance(entity);
    }

    @Override
    public String getDescription() {
        return "Entity should be of class "+wantedClass.getSimpleName();
    }
}
