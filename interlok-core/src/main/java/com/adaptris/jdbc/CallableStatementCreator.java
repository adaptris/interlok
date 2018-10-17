/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

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
