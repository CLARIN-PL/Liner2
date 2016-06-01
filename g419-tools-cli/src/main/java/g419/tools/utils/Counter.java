package g419.tools.utils;

/**
 * Klasa reprezentuje licznik wartości. 
 * 
 * @author czuk
 *
 */
public class Counter{
	
	private Integer counter = 0;
	
	public void increment(){
		this.counter++;
	}
	
	public Integer getValue(){
		return this.counter;
	}
	
}
