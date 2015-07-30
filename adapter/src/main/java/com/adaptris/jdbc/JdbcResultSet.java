package com.adaptris.jdbc;

import java.util.ArrayList;
import java.util.List;

public class JdbcResultSet {

  private List<JdbcResultRow> rows;
  
  public JdbcResultSet() {
    this.setRows(new ArrayList<JdbcResultRow>());
  }

  public List<JdbcResultRow> getRows() {
    return rows;
  }

  public void setRows(List<JdbcResultRow> rows) {
    this.rows = rows;
  }

  public void addRow(JdbcResultRow row) {
    this.getRows().add(row);
  }
  
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append(this.getClass().getSimpleName() + " " + this.getRows().size() + " rows\n");
    
    for(JdbcResultRow row : this.getRows()) {
      buffer.append("\t\t" + row.toString() + "\n");
    }
    
    return buffer.toString();
  }
}
