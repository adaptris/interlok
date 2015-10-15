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

public class NamedParameterApplicatorTest extends TestCase {
  
  private String fiveParamSqlStatement = "insert into table values(#paramOne,#paramTwo,#paramThree,#paramFour,#paramFive)";
  private String twoParamSqlStatementDiffPrefix = "insert into table values(%paramOne,%paramTwo)";
  private String twoParamSqlStatement = "insert into table values(#paramOne,#paramTwo)";
  private String twoParamSqlStatementNoNames = "insert into table values(?,?)";
  private String zeroParamSqlStatement = "select * from dual";
  private String twoParamSelectStatement = "select * from test where fieldOne=#paramOne and fieldTwo=#paramTwo";
  private String expectedTwoParamSelectStatement = "select * from test where fieldOne=? and fieldTwo=?";
  private String twoParamSelectStatementNameNotExists = "select * from test where fieldOne=#xxx and fieldTwo=#zzz";
  
  private StatementParameterList parameters;
  
  private NamedParameterApplicator parameterApplicator;
  
  private AdaptrisMessage message;
  
  @Mock
  private PreparedStatement mockStatement;
  
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    
    message = DefaultMessageFactory.getDefaultInstance().newMessage("SomePayload");
    parameterApplicator = new NamedParameterApplicator();
    
    StatementParameter param1 = new StatementParameter("MyValue1", "java.lang.String", StatementParameter.QueryType.constant);
    param1.setName("paramOne");
    StatementParameter param2 = new StatementParameter("MyValue2", "java.lang.String", StatementParameter.QueryType.constant);
    param2.setName("paramTwo");
    
    parameters = new StatementParameterList();
    parameters.add(param1);
    parameters.add(param2);
  }
  
  public void tearDown() throws Exception {
    
  }
  
  public void testPrepareStatementNoNamedParams() throws Exception {
    assertEquals(twoParamSqlStatementNoNames, parameterApplicator.prepareParametersToStatement(twoParamSqlStatementNoNames));
  }
  
  public void testPrepareStatementTwoNamedParams() throws Exception {
    assertEquals(twoParamSqlStatementNoNames, parameterApplicator.prepareParametersToStatement(twoParamSqlStatement));
  }
  
  public void testSelectPrepareStatementTwoNamedParams() throws Exception {
    assertEquals(expectedTwoParamSelectStatement, parameterApplicator.prepareParametersToStatement(twoParamSelectStatement));
  }
  
  public void testPrepareStatementTwoNamedParamsDiffPrefix() throws Exception {
    assertEquals(twoParamSqlStatementDiffPrefix, parameterApplicator.prepareParametersToStatement(twoParamSqlStatementDiffPrefix));
  }
  
  public void testPrepareStatementTwoNamedParamsConfiguredDiffPrefix() throws Exception {
    parameterApplicator.setParameterNamePrefix("%");
    parameterApplicator.setParameterNameRegex("%\\w*");
    assertEquals(twoParamSqlStatementNoNames, parameterApplicator.prepareParametersToStatement(twoParamSqlStatementDiffPrefix));
  }
  
  public void testParameterApplicator() throws Exception {
    parameterApplicator.applyStatementParameters(message, mockStatement, parameters, twoParamSqlStatement);
    
    verify(mockStatement).setObject(1, "MyValue1");
    verify(mockStatement).setObject(2, "MyValue2");
  }
  
  public void testParameterApplicatorParamNotFound() throws Exception {
    try {
      parameterApplicator.applyStatementParameters(message, mockStatement, parameters, twoParamSelectStatementNameNotExists);
      fail("Should fail, cannot find the param xxx in the list");
    } catch(ServiceException ex) {
      //expected
    }
  }
  
  public void testParameterApplicatorParametersOutOfOrder() throws Exception {
    StatementParameter param1 = new StatementParameter("MyValue1", "java.lang.String", StatementParameter.QueryType.constant);
    param1.setName("paramOne");
    StatementParameter param5 = new StatementParameter("MyValue5", "java.lang.String", StatementParameter.QueryType.constant);
    param5.setName("paramFive");
    StatementParameter param2 = new StatementParameter("MyValue2", "java.lang.String", StatementParameter.QueryType.constant);
    param2.setName("paramTwo");
    StatementParameter param3 = new StatementParameter("MyValue3", "java.lang.String", StatementParameter.QueryType.constant);
    param3.setName("paramThree");
    StatementParameter param4 = new StatementParameter("MyValue4", "java.lang.String", StatementParameter.QueryType.constant);
    param4.setName("paramFour");
    
    parameters = new StatementParameterList();
    parameters.add(param1);
    parameters.add(param5);
    parameters.add(param2);
    parameters.add(param3);
    parameters.add(param4);
    
    parameterApplicator.applyStatementParameters(message, mockStatement, parameters, fiveParamSqlStatement);
    
    verify(mockStatement).setObject(1, "MyValue1");
    verify(mockStatement).setObject(2, "MyValue2");
    verify(mockStatement).setObject(3, "MyValue3");
    verify(mockStatement).setObject(4, "MyValue4");
    verify(mockStatement).setObject(5, "MyValue5");
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

  
  

}
