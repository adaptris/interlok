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

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import junit.framework.TestCase;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.ServiceException;

public class SequentialParameterApplicatorTest extends TestCase {
  
  private String twoParamSqlStatement = "insert into table values(?,?)";
  private String zeroParamSqlStatement = "select * from dual";
  
  private StatementParameterCollection parameters;
  
  private SequentialParameterApplicator parameterApplicator;
  
  private AdaptrisMessage message;
  
  @Mock
  private PreparedStatement mockStatement;
  
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    
    message = DefaultMessageFactory.getDefaultInstance().newMessage("SomePayload");
    parameterApplicator = new SequentialParameterApplicator();
    
    StatementParameter param1 = new StatementParameter("MyValue1", "java.lang.String", StatementParameter.QueryType.constant);
    StatementParameter param2 = new StatementParameter("MyValue2", "java.lang.String", StatementParameter.QueryType.constant);
    
    parameters = new StatementParameterList();
    parameters.add(param1);
    parameters.add(param2);
  }
  
  public void tearDown() throws Exception {
    
  }
  
  public void testParameterApplicator() throws Exception {
    parameterApplicator.applyStatementParameters(message, mockStatement, parameters, twoParamSqlStatement);
    
    verify(mockStatement).setObject(1, "MyValue1");
    verify(mockStatement).setObject(2, "MyValue2");
  }
  
  public void testParameterApplicatorZeroParams() throws Exception {
    parameterApplicator.applyStatementParameters(message, mockStatement, new StatementParameterList(), zeroParamSqlStatement);
    
    verify(mockStatement, never()).setObject(anyInt(), anyString());
  }
  
  public void testParameterApplicatorServiceException() throws Exception {
    doThrow(new SQLException("Expected")).when(mockStatement).setObject(anyInt(), anyString());
    
    try {
      parameterApplicator.applyStatementParameters(message, mockStatement, parameters, twoParamSqlStatement);
      fail("Should throw service exception.");
    } catch (ServiceException ex) {
      // expected
    }
  }

  public void testPrepareStatement() throws Exception {
    assertEquals(zeroParamSqlStatement, parameterApplicator.prepareParametersToStatement(zeroParamSqlStatement));
    assertEquals(twoParamSqlStatement, parameterApplicator.prepareParametersToStatement(twoParamSqlStatement));
  }
}
