package g419.tools.utils;

import java.util.List;

public class GiniImpurity {

	public static double calculate(List<Integer> values){
		double sum = values.stream().mapToInt(Integer::intValue).sum();;
		if ( sum == 0 ){
			return 1;
		}
		double impurity = 1;
		for ( Integer v : values ){
			impurity -= Math.pow((double)v/sum, 2);
		}
		return impurity;
	}
	
}
