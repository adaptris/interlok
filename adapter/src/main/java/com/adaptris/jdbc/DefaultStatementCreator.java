package com.adaptris.jdbc;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Default CallableStatementCreator.
 * 
 * @config default-statement-creator
 * 
 */
@XStreamAlias("default-statement-creator")
public class DefaultStatementCreator implements CallableStatementCreator {

  /**
   * Creates a String of <code>{ CALL my_stored_procedure(?, ?, ?); }</code>
   * 
   * @see CallableStatementCreator#createCall(java.lang.String, int)
   */
  public String createCall(String procedureName, int parameterCount) {
    StringBuffer sb = new StringBuffer("{ CALL ").append(procedureName).append("(");

    for (int i = 0; i < parameterCount; i++) {
      sb.append("?");
      if (i < parameterCount - 1) {
        sb.append(", ");
      }
    }
    sb.append("); }");
    return sb.toString();
  }
}
