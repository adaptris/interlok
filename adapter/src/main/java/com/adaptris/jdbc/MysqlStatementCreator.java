package com.adaptris.jdbc;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * MySQL CallableStatement generator.
 * <p>
 * Depending on which version of the mysql jdbc driver you use, you can either use {@link MysqlStatementCreator} or
 * {@link DefaultStatementCreator}
 * </p>
 * 
 * @config mysql-statement-creator
 * 
 * @author lchan
 * 
 */
@XStreamAlias("mysql-statement-creator")
public class MysqlStatementCreator implements CallableStatementCreator {

  /**
   * Creates a String of <code>CALL my_stored_procedure(?, ?, ?);</code> which is valid for mysql-connector-5.
   * 
   * @see CallableStatementCreator#createCall(java.lang.String, int)
   */
  public String createCall(String procedureName, int parameterCount) {
    StringBuffer sb = new StringBuffer("CALL ").append(procedureName).append("(");

    for (int i = 0; i < parameterCount; i++) {
      sb.append("?");
      if (i < parameterCount - 1) {
        sb.append(", ");
      }
    }
    sb.append(");");
    return sb.toString();
  }
}
