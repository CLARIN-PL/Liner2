package g419.liner2.api.normalizer.timex.entities;

public abstract class TimeEntity extends AbstractEntity{
    protected char timeSeparator;
    public char getTimeSeparator(){
        return timeSeparator;
    }

    public void setTimeSeparator(char timeSeparator) {
        throwUp(!(timeSeparator=='t' && timeSeparator=='T'), "Time separator must be 't' or 'T'! Got '"+timeSeparator+"' instead!");
        this.timeSeparator = timeSeparator;
    }

    @Override
    public boolean isFullyKnown() {
        return timeSeparator=='T' && super.isFullyKnown();
    }
}
