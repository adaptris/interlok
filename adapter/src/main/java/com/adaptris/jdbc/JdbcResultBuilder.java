package com.adaptris.jdbc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.adaptris.core.util.JdbcUtil;

public class JdbcResultBuilder {

  private JdbcResult result;

  public JdbcResultBuilder() {
    result = new JdbcResult();
  }

  public JdbcResultBuilder setInOutParameters(List<StoredProcedureParameter> parameters) {
    result.setParameters(parameters);
    return this;
  }

  public JdbcResultBuilder setHasResultSet(boolean hasResultSet) {
    result.setHasResultSet(hasResultSet);
    return this;
  }

  public JdbcResultBuilder setResultSet(ResultSet resultSet) throws SQLException {
    result.addResultSet(this.mapResultSet(resultSet));
    return this;
  }

  public JdbcResultBuilder setResultSet(Statement statement) throws SQLException {
    result.setResultSets(this.mapResultSet(statement));
    return this;
  }

  public JdbcResultBuilder setRowsUpdatedCount(int count) throws SQLException {
    result.setNumRowsUpdated(count);
    return this;
  }

  public JdbcResult build() {
    return result;
  }

  private JdbcResultSet mapResultSet(ResultSet resultSet) throws SQLException {
    JdbcResultSet resultReturned = new JdbcResultSet();

    if(resultSet != null) {
      ResultSetMetaData rsmd = resultSet.getMetaData();
      int columnCount = rsmd.getColumnCount();

      while(resultSet.next()) {
        result.setHasResultSet(true);
        JdbcResultRow row = new JdbcResultRow();
        for(int counter = 1; counter <= columnCount; counter ++) {
          row.setFieldValue(rsmd.getColumnName(counter), resultSet.getObject(counter));
        }
        resultReturned.addRow(row);
      }
    }
    JdbcUtil.closeQuietly(resultSet);

    return resultReturned;
  }

  private List<JdbcResultSet> mapResultSet(Statement statement) throws SQLException {
    ArrayList<JdbcResultSet> result = new ArrayList<JdbcResultSet>();

    do{
      JdbcResultSet singleResultSet = new JdbcResultSet();
      ResultSet resultSet = statement.getResultSet();

      if(resultSet != null) {
        ResultSetMetaData rsmd = resultSet.getMetaData();
        int columnCount = rsmd.getColumnCount();

        while(resultSet.next()) {
          JdbcResultRow row = new JdbcResultRow();
          for(int counter = 1; counter <= columnCount; counter ++) {
            row.setFieldValue(rsmd.getColumnName(counter), resultSet.getObject(counter));
          }
          singleResultSet.addRow(row);
        }

        result.add(singleResultSet);
      }
      JdbcUtil.closeQuietly(resultSet);

    } while(statement.getMoreResults());

    return result;
  }
}
