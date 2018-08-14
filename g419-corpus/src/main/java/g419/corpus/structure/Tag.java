package g419.corpus.structure;

public class Tag {

    String base = null;
    String ctag = null;
    boolean disamb = false;
    String pos = "";
    String cas = "";

    public Tag() {
    }

    public Tag(final String base, final String ctag, final boolean disamb) {
        this.base = base;
        this.ctag = ctag;
        this.disamb = disamb;

        // ToDo: specyficzne dla tagu nkjp
        if (ctag != null) {
            final String[] parts = ctag.split(":");
            pos = parts[0];
            if (parts.length > 2) {
                cas = parts[2];
            }
        }
    }

    public String getBase() {
        return base;
    }

    public String getCtag() {
        return ctag;
    }

    public boolean getDisamb() {
        return disamb;
    }

    public String getPos() {
        return pos;
    }

    public String getCase() {
        return cas;
    }

    public void setDisamb(final boolean disamb) {
        this.disamb = disamb;
    }

    public void setBase(final String base) {
        this.base = base;
    }

    @Override
    public String toString() {
        return "Tag{" +
                "base='" + base + '\'' +
                ", ctag='" + ctag + '\'' +
                ", disamb=" + disamb +
                '}';
    }
}
