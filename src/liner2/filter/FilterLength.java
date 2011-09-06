package liner2.filter;

import liner2.structure.Chunk;

public class FilterLength extends Filter {

	public FilterLength(){
		this.appliesTo.add("PERSON_FIRST_NAM");
		this.appliesTo.add("PERSON_LAST_NAM");
		this.appliesTo.add("CITY_NAM"); 
		this.appliesTo.add("COUNTRY_NAM");
	}
	
	@Override
	public String getDescription() {
		return "Length greater than 1";
	}

	@Override
	public Chunk pass(Chunk chunk, CharSequence charSeq) {
		if ( chunk.getEnd()-chunk.getBegin()>0 )
			return chunk;
		else
			return null;
	}

}
