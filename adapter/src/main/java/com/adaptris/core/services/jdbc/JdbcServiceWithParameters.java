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

package com.adaptris.core.services.jdbc;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.jdbc.JdbcService;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * Base implementation for interacting with a database with configurable parameters.
 *
 * @since 3.4.0
 */
public abstract class JdbcServiceWithParameters extends JdbcService {

  @NotNull
  @AutoPopulated
  @Valid
  @AdvancedConfig
  private ParameterApplicator parameterApplicator;
  @NotNull
  @AutoPopulated
  @Valid
  @XStreamImplicit
  private StatementParameterList statementParameters;

  public JdbcServiceWithParameters() {
    setParameterApplicator(new SequentialParameterApplicator());
    setStatementParameters(new StatementParameterList());
  }

  public ParameterApplicator getParameterApplicator() {
    return parameterApplicator;
  }

  /**
   * Specify how parameters will be applied to the SQL statement.
   * 
   * @param p the parameter applicator implementation; default is {@link SequentialParameterApplicator}
   * @see SequentialParameterApplicator
   * @see NamedParameterApplicator
   */
  public void setParameterApplicator(ParameterApplicator p) {
    this.parameterApplicator = p;
  }


  /**
   * @return Returns the statementParameters.
   */
  public StatementParameterList getStatementParameters() {
    return statementParameters;
  }

  /**
   * @param list The statementParameters to set.
   */
  public void setStatementParameters(StatementParameterList list) {
    statementParameters = Args.notNull(list, "statementParameters");
  }

  /**
   * Add a StatementParameter to this service.
   * 
   * @see StatementParameter
   * @param query the StatementParameter
   */
  public void addStatementParameter(JdbcStatementParameter query) {
    statementParameters.add(Args.notNull(query, "statementParameter"));
  }
}
