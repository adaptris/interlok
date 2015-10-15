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

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Binary Stream Statement Parameter.
 * 
 * <p>
 * Note that this class pays no heed to the {@link StatementParameter#setQueryClass(String)} setting. Additionally,
 * {@link StatementParameter#setConvertNull(Boolean)} has no meaning; null/empty/whitespace values are implicitly false.
 * </p>
 * 
 * @config jdbc-binary-stream-statement-parameter
 * @author amcgrath
 * 
 */
@XStreamAlias("jdbc-binary-stream-statement-parameter")
public class BinaryStreamStatementParameter extends StatementParameter {

  public BinaryStreamStatementParameter() {
    super();
    setQueryType(QueryType.payload);
  }

  public BinaryStreamStatementParameter(String query, Class<?> clazz, QueryType type) {
    this(query, clazz, type, null);
  }

  public BinaryStreamStatementParameter(String query, String classname, QueryType type) {
    this(query, classname, type, null);
  }

  public BinaryStreamStatementParameter(String query, String classname, QueryType type, Boolean nullConvert) {
    this(query, classname, type, nullConvert, null);
  }

  public BinaryStreamStatementParameter(String query, String classname, QueryType type, Boolean nullConvert, String paramName) {
    super(query, classname, type, nullConvert, paramName);
  }
  
  public BinaryStreamStatementParameter(String query, Class<?> clazz, QueryType type, Boolean nullConvert) {
    this(query, clazz.getName(), type, nullConvert);
  }
  
  @Override
  public void apply(int parameterIndex, PreparedStatement statement, AdaptrisMessage msg) throws SQLException, ServiceException {
    try {
      statement.setBinaryStream(parameterIndex, msg.getInputStream());
    } catch (IOException ex) {
      throw new ServiceException(ex);
    }
  }
  
  @Override
  public Object convertToQueryClass(Object value) throws ServiceException {
    return null;
  }
  
  public Object getQueryValue(AdaptrisMessage msg) {
    return null;
  }
  
}
