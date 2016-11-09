package g419.liner2.api.normalizer.timex.entities;

public class DateTimeEntity extends AbstractEntity{
    protected DateEntity dateEntity;
    protected TimeEntity timeEntity;

    public DateTimeEntity() {
    }

    public DateTimeEntity(DateEntity dateEntity, TimeEntity timeEntity) {
        this.dateEntity = dateEntity;
        this.timeEntity = timeEntity;
    }

    @Override
    public String toTimex() {
        String out = "";
        if (dateEntity!=null){
            out += dateEntity.toTimex();
        }
        if (timeEntity!=null)
            out += timeEntity.toTimex();
        return out;
    }

    public DateTimeEntity fill(DateTimeEntity general) {
        return new DateTimeEntity(
                dateEntity == null ? null : (GeneralDateEntity) dateEntity.fill(general.dateEntity),
                timeEntity == null ? null : (TimeEntity) timeEntity.fill(general.timeEntity)
        );
    }

    @Override
    public TimexEntity fill(TimexEntity general) {
        return fill((DateTimeEntity) general);
    }

    @Override
    public boolean isFullySpecified() {
        return dateEntity != null && timeEntity != null && dateEntity.isFullySpecified() && timeEntity.isFullySpecified();
    }

    @Override
    public boolean isFullyKnown() {
        return dateEntity != null && timeEntity != null && dateEntity.isFullyKnown() && timeEntity.isFullyKnown();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DateTimeEntity that = (DateTimeEntity) o;

        if (dateEntity != null ? !dateEntity.equals(that.dateEntity) : that.dateEntity != null) return false;
        if (timeEntity != null ? !timeEntity.equals(that.timeEntity) : that.timeEntity != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = dateEntity != null ? dateEntity.hashCode() : 0;
        result = 31 * result + (timeEntity != null ? timeEntity.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DateTimeEntity{" +
                "dateEntity=" + dateEntity +
                ", timeEntity=" + timeEntity +
                '}';
    }

    @Override
    public TimexEntity copy() {
        return new DateTimeEntity((DateEntity) dateEntity.copy(), (TimeEntity) timeEntity.copy());
    }
}
