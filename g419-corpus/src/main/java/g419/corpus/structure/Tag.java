package g419.corpus.structure;

public class Tag {

	String base = null;
	String ctag = null;
	boolean disamb = false;
	String pos = "";
	
	public Tag()	{}
	
	public Tag(String base, String ctag, boolean disamb) {
		this.base = base;
		this.ctag = ctag;
		this.disamb = disamb;
		
		// ToDo: specyficzne dla tagu nkjp
		String[] parts = ctag.split(":");
		this.pos = parts[0];
	}
	
	public String getBase()	{ 
		return this.base;	
	}
	
	public String getCtag()	{ 
		return this.ctag;	
	}
	
	public boolean getDisamb()	{ 
		return this.disamb; 
	}
	
	public String getPos(){ 
		return this.pos; 
	}

    public void setDisamb(boolean disamb){
        this.disamb = disamb;
    }

    // TODO
//    public void SetBase(String base){
//    	ti
//    }
}
