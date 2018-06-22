package g419.corpus.structure;

import java.util.*;

/**
 * Klasa reprezentuje żadką macierz.
 * @author czuk
 *
 */
public class SparseMatrix<Columns, Row, Cell> {

	Set<Row> rowValues = new TreeSet<Row>();
	Set<Columns> columnValues = new TreeSet<Columns>();
	
	// Wiersz => Kolumna => Komórka
	Map<Row, Map<Columns, Cell>> cells = new HashMap<Row, Map<Columns, Cell>>();
	
	public Set<Row> getRows(){
		return this.rowValues;
	}
	
	public Set<Columns> getColumns(){
		return this.columnValues;
	}
	
	public void removeColumn(Columns column){
		this.columnValues.remove(column);
		for ( Map<Columns, Cell> rowValues : cells.values() ){
			rowValues.remove(column);
		}
	}
	

	public void removeColumns(Collection<Columns> columns) {
		this.columnValues.removeAll(columns);
		for ( Map<Columns, Cell> rowValues : cells.values() ){
			for ( Columns column : columns){
				rowValues.remove(column);
			}
		}
	}	
	
	public List<Cell> getRowValues(String row){
		List<Cell> values = new ArrayList<Cell>();
		Map<Columns, Cell> rowCounters = this.cells.get(row);
		if (rowCounters != null ){
			for (Columns column : this.columnValues ){
				Cell counter = rowCounters.get(column);
				values.add(counter == null ? null : counter);
			}
		}
		return values;
	}
	
	public void addCell(Row row, Columns column, Cell object){
		this.rowValues.add(row);
		this.columnValues.add(column);
		Map<Columns, Cell> rowCells = this.cells.get(row);
		if ( rowCells == null ){
			rowCells = new HashMap<Columns, Cell>();
			this.cells.put(row, rowCells);
		}
		if ( rowCells.containsKey(object) ){
			rowCells.remove(object);
		}
		rowCells.put(column, object);
	}
	
	public Cell getCell(Row row, Columns column){
		Map<Columns, Cell> rowCells = this.cells.get(row);
		if ( rowCells == null ){
			return null;
		}
		else{
			return rowCells.get(column);
		}
	}
	
}
