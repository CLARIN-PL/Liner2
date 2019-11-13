package g419.corpus.structure;

import java.util.*;

/**
 * Klasa reprezentuje żadką macierz.
 *
 * @author czuk
 */
public class SparseMatrix<Columns, Row, Cell> {

  Set<Row> rowValues = new TreeSet<>();
  Set<Columns> columnValues = new TreeSet<>();

  // Wiersz => Kolumna => Komórka
  Map<Row, Map<Columns, Cell>> cells = new HashMap<>();

  public Set<Row> getRows() {
    return rowValues;
  }

  public Set<Columns> getColumns() {
    return columnValues;
  }

  public void removeColumn(final Columns column) {
    columnValues.remove(column);
    for (final Map<Columns, Cell> rowValues : cells.values()) {
      rowValues.remove(column);
    }
  }


  public void removeColumns(final Collection<Columns> columns) {
    columnValues.removeAll(columns);
    for (final Map<Columns, Cell> rowValues : cells.values()) {
      for (final Columns column : columns) {
        rowValues.remove(column);
      }
    }
  }

  public List<Cell> getRowValues(final String row) {
    final List<Cell> values = new ArrayList<>();
    final Map<Columns, Cell> rowCounters = cells.get(row);
    if (rowCounters != null) {
      for (final Columns column : columnValues) {
        final Cell counter = rowCounters.get(column);
        values.add(counter == null ? null : counter);
      }
    }
    return values;
  }

  public void addCell(final Row row, final Columns column, final Cell object) {
    rowValues.add(row);
    columnValues.add(column);
    Map<Columns, Cell> rowCells = cells.get(row);
    if (rowCells == null) {
      rowCells = new HashMap<>();
      cells.put(row, rowCells);
    }
    if (rowCells.containsKey(object)) {
      rowCells.remove(object);
    }
    rowCells.put(column, object);
  }

  public Cell getCell(final Row row, final Columns column) {
    final Map<Columns, Cell> rowCells = cells.get(row);
    if (rowCells == null) {
      return null;
    } else {
      return rowCells.get(column);
    }
  }

}
