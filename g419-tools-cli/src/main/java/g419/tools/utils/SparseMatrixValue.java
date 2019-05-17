package g419.tools.utils;

import java.util.*;

/**
 * Klasa reprezentuje rzadką macierz do przechowywania wartości.
 *
 * @author czuk
 */
public class SparseMatrixValue {

  Set<String> rowValues = new TreeSet<String>();
  Set<String> columnValues = new TreeSet<String>();

  // Wiersz => Kolumna => Counter
  Map<String, Map<String, String>> counters = new HashMap<String, Map<String, String>>();

  public Set<String> getRows() {
    return this.rowValues;
  }

  public Set<String> getColumns() {
    return this.columnValues;
  }

  public List<String> getRowValues(String row) {
    List<String> values = new ArrayList<String>();
    Map<String, String> rowCounters = this.counters.get(row);
    if (rowCounters != null) {
      for (String column : this.columnValues) {
        String value = rowCounters.get(column);
        values.add(value == null ? "" : value);
      }
    }
    return values;
  }

  public Map<String, String> getRowValuesSparse(String row) {
    Map<String, String> values = this.counters.get(row);
    if (values == null) {
      values = new HashMap<String, String>();
    }
    return values;
  }

  public void setValue(String row, String column, String value) {
    this.rowValues.add(row);
    this.columnValues.add(column);
    Map<String, String> rowCounters = this.counters.get(row);
    if (rowCounters == null) {
      rowCounters = new HashMap<String, String>();
      this.counters.put(row, rowCounters);
    }
    rowCounters.put(column, value);
  }

  public String getValue(String row, String column) {
    Map<String, String> rowValues = this.counters.get(row);
    if (rowValues == null) {
      return null;
    } else {
      return rowValues.get(column);
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    // Nagłówek
    sb.append(this.toStringHeader());
    sb.append("\n");
    for (String row : this.rowValues) {
      sb.append(this.toString(row));
      sb.append("\n");
    }
    return sb.toString();
  }

  public String toStringHeader() {
    StringBuilder sb = new StringBuilder();
    // Nagłówek
    sb.append("Cecha");
    for (String column : this.columnValues) {
      sb.append("\t" + column);
    }
    return sb.toString();
  }

  public String toString(String row) {
    StringBuilder sb = new StringBuilder();
    sb.append(row);
    for (String column : this.columnValues) {
      String value = this.getValue(row, column);
      sb.append("\t" + (value == null ? 0 : value));
    }
    return sb.toString();
  }

}
