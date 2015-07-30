package com.adaptris.jdbc;

/**
 * @author lchan
 *
 */
public interface CallableStatementCreator {

  /**
   * Create a CallableStatement String suitable for the database in question.
   * 
   * @param procedureName the procedure name
   * @param parameterCount The number of parameters for this stored procedure call.
   * @return a String similar to <code>{ CALL my_stored_procedure(?, ?, ?); }</code> depending on the database.
   */
  String createCall(String procedureName, int parameterCount);
}
