package g419.spatial.filter;

import g419.spatial.structure.SpatialRelation;

public interface IRelationFilter {

	/**
	 * Sprawdza, czy obiekt relation przechodzi przez filtr. Jeżeli spełniony jest warunek filtru,
	 * to zwracana jest wartość true, wpp. false. 
	 * @param relation Obiekt reprezentujący relację.
	 * @return true, jeżeli zaszedł warunek filtru.
	 */
	public boolean pass(SpatialRelation relation);
	
}
