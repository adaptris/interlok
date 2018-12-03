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

import java.sql.PreparedStatement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link ParameterApplicator} implementation that allows referencing by name.
 * 
 * <p>
 * Using a {@link NamedParameterApplicator} implementation means that you can modify your SQL statement to reference named statement
 * parameter making it no longer depending on declaration order.
 * </p>
 * <p>
 * For instance:
 * 
 * <pre>
 * {@code SELECT * FROM mytable WHERE field1=#param1 AND field2=#param2 AND field3=#param3 AND field4=#param4 AND field5=#param5}
 * </pre>
 * If you then named your statement parameters as {@code param1, param2, param3, param4, param5} using
 * {@link StatementParameter#setName(String)} then the order of parameters as they appear in configuration no longer matters.
 * </p>
 * 
 * @author lchan
 * 
 */
@XStreamAlias("named-parameter-applicator")
public class NamedParameterApplicator implements ParameterApplicator {

  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  public static final String DEFAULT_PARAM_NAME_PREFIX = "#";
  public static final String DEFAULT_PARAM_NAME_REGEX = "#\\w*";

  @NotBlank
  @AutoPopulated
  @InputFieldDefault(value = "#")
  private String parameterNamePrefix;

  @NotBlank
  @AutoPopulated
  @InputFieldDefault(value = "#\\w*")
  private String parameterNameRegex;

  public NamedParameterApplicator() {
    this.setParameterNamePrefix(DEFAULT_PARAM_NAME_PREFIX);
    this.setParameterNameRegex(DEFAULT_PARAM_NAME_REGEX);
  }

  @Override
  public void applyStatementParameters(AdaptrisMessage message, PreparedStatement statement,
                                       StatementParameterCollection parameters, String originalSql) throws ServiceException {
    Matcher m = Pattern.compile(this.getParameterNameRegex()).matcher(originalSql);

    int counter = 0;
    while (m.find()) {
      counter++;

      String parameterName = m.group();
      JdbcStatementParameter statementParameter =
          parameters.getParameterByName(parameterName.substring(this.getParameterNamePrefix()
          .length()));
      try {
        Args.notNull(statementParameter, "statementParameter");
        statementParameter.apply(counter, statement, message);
      }
      catch (Exception ex) {
        throw ExceptionHelper.wrapServiceException(ex);
      }
    }
  }

  @Override
  public String prepareParametersToStatement(String originalStatement) {
    return originalStatement.replaceAll(getParameterNameRegex(), "?");
  }

  public String getParameterNamePrefix() {
    return parameterNamePrefix;
  }

  /**
   * Set the parameter name prefix.
   * 
   * @param s the parameter name prefix, defaults to {@value #DEFAULT_PARAM_NAME_PREFIX}
   */
  public void setParameterNamePrefix(String s) {
    this.parameterNamePrefix = s;
  }

  public String getParameterNameRegex() {
    return parameterNameRegex;
  }

  /**
   * Set the parameter name regular expression.
   * 
   * @param regex the parameter name regex, defaults to {@value #DEFAULT_PARAM_NAME_REGEX}
   */
  public void setParameterNameRegex(String regex) {
    this.parameterNameRegex = regex;
  }
}
