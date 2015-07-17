package g419.liner2.api.normalizer.timex.entities;

public class LValEntity extends AbstractEntity{

    protected DateTimeEntity dateTimeEntity;
    protected String unparsed;

    public LValEntity() {
    }

    public LValEntity(DateTimeEntity dateTimeEntity, String unparsed) {
        this.dateTimeEntity = dateTimeEntity;
        this.unparsed = unparsed;
    }

    @Override
    boolean isSpecified() {
        return dateTimeEntity.isFullySpecified() && !unparsed.contains("x");
    }

    @Override
    boolean isKnown() {
        return dateTimeEntity.isFullyKnown() && !(unparsed.contains("x") || unparsed.contains("t"));
    }

    @Override
    public String toTimex() {
        return dateTimeEntity.toTimex()+unparsed;
    }

    @Override
    public TimexEntity fill(TimexEntity general) {
        return dateTimeEntity.fill(((LValEntity) general).dateTimeEntity);
    }

    public DateTimeEntity getDateTimeEntity() {
        return dateTimeEntity;
    }

    public void setDateTimeEntity(DateTimeEntity dateTimeEntity) {
        this.dateTimeEntity = dateTimeEntity;
    }

    public String getUnparsed() {
        return unparsed;
    }

    public void setUnparsed(String unparsed) {
        this.unparsed = unparsed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LValEntity that = (LValEntity) o;

        if (dateTimeEntity != null ? !dateTimeEntity.equals(that.dateTimeEntity) : that.dateTimeEntity != null)
            return false;
        if (unparsed != null ? !unparsed.equals(that.unparsed) : that.unparsed != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = dateTimeEntity != null ? dateTimeEntity.hashCode() : 0;
        result = 31 * result + (unparsed != null ? unparsed.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "LValEntity{" +
                "dateTimeEntity=" + dateTimeEntity +
                ", unparsed='" + unparsed + '\'' +
                '}';
    }

    @Override
    public TimexEntity copy() {
        return new LValEntity(dateTimeEntity==null ? null : (DateTimeEntity) dateTimeEntity.copy(), unparsed);
    }
}
