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

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Oracle CallableStatement generator.
 * 
 * @config oracle-statement-creator
 * 
 */
@JacksonXmlRootElement(localName = "oracle-statement-creator")
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
    sb.append("); end;");
    return sb.toString();
  }
}
