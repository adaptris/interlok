package com.adaptris.jdbc;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Aaron McGrath
 *
 */
public class JdbcResult {
  
  private boolean hasResultSet;
  
  private int numRowsUpdated;
  
  private List<StoredProcedureParameter> parameters;
  
  private List<JdbcResultSet> resultSets;
  
  public JdbcResult() {
    this.setParameters(new ArrayList<StoredProcedureParameter>());
    this.setResultSets(new ArrayList<JdbcResultSet>());
  }

  public boolean isHasResultSet() {
    return hasResultSet;
  }

  public void setHasResultSet(boolean hasResultSet) {
    this.hasResultSet = hasResultSet;
  }

  public List<StoredProcedureParameter> getParameters() {
    return parameters;
  }

  public void setParameters(List<StoredProcedureParameter> parameters) {
    this.parameters = parameters;
  }

  public List<JdbcResultSet> getResulSets() {
    return resultSets;
  }

  public void setResultSets(List<JdbcResultSet> resultSet) {
    this.resultSets = resultSet;
  }

  public void addResultSet(JdbcResultSet resultSet) {
    this.getResulSets().add(resultSet);
  }
  
  public int countResultSets() {
    return this.getResulSets().size();
  }
  
  public JdbcResultSet getResultSet(int index) {
    return this.getResulSets().get(index);
  }
  
  public int getNumRowsUpdated() {
    return numRowsUpdated;
  }

  public void setNumRowsUpdated(int numRowsUpdated) {
    this.numRowsUpdated = numRowsUpdated;
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append(this.getClass().getSimpleName() + "\n");
    buffer.append("HasResultSet = " + this.isHasResultSet() + "\n");
    buffer.append("Parameters;\n");
    for(StoredProcedureParameter param : this.getParameters())
      buffer.append("\t" + param.toString() + "\n");
    buffer.append("ResultSets;\n");
    for(JdbcResultSet resultSet : this.getResulSets())
      buffer.append("\t" + resultSet.toString() + "\n");
    
    return buffer.toString();
  }
}
