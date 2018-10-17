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

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.BaseCase;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.jdbc.StatementParameterImpl.QueryType;
import com.adaptris.core.services.metadata.XpathMetadataServiceTest;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.text.xml.SimpleNamespaceContext;

public class StatementParameterTest extends BaseCase {

  private static final String STRING_VALUE = "ABCDEFG";

  public StatementParameterTest(String n) {
    super(n);
  }

  @Before
  public void setUp() throws Exception {

  }

  @After
  public void tearDown() throws Exception {

  }

  @Test
  public void testConvertString() throws Exception {
    StatementParameter sp = new StatementParameter();
    sp.setQueryClass(String.class.getCanonicalName());
    assertEquals(STRING_VALUE, sp.convertToQueryClass(STRING_VALUE));
  }

  @Test
  public void testConvertNoClass() throws Exception {
    StatementParameter sp = new StatementParameter();
    try {
      sp.convertToQueryClass(STRING_VALUE);
      fail("Expected ServiceException");
    }
    catch (ServiceException expected) {
      // expected
    }
  }

  @Test
  public void testConvertToNonString() throws Exception {
    StatementParameter sp = new StatementParameter();
    sp.setQueryClass(SimpleStringWrapper.class.getCanonicalName());
    SimpleStringWrapper wrapper = new SimpleStringWrapper(STRING_VALUE);
    assertEquals(wrapper, sp.convertToQueryClass(STRING_VALUE));
  }

  @Test
  public void testConvertNull() throws Exception {
    StatementParameter sp = new StatementParameter();
    sp.setQueryClass(String.class.getCanonicalName());
    sp.setConvertNull(false);
    assertNull(sp.convertToQueryClass(null));
  }

  @Test
  public void testConvertWithConvertNull() throws Exception {
    StatementParameter sp = new StatementParameter();
    sp.setQueryClass(String.class.getCanonicalName());
    sp.setConvertNull(true);
    assertEquals("", sp.convertToQueryClass(null));
  }

  @Test
  public void testMakeCopy() throws Exception {
    StatementParameter sp = new StatementParameter("hello", String.class.getName(), QueryType.constant, null, null);
    StatementParameter copy = sp.makeCopy();
    assertRoundtripEquality(sp, copy);
  }

  @Test
  public void testXpathParameter_NoNamespace() throws Exception {
    StatementParameter sp = new StatementParameter("//source-id", String.class.getName(), QueryType.xpath);
    AdaptrisMessage msg = addHelpers(AdaptrisMessageFactory.getDefaultInstance().newMessage(XpathMetadataServiceTest.XML), null);
    MyPreparedStatement stmt = new MyPreparedStatement();
    stmt.clearParameters();
    sp.apply(1, stmt, msg);
    assertEquals("partnera", stmt.getParameter(1));
  }

  @Test
  public void testXpathParameter_WithNamespace() throws Exception {
    StatementParameter sp = new StatementParameter("/svrl:schematron-output/svrl:failed-assert[1]/svrl:text",
        String.class.getName(),
        QueryType.xpath);
    AdaptrisMessage msg = addHelpers(
        AdaptrisMessageFactory.getDefaultInstance().newMessage(XpathMetadataServiceTest.XML_WITH_NAMESPACE),
        XpathMetadataServiceTest.createContextEntries());
    MyPreparedStatement stmt = new MyPreparedStatement();
    stmt.clearParameters();
    sp.apply(1, stmt, msg);
    assertEquals("Error: Product Code must be present.", stmt.getParameter(1));
  }

  @Test
  public void testXpathParameter_NotNamespaceAware() throws Exception {
    StatementParameter sp = new StatementParameter("schematron-output/failed-assert[1]/text", String.class.getName(),
        QueryType.xpath);
    AdaptrisMessage msg = addHelpers(
        AdaptrisMessageFactory.getDefaultInstance().newMessage(XpathMetadataServiceTest.XML_WITH_NAMESPACE),
        null, DocumentBuilderFactoryBuilder.newInstance().withNamespaceAware(false));
    MyPreparedStatement stmt = new MyPreparedStatement();
    stmt.clearParameters();
    sp.apply(1, stmt, msg);
    assertEquals("Error: Product Code must be present.", stmt.getParameter(1));
  }

  @Test
  public void testConstantParameter() throws Exception {
    StatementParameter sp = new StatementParameter("hello", String.class.getName(), QueryType.constant, null, null);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XpathMetadataServiceTest.XML);
    MyPreparedStatement stmt = new MyPreparedStatement();
    stmt.clearParameters();
    sp.apply(1, stmt, msg);
    assertEquals("hello", stmt.getParameter(1));
  }

  @Test
  public void testMetadataParameter() throws Exception {
    StatementParameter sp = new StatementParameter("hello", String.class.getName(), QueryType.metadata, null, null);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XpathMetadataServiceTest.XML);
    msg.addMetadata(new MetadataElement("hello", "world"));
    MyPreparedStatement stmt = new MyPreparedStatement();
    stmt.clearParameters();
    sp.apply(1, stmt, msg);
    assertEquals("world", stmt.getParameter(1));
  }

  private AdaptrisMessage addHelpers(AdaptrisMessage msg) throws CoreException {
    return addHelpers(msg, null);
  }

  private AdaptrisMessage addHelpers(AdaptrisMessage msg, KeyValuePairSet ctx) throws CoreException {
    return addHelpers(msg, ctx, DocumentBuilderFactoryBuilder.newInstance());
  }

  // Copied from JdbcDataQueryService#initXmlHelper()
  private AdaptrisMessage addHelpers(AdaptrisMessage msg, KeyValuePairSet ctx, DocumentBuilderFactoryBuilder builder)
      throws CoreException {
    NamespaceContext namespaceCtx = SimpleNamespaceContext.create(ctx, msg);
    if (namespaceCtx != null) {
      builder = builder.withNamespaceAware(true);
      msg.getObjectHeaders().put(JdbcDataQueryService.KEY_NAMESPACE_CTX, namespaceCtx);
    }
    msg.getObjectHeaders().put(JdbcDataQueryService.KEY_DOCBUILDER_FAC, builder);
    msg.getObjectHeaders().put(JdbcDataQueryService.KEY_XML_UTILS, XmlHelper.createXmlUtils(msg, namespaceCtx, builder));
    return msg;
  }

  private class MyPreparedStatement implements PreparedStatement {

    private Map<Integer, Object> parameters = new HashMap<>();

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
      return null;
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
      return 0;
    }

    @Override
    public void close() throws SQLException {
    }

    @Override
    public int getMaxFieldSize() throws SQLException {
      return 0;
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {
    }

    @Override
    public int getMaxRows() throws SQLException {
      return 0;
    }

    @Override
    public void setMaxRows(int max) throws SQLException {
    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {
    }

    @Override
    public int getQueryTimeout() throws SQLException {
      return 0;
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {
    }

    @Override
    public void cancel() throws SQLException {
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
      return null;
    }

    @Override
    public void clearWarnings() throws SQLException {
    }

    @Override
    public void setCursorName(String name) throws SQLException {
    }

    @Override
    public boolean execute(String sql) throws SQLException {
      return false;
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
      return null;
    }

    @Override
    public int getUpdateCount() throws SQLException {
      return 0;
    }

    @Override
    public boolean getMoreResults() throws SQLException {
      return false;
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
    }

    @Override
    public int getFetchDirection() throws SQLException {
      return 0;
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
    }

    @Override
    public int getFetchSize() throws SQLException {
      return 0;
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
      return 0;
    }

    @Override
    public int getResultSetType() throws SQLException {
      return 0;
    }

    @Override
    public void addBatch(String sql) throws SQLException {
    }

    @Override
    public void clearBatch() throws SQLException {
    }

    @Override
    public int[] executeBatch() throws SQLException {
      return null;
    }

    @Override
    public Connection getConnection() throws SQLException {
      return null;
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException {
      return false;
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
      return null;
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
      return 0;
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
      return 0;
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
      return 0;
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
      return false;
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
      return false;
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
      return false;
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
      return 0;
    }

    @Override
    public boolean isClosed() throws SQLException {
      return false;
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {
    }

    @Override
    public boolean isPoolable() throws SQLException {
      return false;
    }

    @Override
    public void closeOnCompletion() throws SQLException {
    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
      return false;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
      return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
      return false;
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
      return null;
    }

    @Override
    public int executeUpdate() throws SQLException {
      return 0;
    }

    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
    }

    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
    }

    @Override
    public void setByte(int parameterIndex, byte x) throws SQLException {
    }

    @Override
    public void setShort(int parameterIndex, short x) throws SQLException {
    }

    @Override
    public void setInt(int parameterIndex, int x) throws SQLException {
    }

    @Override
    public void setLong(int parameterIndex, long x) throws SQLException {
    }

    @Override
    public void setFloat(int parameterIndex, float x) throws SQLException {
    }

    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException {
    }

    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
    }

    @Override
    public void setString(int parameterIndex, String x) throws SQLException {
    }

    public Object getParameter(int parameterIndex) {
      return parameters.get(Integer.valueOf(parameterIndex));
    }

    @Override
    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
    }

    @Override
    public void setDate(int parameterIndex, Date x) throws SQLException {
    }

    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException {
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
    }

    @Override
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
    }

    @Override
    public void clearParameters() throws SQLException {
      parameters.clear();
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
    }

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
      parameters.put(Integer.valueOf(parameterIndex), x);
    }

    @Override
    public boolean execute() throws SQLException {
      return false;
    }

    @Override
    public void addBatch() throws SQLException {
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader x, int length) throws SQLException {
      parameters.put(Integer.valueOf(parameterIndex), x);
    }

    @Override
    public void setRef(int parameterIndex, Ref x) throws SQLException {
      parameters.put(Integer.valueOf(parameterIndex), x);
    }

    @Override
    public void setBlob(int parameterIndex, Blob x) throws SQLException {
      parameters.put(Integer.valueOf(parameterIndex), x);
    }

    @Override
    public void setClob(int parameterIndex, Clob x) throws SQLException {
      parameters.put(Integer.valueOf(parameterIndex), x);
    }

    @Override
    public void setArray(int parameterIndex, Array x) throws SQLException {
      parameters.put(Integer.valueOf(parameterIndex), x);
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
      return null;
    }

    @Override
    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
      parameters.put(Integer.valueOf(parameterIndex), x);
    }

    @Override
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
      parameters.put(Integer.valueOf(parameterIndex), x);
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
      parameters.put(Integer.valueOf(parameterIndex), x);
    }

    @Override
    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
    }

    @Override
    public void setURL(int parameterIndex, URL x) throws SQLException {
      parameters.put(Integer.valueOf(parameterIndex), x);
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
      return null;
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
      parameters.put(Integer.valueOf(parameterIndex), x);
    }

    @Override
    public void setNString(int parameterIndex, String x) throws SQLException {
      parameters.put(Integer.valueOf(parameterIndex), x);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader x, long length) throws SQLException {
      parameters.put(Integer.valueOf(parameterIndex), x);
    }

    @Override
    public void setNClob(int parameterIndex, NClob x) throws SQLException {
      parameters.put(Integer.valueOf(parameterIndex), x);
    }

    @Override
    public void setClob(int parameterIndex, Reader x, long length) throws SQLException {
      parameters.put(Integer.valueOf(parameterIndex), x);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream x, long length) throws SQLException {
      parameters.put(Integer.valueOf(parameterIndex), x);
    }

    @Override
    public void setNClob(int parameterIndex, Reader x, long length) throws SQLException {
      parameters.put(Integer.valueOf(parameterIndex), x);
    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML x) throws SQLException {
      parameters.put(Integer.valueOf(parameterIndex), x);
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
      parameters.put(Integer.valueOf(parameterIndex), x);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
      parameters.put(Integer.valueOf(parameterIndex), x);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
      parameters.put(Integer.valueOf(parameterIndex), x);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader x, long length) throws SQLException {
      parameters.put(Integer.valueOf(parameterIndex), x);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
      parameters.put(Integer.valueOf(parameterIndex), x);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
      parameters.put(Integer.valueOf(parameterIndex), x);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader x) throws SQLException {
      parameters.put(Integer.valueOf(parameterIndex), x);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader x) throws SQLException {
      parameters.put(Integer.valueOf(parameterIndex), x);
    }

    @Override
    public void setClob(int parameterIndex, Reader x) throws SQLException {
      parameters.put(Integer.valueOf(parameterIndex), x);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream x) throws SQLException {
      parameters.put(Integer.valueOf(parameterIndex), x);
    }

    @Override
    public void setNClob(int parameterIndex, Reader x) throws SQLException {
      parameters.put(Integer.valueOf(parameterIndex), x);
    }

  }
}
