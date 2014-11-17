package g419.corpus.structure;

import java.util.ArrayList;

/**
 * Reprezentuje token, z których składa się zdanie (Sentence)
 * @author czuk
 *
 */
public class Token {

    public Token(TokenAttributeIndex attrIdx){
        this.attrIdx = attrIdx;
        packAtributes(attrIdx.getLength());
    }

    public Token(String orth, Tag firstTag, TokenAttributeIndex attrIdx){
        this.attrIdx = attrIdx;
        packAtributes(attrIdx.getLength());
        setAttributeValue(attrIdx.getIndex("orth"), orth);
        addTag(firstTag);
    }
    /* Indeks atrybutów */
    public TokenAttributeIndex attrIdx;

	/* Uporządkowana lista atrybutów */
	ArrayList<String> attributes = new ArrayList<String>();
	
	/* Lista analiz morfologicznych, jeżeli dostępna. */
	ArrayList<Tag> tags = new ArrayList<Tag>();
	
	/* Oznaczenie, czy między bieżącym a następnym tokenem był biały znak. */
	boolean noSpaceAfter = false; 

	private String id = null;
	
	public void clearAttributes() {
		this.attributes = new ArrayList<String>();
	}
	
	public void removeAttribute(int attrIdx){
		this.attributes.remove(attrIdx);
	}

	/**
	 * TODO
	 * Zwraca wartość atrybutu o podany indeksie.
	 * @param index
	 * @return
	 */
	public String getAttributeValue(int index){
		return attributes.get(index);
	}

    public String getAttributeValue(String attr){
        int index = this.attrIdx.getIndex(attr);
        return getAttributeValue(index);
    }
	
	public int getNumAttributes() {
		return attributes.size();
	}

	public String getId(){
		return this.id;
	}
	
	/**
	 * TODO
	 * Funkcja pomocnicza zwraca wartość pierwszego atrybutu.
	 * Przeważnie będzie to orth.
	 * @return
	 */
	public String getOrth(){
		return attributes.get(attrIdx.getIndex("orth"));
	}
	
	public boolean getNoSpaceAfter() {
		return this.noSpaceAfter;
	}
	
	public void addTag(Tag tag) {
        tags.add(tag);
        if (attrIdx.getIndex("base") != -1 && attributes.get(attrIdx.getIndex("base")) == null) {
            this.setAttributeValue(this.attrIdx.getIndex("base"), tag.getBase());
        }
        if (attrIdx.getIndex("ctag") != -1 && attributes.get(attrIdx.getIndex("ctag")) == null) {
            this.setAttributeValue(this.attrIdx.getIndex("ctag"), tag.getCtag());
        }
    }
	
	public ArrayList<Tag> getTags() {
		return tags;
	}
	
	public void packAtributes(int size){
		while(getNumAttributes()<size)
			attributes.add(null);
	}
	
	public void setAttributeValue(int index, String value) {
		if (index < attributes.size())
			attributes.set(index, value);
		else if (index == attributes.size())
			attributes.add(value);
	}

    public void setAttributeValue(String attr, String value) {
        int index = this.attrIdx.getIndex(attr);
        setAttributeValue(index, value);
    }
	
	public void setNoSpaceAfter(boolean noSpaceAfter) {
		this.noSpaceAfter = noSpaceAfter;
	}
	
	public void setId(String id){
		this.id = id;
	}
    
    public String getAttributesAsString(){
    	StringBuilder sb = new StringBuilder();
    	for ( String attr : this.attributes )
    		sb.append((sb.length() == 0 ? "" : ", ") + attr);
    	return sb.toString();
    }

    public Token clone(){
        Token cloned = new Token(this.attrIdx.clone());
        cloned.tags = new ArrayList<Tag>(this.tags);
        cloned.attributes = new ArrayList<String>(this.attributes);
        cloned.id = id;
        cloned.noSpaceAfter = this.noSpaceAfter;
        return cloned;
    }

    public void setAttributeIndex(TokenAttributeIndex newAttrIdx){
        ArrayList<String> newAttributes = new ArrayList<String>();
        for(String feature: newAttrIdx.allAtributes()){
            String value = attrIdx.getIndex(feature) == -1 ? null : getAttributeValue(feature);
            newAttributes.add(value);
        }
        attrIdx = newAttrIdx;
        attributes = newAttributes;
    }
}
