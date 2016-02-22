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
import java.sql.SQLException;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Byte Payload Statement Parameter.
 * 
 * 
 * @config jdbc-byte-payload-statement-parameter
 * @author amcgrath
 * 
 */
@XStreamAlias("jdbc-byte-payload-statement-parameter")
public class BytePayloadStatementParameter extends NamedStatementParameter {
  
  public BytePayloadStatementParameter() {
  }

  public BytePayloadStatementParameter(String name) {
    this();
    setName(name);
  }
  
  @Override
  public void apply(int parameterIndex, PreparedStatement statement, AdaptrisMessage msg) throws SQLException, ServiceException {
    statement.setBytes(parameterIndex, msg.getPayload());
  }

  @Override
  public BytePayloadStatementParameter makeCopy() {
    return new BytePayloadStatementParameter(getName());
  }
}
