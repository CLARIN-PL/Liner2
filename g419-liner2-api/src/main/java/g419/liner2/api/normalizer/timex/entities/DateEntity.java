package g419.liner2.api.normalizer.timex.entities;

public abstract class DateEntity extends AbstractEntity{
    protected String year;

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    @Override
    public boolean isFullySpecified() {
        return year!=null && super.isFullySpecified();
    }


}
