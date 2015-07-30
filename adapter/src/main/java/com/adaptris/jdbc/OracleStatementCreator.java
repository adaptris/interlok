package com.adaptris.jdbc;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Oracle CallableStatement generator.
 * 
 * @config oracle-statement-creator
 * 
 */
@XStreamAlias("oracle-statement-creator")
public class OracleStatementCreator implements CallableStatementCreator {

  /**
   * Creates a String of <code>begin ? := procedureName(?,?,?,?,?); end; </code> which should be suitable for Oracle databases.
   * 
   * @see CallableStatementCreator#createCall(java.lang.String, int)
   */
  public String createCall(String procedureName, int parameterCount) {
    StringBuffer sb = new StringBuffer();
    sb.append("begin ? := ");
    sb.append(procedureName);
    sb.append("(");

    for (int i = 0; i < parameterCount-1; i++) {
      sb.append("?");
      if (i < parameterCount - 2) {
        sb.append(", ");
      }
    }
    sb.append("); end; ");
    return sb.toString();
  }
}
