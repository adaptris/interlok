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

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldHint;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * ConfiguredStatementCreator.
 * 
 * <p>
 * With this implementation of the CallableStatementCreator, you can specify the entire statement string. An example of a full
 * statement; { CALL procedureName(?, ?, ?); }
 * </p>
 * <p>
 * You have the choice of hard-coding the procedure name as in the above example, or simply inserting the dollar ($) symbol to have
 * the procedure name injected for you; { CALL $(?, ?, ?); }
 * </p>
 * 
 * @config configured-statement-creator
 * 
 */
@XStreamAlias("configured-statement-creator")
@DisplayOrder(order = {"statement"})
public class ConfiguredStatementCreator implements CallableStatementCreator {

  @InputFieldHint(style = "SQL")
  @NotBlank
  public String statement;
  
  @Override
  public String createCall(String procedureName, int parameterCount) {
    if(statement.indexOf("$") >= 0)
      return statement.replace("$", procedureName);
    
    return statement;
  }

  public String getStatement() {
    return statement;
  }

  public void setStatement(String statement) {
    this.statement = statement;
  }

}
