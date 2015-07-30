package com.adaptris.jdbc;

import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.adaptris.core.BaseCase;
import com.adaptris.core.CoreException;
import com.adaptris.core.jdbc.JdbcConnection;
import com.adaptris.core.util.LifecycleHelper;

public class JdbcStoredProcedureTest extends BaseCase {

  public JdbcStoredProcedureTest(String name) {
    super(name);
  }

  private static final String JDBC_STOREDPROC_TESTS_ENABLED = "jdbc.storedproc.tests.enabled";
  private static final String JDBC_DRIVER = "jdbc.storedproc.driver";
  private static final String JDBC_URL = "jdbc.storedproc.url";
  private static final String JDBC_USER = "jdbc.storedproc.username";
  private static final String JDBC_PASSWORD = "jdbc.storedproc.password";
  private static final String JDBC_VENDOR = "jdbc.storedproc.vendor";

  private JdbcConnection jdbcConnection;
  private Connection connection;
  
  private CallableStatementCreator statementCreator;

  private StoredProcedure storedProcedure;

  private boolean testsEnabled = false;

  public void setUp() throws Exception {
    if(Boolean.parseBoolean(PROPERTIES.getProperty(JDBC_STOREDPROC_TESTS_ENABLED, "false"))) {
      jdbcConnection = new JdbcConnection(PROPERTIES.getProperty(JDBC_URL), PROPERTIES.getProperty(JDBC_DRIVER));
      jdbcConnection.setUsername(PROPERTIES.getProperty(JDBC_USER));
      jdbcConnection.setPassword(PROPERTIES.getProperty(JDBC_PASSWORD));
      
      LifecycleHelper.init(jdbcConnection);
      LifecycleHelper.start(jdbcConnection);
      
      if(PROPERTIES.getProperty(JDBC_VENDOR).equals("mysql"))
        statementCreator = new MysqlStatementCreator();
      else if(PROPERTIES.getProperty(JDBC_VENDOR).equals("sqlserver"))
        statementCreator = new SqlServerStatementCreator();
      else
        fail("Vendor for JDBC tests unknown: " + PROPERTIES.getProperty(JDBC_VENDOR));
        
      connection = jdbcConnection.connect();
      
      storedProcedure = new StoredProcedure();
      storedProcedure.setConnection(connection);
      storedProcedure.setStatementCreator(statementCreator);
      storedProcedure.setStatementExecutor(new ExecuteCallableStatementExecutor());
      
      testsEnabled = true;
    }
  }

  public void tearDown() throws Exception {
    if(testsEnabled)
      connection.close();
  }

  public void testNoParams() throws Exception {
    if (testsEnabled) {
      storedProcedure.setName("no_params");
      
      JdbcResult procedureResult = storedProcedure.execute();
      assertEquals(true, procedureResult.isHasResultSet());
    }
  }
  
  public void testOneIn() throws Exception {
    if(testsEnabled) {
      storedProcedure.setName("one_in");
      
      StoredProcedureParameter parameter = new StoredProcedureParameter("xType", 1, ParameterValueType.VARCHAR, ParameterType.IN, "Sold");
      storedProcedure.addParameter(parameter);
      
      JdbcResult procedureResult = storedProcedure.execute();
      assertEquals(true, procedureResult.isHasResultSet());
    }
  }
  
  public void testManyIn() throws Exception {
    if(testsEnabled) {
      storedProcedure.setName("many_in");
      
      StoredProcedureParameter parameter1 = new StoredProcedureParameter("xType", 1, ParameterValueType.VARCHAR, ParameterType.IN, "Sold");
      StoredProcedureParameter parameter2 = new StoredProcedureParameter("xManager", 2, ParameterValueType.VARCHAR, ParameterType.IN, "Gerard Houllier");
      
      storedProcedure.addParameter(parameter1);
      storedProcedure.addParameter(parameter2);
      
      JdbcResult procedureResult = storedProcedure.execute();
      assertEquals(true, procedureResult.isHasResultSet());
    }
  }
  
  public void testOneOut() throws Exception {
    if(testsEnabled) {
      storedProcedure.setName("one_out");
      
      StoredProcedureParameter parameter = new StoredProcedureParameter("transferCount", 1, ParameterValueType.INTEGER, ParameterType.OUT, null);
      storedProcedure.addParameter(parameter);
      
      JdbcResult procedureResult = storedProcedure.execute();
      assertEquals(false, procedureResult.isHasResultSet());
      assertEquals(31, Integer.parseInt(procedureResult.getParameters().get(0).getOutValue().toString()));
    }
  }
  
  public void testManyOut() throws Exception {
    if(testsEnabled) {
      storedProcedure.setName("many_out");
      
      StoredProcedureParameter parameter1 = new StoredProcedureParameter("numberSold", 1, ParameterValueType.INTEGER, ParameterType.OUT, null);
      StoredProcedureParameter parameter2 = new StoredProcedureParameter("numberPurchased", 2, ParameterValueType.INTEGER, ParameterType.OUT, null);
      StoredProcedureParameter parameter3 = new StoredProcedureParameter("totalTransfers", 3, ParameterValueType.INTEGER, ParameterType.OUT, null);
      
      storedProcedure.addParameter(parameter1);
      storedProcedure.addParameter(parameter2);
      storedProcedure.addParameter(parameter3);
      
      JdbcResult procedureResult = storedProcedure.execute();
      assertEquals(false, procedureResult.isHasResultSet());
      
      assertEquals(15, Integer.parseInt(procedureResult.getParameters().get(0).getOutValue().toString()));
      assertEquals(15, Integer.parseInt(procedureResult.getParameters().get(1).getOutValue().toString()));
      assertEquals(31, Integer.parseInt(procedureResult.getParameters().get(2).getOutValue().toString()));
    }
  }
  
  public void testOneInout() throws Exception {
    if(testsEnabled) {
      storedProcedure.setName("one_inout");
      
      StoredProcedureParameter parameter = new StoredProcedureParameter("xSomeAmount", 1, ParameterValueType.INTEGER, ParameterType.INOUT, 100);
      storedProcedure.addParameter(parameter);
      
      JdbcResult procedureResult = storedProcedure.execute();
      assertEquals(false, procedureResult.isHasResultSet());
      
      assertEquals(105, Integer.parseInt(procedureResult.getParameters().get(0).getOutValue().toString()));
    }
  }
  
  public void testOneInOneOut() throws Exception {
    if(testsEnabled) {
      storedProcedure.setName("one_in_one_out");
      
      StoredProcedureParameter parameter1 = new StoredProcedureParameter("xType", 1, ParameterValueType.VARCHAR, ParameterType.IN, "Sold");
      StoredProcedureParameter parameter2 = new StoredProcedureParameter("transferCount", 2, ParameterValueType.INTEGER, ParameterType.OUT, null);
      
      storedProcedure.addParameter(parameter1);
      storedProcedure.addParameter(parameter2);
      
      JdbcResult procedureResult = storedProcedure.execute();
      
      assertEquals(false, procedureResult.isHasResultSet());
      assertEquals(15, Integer.parseInt(procedureResult.getParameters().get(1).getOutValue().toString()));
    }
  }
  
  public void testManyInManyOut() throws Exception {
    if(testsEnabled) {
      storedProcedure.setName("many_in_many_out");
      
      StoredProcedureParameter parameter1 = new StoredProcedureParameter("xManagerName", 1, ParameterValueType.VARCHAR, ParameterType.IN, "Gerard Houllier");
      StoredProcedureParameter parameter2 = new StoredProcedureParameter("xDateFrom", 2, ParameterValueType.DATE, ParameterType.IN, this.createFormattedDate(1, 1, 2004));
      StoredProcedureParameter parameter3 = new StoredProcedureParameter("xDateTo", 3, ParameterValueType.DATE, ParameterType.IN, this.createFormattedDate(1, 6, 2004));
      StoredProcedureParameter parameter4 = new StoredProcedureParameter("transferType", 4, ParameterValueType.VARCHAR, ParameterType.OUT, null);
      StoredProcedureParameter parameter5 = new StoredProcedureParameter("playerName", 5, ParameterValueType.VARCHAR, ParameterType.OUT, null);
      
      storedProcedure.addParameter(parameter1);
      storedProcedure.addParameter(parameter2);
      storedProcedure.addParameter(parameter3);
      storedProcedure.addParameter(parameter4);
      storedProcedure.addParameter(parameter5);
      
      JdbcResult procedureResult = storedProcedure.execute();
      assertEquals(false, procedureResult.isHasResultSet());
      assertEquals("Sold", procedureResult.getParameters().get(3).getOutValue());
      assertEquals("Emile Heskey", procedureResult.getParameters().get(4).getOutValue());
    }
  }
  
  public void testOneInoutOneIn() throws Exception {
    if(testsEnabled) {
      storedProcedure.setName("one_inout_one_in");
      
      StoredProcedureParameter parameter1 = new StoredProcedureParameter("xAgentsAmount", 1, ParameterValueType.INTEGER, ParameterType.INOUT, 500);
      StoredProcedureParameter parameter2 = new StoredProcedureParameter("xManagerName", 2, ParameterValueType.VARCHAR, ParameterType.IN, "Gerard Houllier");

      storedProcedure.addParameter(parameter1);
      storedProcedure.addParameter(parameter2);
      
      JdbcResult procedureResult = storedProcedure.execute();
      
      assertEquals(false, procedureResult.isHasResultSet());
      assertEquals(14500500, Integer.parseInt(procedureResult.getParameters().get(0).getOutValue().toString()));
    }
  }
  
  public void testOneInoutOneOut() throws Exception {
    if(testsEnabled) {
      storedProcedure.setName("one_inout_one_out");
      
      StoredProcedureParameter parameter1 = new StoredProcedureParameter("xAmount", 1, ParameterValueType.INTEGER, ParameterType.INOUT, 4000000);
      StoredProcedureParameter parameter2 = new StoredProcedureParameter("playersName", 2, ParameterValueType.VARCHAR, ParameterType.OUT, null);

      storedProcedure.addParameter(parameter1);
      storedProcedure.addParameter(parameter2);
      
      JdbcResult procedureResult = storedProcedure.execute();
      
      assertEquals(false, procedureResult.isHasResultSet());
      assertEquals(3750000, Integer.parseInt(procedureResult.getParameters().get(0).getOutValue().toString()));
      assertEquals("Sander Westerveld", procedureResult.getParameters().get(1).getOutValue());
    }
  }
  
  public void testOneInoutOneInOneOut() throws Exception {
    if(testsEnabled) {
      storedProcedure.setName("one_inout_one_in_one_out");
      
      StoredProcedureParameter parameter1 = new StoredProcedureParameter("xAmount", 1, ParameterValueType.INTEGER, ParameterType.INOUT, 8000000);
      StoredProcedureParameter parameter2 = new StoredProcedureParameter("managersName", 2, ParameterValueType.VARCHAR, ParameterType.IN, "Rafael Benitez");
      StoredProcedureParameter parameter3 = new StoredProcedureParameter("playersName", 3, ParameterValueType.VARCHAR, ParameterType.OUT, null);

      storedProcedure.addParameter(parameter1);
      storedProcedure.addParameter(parameter2);
      storedProcedure.addParameter(parameter3);
      
      JdbcResult procedureResult = storedProcedure.execute();
 
      assertEquals(false, procedureResult.isHasResultSet());
      assertEquals(7000000, Integer.parseInt(procedureResult.getParameters().get(0).getOutValue().toString()));
      assertEquals("Peter Crouch", procedureResult.getParameters().get(2).getOutValue());
    }
  }
  
  public void testOneInoutOneInOneOutExecuteExecutor() throws Exception {
    if(testsEnabled) {
      storedProcedure.setName("one_inout_one_in_one_out");
      storedProcedure.setStatementExecutor(new ExecuteCallableStatementExecutor());
      
      StoredProcedureParameter parameter1 = new StoredProcedureParameter("xAmount", 1, ParameterValueType.INTEGER, ParameterType.INOUT, 8000000);
      StoredProcedureParameter parameter2 = new StoredProcedureParameter("managersName", 2, ParameterValueType.VARCHAR, ParameterType.IN, "Rafael Benitez");
      StoredProcedureParameter parameter3 = new StoredProcedureParameter("playersName", 3, ParameterValueType.VARCHAR, ParameterType.OUT, null);

      storedProcedure.addParameter(parameter1);
      storedProcedure.addParameter(parameter2);
      storedProcedure.addParameter(parameter3);
      
      JdbcResult procedureResult = storedProcedure.execute();
 
      assertEquals(false, procedureResult.isHasResultSet());
      assertEquals(7000000, Integer.parseInt(procedureResult.getParameters().get(0).getOutValue().toString()));
      assertEquals("Peter Crouch", procedureResult.getParameters().get(2).getOutValue());
    }
  }
  
  public void testNoNamesOneInoutOneInOneOut() throws Exception {
    if(testsEnabled) {
      storedProcedure.setName("one_inout_one_in_one_out");
      
      StoredProcedureParameter parameter1 = new StoredProcedureParameter("", 1, ParameterValueType.INTEGER, ParameterType.INOUT, 8000000);
      StoredProcedureParameter parameter2 = new StoredProcedureParameter("", 2, ParameterValueType.VARCHAR, ParameterType.IN, "Rafael Benitez");
      StoredProcedureParameter parameter3 = new StoredProcedureParameter("", 3, ParameterValueType.VARCHAR, ParameterType.OUT, null);

      storedProcedure.addParameter(parameter1);
      storedProcedure.addParameter(parameter2);
      storedProcedure.addParameter(parameter3);
      
      JdbcResult procedureResult = storedProcedure.execute();
 
      assertEquals(false, procedureResult.isHasResultSet());
      assertEquals(7000000, Integer.parseInt(procedureResult.getParameters().get(0).getOutValue().toString()));
      assertEquals("Peter Crouch", procedureResult.getParameters().get(2).getOutValue());
    }
  }
  
  public void testOneResultsetExecuteUpdate() throws Exception {
    if(testsEnabled) {
      storedProcedure.setName("one_resultset");
      storedProcedure.setStatementExecutor(new ExecuteUpdateCallableStatementExecutor());
      
      try {
        JdbcResult procedureResult = storedProcedure.execute();
        assertEquals(false, procedureResult.isHasResultSet());
      } catch (Exception ex) {
        // some drivers will thrown an error here, but that's fine too.
      }
    }
  }
  
  public void testOneResultsetExecuteQuery() throws Exception {
    if(testsEnabled) {
      storedProcedure.setName("one_resultset");
      storedProcedure.setStatementExecutor(new ExecuteQueryCallableStatementExecutor());
      
      JdbcResult procedureResult = storedProcedure.execute();
      assertEquals(true, procedureResult.isHasResultSet());
      
      assertEquals(1, procedureResult.countResultSets());
      assertEquals(5, procedureResult.getResultSet(0).getRows().size());  
    }
  }
  
  public void testOneResultset() throws Exception {
    if(testsEnabled) {
      storedProcedure.setName("one_resultset");
      
      JdbcResult procedureResult = storedProcedure.execute();
      assertEquals(true, procedureResult.isHasResultSet());
      
      assertEquals(1, procedureResult.countResultSets());
      assertEquals(5, procedureResult.getResultSet(0).getRows().size());  
    }
  }
  
  public void testManyResultsets() throws Exception {
    if(testsEnabled) {
      storedProcedure.setName("many_resultsets");
      
      JdbcResult procedureResult = storedProcedure.execute();
      assertEquals(true, procedureResult.isHasResultSet());
      
      assertEquals(2, procedureResult.countResultSets());
      
      assertEquals(5, procedureResult.getResultSet(0).getRows().size());  
      assertEquals(5, procedureResult.getResultSet(1).getRows().size());  
    }
  }
  
  public void testStoredProcedureMultiParamConstructor() throws Exception {
    if(testsEnabled) {
      StoredProcedure storedProcedure = new StoredProcedure("many_resultsets", new ArrayList<StoredProcedureParameter>(), connection, statementCreator);
      storedProcedure.setStatementExecutor(new ExecuteCallableStatementExecutor());
      
      JdbcResult procedureResult = storedProcedure.execute();
      assertEquals(true, procedureResult.isHasResultSet());
      
      assertEquals(2, procedureResult.countResultSets());
      
      assertEquals(5, procedureResult.getResultSet(0).getRows().size());  
      assertEquals(5, procedureResult.getResultSet(1).getRows().size());  
    }
  }
  
  public void testStoredProcedureNameDoesntExist() throws Exception {
    if(testsEnabled) {
      StoredProcedure storedProcedure = new StoredProcedure("I_do_not_exist", new ArrayList<StoredProcedureParameter>(), connection, statementCreator);
      storedProcedure.setStatementExecutor(new ExecuteCallableStatementExecutor());
      
      try {
        storedProcedure.execute();
      } catch (CoreException ex) {
        // expected.
      }
    }
  }
  
  private String createFormattedDate(int dayOfMonth, int month, int year) {
    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Calendar date = new GregorianCalendar();
    date.setTime(new Date());
    date.set(Calendar.HOUR_OF_DAY, 0);
    date.set(Calendar.MINUTE, 0);
    date.set(Calendar.SECOND, 0);
    date.set(Calendar.DAY_OF_MONTH, dayOfMonth);
    date.set(Calendar.MONTH, month);
    date.set(Calendar.YEAR, year);

    return formatter.format(date.getTime());
  }

}
