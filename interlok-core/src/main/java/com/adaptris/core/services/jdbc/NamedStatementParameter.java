/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.adaptris.core.services.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class NamedStatementParameter implements JdbcStatementParameter {
  protected transient Logger log = LoggerFactory.getLogger(getClass());

  private String name;
  public NamedStatementParameter() {

  }

  public String getName() {
    return name;
  }

  /**
   * Set the name to be associated with this parameter.
   * <p>
   * The name is only used if you are using a {@link NamedParameterApplicator} to apply parameters to your JDBC statement.
   * </p>
   * 
   * @param name the name
   */
  public void setName(String name) {
    this.name = name;
  }
}
