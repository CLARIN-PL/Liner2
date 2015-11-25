package g419.tools.utils;

import java.util.Set;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Klasa reprezentuje żadką macierz do zliczania częstości cech na przecięciu wieresza i kolumny.
 * @author czuk
 *
 */
public class SparseMatrixCounter {

	Set<String> rowValues = new TreeSet<String>();
	Set<String> columnValues = new TreeSet<String>();
	
	// Wiersz => Kolumna => Counter
	Map<String, Map<String, Counter>> counters = new HashMap<String, Map<String, Counter>>();
	
	
	
	public Set<String> getRows(){
		return this.rowValues;
	}
	
	public Set<String> getColumns(){
		return this.columnValues;
	}
	
	public int sumColumn(String column){
		int sum = 0;
		for ( Map<String, Counter> rowValues : counters.values() ){
			Counter c = rowValues.get(column); 
			if ( c != null ){
				sum += c.getValue();
			}
		}
		return sum;
	}
	
	public void removeColumn(String column){
		this.columnValues.remove(column);
		for ( Map<String, Counter> rowValues : counters.values() ){
			rowValues.remove(column);
		}
	}
	

	public void removeColumns(Collection<String> columns) {
		this.columnValues.removeAll(columns);
		for ( Map<String, Counter> rowValues : counters.values() ){
			for ( String column : columns){
				rowValues.remove(column);
			}
		}
	}	
	
	public List<Integer> getRowValues(String row){
		List<Integer> values = new ArrayList<Integer>();
		Map<String, Counter> rowCounters = this.counters.get(row);
		if (rowCounters != null ){
			for (String column : this.columnValues ){
				Counter counter = rowCounters.get(column);
				values.add(counter == null ? 0 : counter.getValue());
			}
		}
		return values;
	}
	
	public void addItem(String row, String column){
		this.rowValues.add(row);
		this.columnValues.add(column);
		Map<String, Counter> rowCounters = this.counters.get(row);
		if ( rowCounters == null ){
			rowCounters = new HashMap<String, Counter>();
			this.counters.put(row, rowCounters);
		}
		Counter counter = rowCounters.get(column);
		if ( counter == null ){
			counter = new Counter();
			rowCounters.put(column, counter);
		}
		counter.increment();
	}
	
	public Counter getCounter(String row, String column){
		Map<String, Counter> rowCounters = this.counters.get(row);
		if ( rowCounters == null ){
			return null;
		}
		else{
			return rowCounters.get(column);
		}
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		// Nagłówek
		sb.append(this.toStringHeader());
		sb.append("\n");
		for ( String row : this.rowValues ){
			sb.append(this.toString(row));
			sb.append("\n");
		}
		return sb.toString();
	}
	
	public String toStringHeader(){
		StringBuilder sb = new StringBuilder();
		// Nagłówek
		sb.append("Cecha");
		for ( String column : this.columnValues ){
			sb.append("\t"+column);
		}
		return sb.toString();
	}
	
	public String toString(String row){
		StringBuilder sb = new StringBuilder();
		sb.append(row);
		for ( String column : this.columnValues ){
			Counter counter = this.getCounter(row, column);
			sb.append("\t"+(counter==null ? 0 : counter.getValue()));
		}
		return sb.toString();
	}
	

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

}
