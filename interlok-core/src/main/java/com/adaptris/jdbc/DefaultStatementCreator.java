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
