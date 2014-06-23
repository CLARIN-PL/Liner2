package g419.corpus.structure;

public class Tag {

	String base = null;
	String ctag = null;
	boolean disamb = false;
	
	public Tag()	{}
	
	public Tag(String base, String ctag, boolean disamb) {
		this.base = base;
		this.ctag = ctag;
		this.disamb = disamb;
	}
	
	public String getBase()		{ return base;	}
	public String getCtag()		{ return ctag;	}
	public boolean getDisamb()	{ return disamb; }

    public void setDisamb(boolean disamb){
        this.disamb = disamb;
    }
}
