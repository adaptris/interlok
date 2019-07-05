/*
 * Copyright 2017 Adaptris Ltd.
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

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.CharUtils;
import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.jdbc.JdbcService;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.JdbcUtil;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

/**
 * 
 * Base behaviour of inserting Objects directly into a db.
 *
 */
public abstract class JdbcMapInsert extends JdbcService {

  private static final KeyValuePairSet EMPTY_SET = new KeyValuePairSet();

  /**
   * Handles simple type conversions for the fields in the map that needs to be inserted into the DB.
   *
   */
  public static enum BasicType {

    /**
     * @see PreparedStatement#setString(int, String)
     * 
     */
    String() {
      @Override
      StatementParam wrap(final java.lang.String s) {
        return (i, p) -> { p.setString(i, s);}; 
      }

    },
    /**
     * @see PreparedStatement#setInt(int, int)
     * 
     */
    Integer() {
      @Override
      StatementParam wrap(final java.lang.String s) {
        return (i, p) -> {
          p.setInt(i, java.lang.Integer.parseInt(s));
        };
      }
    },
    /**
     * @see PreparedStatement#setLong(int, long)
     * 
     */
    Long() {
      @Override
      StatementParam wrap(final java.lang.String s) {
        return (i, p) -> {
          p.setLong(i, java.lang.Long.parseLong(s));
        };
      }
    },
    /**
     * Converts to a boolean value via {@link BooleanUtils#toBooleanObject(String)}.
     * 
     * @see PreparedStatement#setBoolean(int, boolean)
     */
    Boolean() {
      @Override
      StatementParam wrap(final java.lang.String s) {        
        return (i, p) -> { p.setBoolean(i, BooleanUtils.toBooleanObject(s));}; 
      }
    },
    /**
     * There doesn't appear to be an equivalent JDBC method for setting a {@code BigInteger} on a prepared statement so this uses
     * {@link PreparedStatement#setObject(int, Object)} so behaviour will largely depend on the provider.
     * 
     */
    BigInteger() {
      @Override
      StatementParam wrap(final java.lang.String s) {
        return (i, p) -> {
          p.setObject(i, java.math.BigInteger.valueOf(java.lang.Long.parseLong(s)));
        };
      }
    },
    /**
     * @see PreparedStatement#setBigDecimal(int, java.math.BigDecimal)
     * 
     */
    BigDecimal() {
      @Override
      StatementParam wrap(final java.lang.String s) {
        return (i, p) -> {
          p.setBigDecimal(i, java.math.BigDecimal.valueOf(java.lang.Double.parseDouble(s)));
        };
      }
    },
    /**
     * @see PreparedStatement#setFloat(int, float)
     * 
     */
    Float() {
      @Override
      StatementParam wrap(final java.lang.String s) {
        return (i, p) -> {
          p.setFloat(i, java.lang.Float.parseFloat(s));
        };
      }
    },
    /**
     * @see PreparedStatement#setDouble(int, double)
     * 
     */
    Double() {
      @Override
      StatementParam wrap(final java.lang.String s) {
        return (i, p) -> {
          p.setDouble(i, java.lang.Double.parseDouble(s));
        };
      }
    },
    /**
     * Converts to {@link java.sql.Date} via {@link java.sql.Date#valueOf(String)}
     * 
     * @see PreparedStatement#setDate(int, java.sql.Date)
     */
    Date() {
      @Override
      StatementParam wrap(final String s) {
        return (i, p) -> { p.setDate(i, java.sql.Date.valueOf(s));}; 
      }
    },
    /**
     * Converts to {@link java.sql.Timestamp} via {@link java.sql.Timestamp#valueOf(String)}.
     * 
     * @see PreparedStatement#setTimestamp(int, java.sql.Timestamp)
     */
    Timestamp() {      
      @Override
      StatementParam wrap(final String s) {
        return (i, p) -> { p.setTimestamp(i, java.sql.Timestamp.valueOf(s));}; 
      }
    },
    /**
     * Converts to {@link java.sql.Time} via {@link java.sql.Time#valueOf(String)}.
     * 
     * @see PreparedStatement#setTime(int, java.sql.Time)
     */
    Time() {
     @Override
      StatementParam wrap(final String s) {
        return (i, p) -> { p.setTime(i, java.sql.Time.valueOf(s));}; 
      }
    };
    abstract StatementParam wrap(String s);
  }

  @NotBlank
  @InputFieldHint(expression = true)
  private String table;
  @AdvancedConfig
  @Valid
  private KeyValuePairSet fieldMappings;

  @InputFieldDefault(value = "")
  @AdvancedConfig
  private Character columnBookendCharacter;

  @InputFieldDefault(value = "")
  private String rowsAffectedMetadataKey;

  public JdbcMapInsert() {
    super();
  }

  /**
   * @return the table
   */
  public String getTable() {
    return table;
  }

  /**
   * @param s the table to insert on.
   */
  public void setTable(String s) {
    this.table = Args.notBlank(s, "table");
  }

  protected String table(AdaptrisMessage msg) {
    return msg.resolve(getTable());
  }

  public <T extends JdbcMapInsert> T withTable(String s) {
    setTable(s);
    return (T) this;
  }


  public KeyValuePairSet getFieldMappings() {
    return fieldMappings;
  }

  /**
   * Set the converters for various fields in the map.
   * <p>
   * In the event that the database doesn't auto-convert types (e.g. MySQL will convert {@code 2017-01-01} into a DATE if that is
   * the column type); you can specify the java type that the string should be converted to; if the type cannot be handled
   * automagically then it is assumed to be a fully qualified classname with a String constructor.
   * </p>
   * 
   * @param mappings the key is the key in the map (e.g. the JSON fieldname), the value is the {@link BasicType} that we should
   *          attempt to convert to
   * @see BasicType
   */
  public void setFieldMappings(KeyValuePairSet mappings) {
    this.fieldMappings = mappings;
  }

  private KeyValuePairSet fieldMappings() {
    return getFieldMappings() != null ? getFieldMappings() : EMPTY_SET;
  }

  public <T extends JdbcMapInsert> T withMappings(KeyValuePairSet s) {
    setFieldMappings(s);
    return (T) this;
  }

  protected StatementParam buildStatementParam(String key, final String value) {
    KeyValuePair kp = fieldMappings().getKeyValuePair(key);
    StatementParam result = (i, p) -> { p.setString(i, value); }; 
    if (kp != null) {
      result = basicWrapper(kp, value);
      if (result == null) {
        result = reflectConversion(kp.getValue(), value);
      }
    }
    return result;
  }
 
  private StatementParam basicWrapper(KeyValuePair kp, String value) {
    StatementParam result = null;
    try {
      BasicType type = BasicType.valueOf(kp.getValue());
      result = type.wrap(value);
    }
    catch (IllegalArgumentException | NullPointerException e) {
      result = null;
    }
    return result;
  }

  @Override
  protected void closeJdbcService() {
  }

  @Override
  protected void initJdbcService() throws CoreException {
    try {
      Args.notBlank(getTable(), "table-name");
    }
    catch (IllegalArgumentException e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  @Override
  protected void prepareService() throws CoreException {
  }

  @Override
  protected void startService() throws CoreException {
  }

  @Override
  protected void stopService() {
  }

  protected int handleInsert(String tableName, Connection conn, Map<String, String> obj) throws ServiceException {
    PreparedStatement insertStmt = null;
    int rowsAffected = 0;
    try {
      InsertWrapper inserter = new InsertWrapper(tableName, obj);
      log.trace("INSERT [{}]", inserter.statement);
      insertStmt = inserter.addParams(prepareStatement(conn, inserter.statement), obj);
      rowsAffected = insertStmt.executeUpdate();
    }
    catch (SQLException e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
    finally {
      JdbcUtil.closeQuietly(insertStmt);
    }
    return rowsAffected;
  }

  protected AdaptrisMessage addUpdatedMetadata(int count, AdaptrisMessage msg) {
    if (!isBlank(getRowsAffectedMetadataKey())) {
      msg.addMessageHeader(getRowsAffectedMetadataKey(), String.valueOf(count));
    }
    return msg;
  }

  public interface StatementWrapper {
    String statement();

    PreparedStatement addParams(PreparedStatement statement, Map<String, String> object) throws SQLException;
  }

  public class InsertWrapper implements StatementWrapper {
    private List<String> columns;
    private String statement;
    public InsertWrapper(String tablename, Map<String, String> json) {
      columns = new ArrayList<>(json.keySet());
      statement = String.format("INSERT into %s (%s) VALUES (%s)", tablename, createString(true), createString(false));
    }

    private String createString(boolean columnsNotQuestionMarks) {
      StringBuilder sb = new StringBuilder();
      String bookend = columnBookend();
      for (Iterator<String> i = columns.iterator(); i.hasNext();) {
        String s = i.next();
        if (columnsNotQuestionMarks) {
          sb.append(bookend).append(s).append(bookend);
        }
        else {
          sb.append("?");
        }
        if (i.hasNext()) {
          sb.append(",");
        }
      }
      return sb.toString();
    }

    @Override
    public String statement() {
      return statement;
    }

    @Override
    public PreparedStatement addParams(PreparedStatement statement, Map<String, String> obj) throws SQLException {
      int paramIndex = 1;
      statement.clearParameters();
      for (Iterator<String> i = columns.iterator(); i.hasNext(); paramIndex++) {
        String key = i.next();
        buildStatementParam(key, obj.get(key)).apply(paramIndex, statement);
      }
      return statement;
    }
  }

  private static StatementParam reflectConversion(String classname, final String value) {
    StatementParam result = null;
    try {
      final Object o  = Class.forName(classname).getDeclaredConstructor(new Class[]
      {
          String.class
      }).newInstance(new Object[]
      {
          value
      });
      return (i, p) -> { p.setObject(i,  o);};
    }
    catch (Exception e) {
      result = (i, p) -> { p.setString(i,  value);};
    }
    return result;
  }

  public Character getColumnBookendCharacter() {
    return columnBookendCharacter;
  }

  /**
   * Set the character used to bookend the column names.
   * <p>
   * Sometimes you may need to bookend the column names with something like a {@code `} because the names are in fact reserved
   * words. Specify this as required.
   * </p>
   * 
   * @param c default is null (or no book-ending).
   */
  public void setColumnBookendCharacter(Character c) {
    this.columnBookendCharacter = c;
  }

  public <T extends JdbcMapInsert> T withColumnBookend(Character c) {
    setColumnBookendCharacter(c);
    return (T) this;
  }

  protected String columnBookend() {
    return defaultIfEmpty(CharUtils.toString(getColumnBookendCharacter()), "");
  }
  

  public String getRowsAffectedMetadataKey() {
    return rowsAffectedMetadataKey;
  }

  /**
   * Set the metadata that will contain the number of rows inserted/updated by this service.
   * 
   * @param key defaults to the empty string, and if not set, no metadata will be set.
   * @since 3.9.0
   */
  public void setRowsAffectedMetadataKey(String key) {
    this.rowsAffectedMetadataKey = key;
  }

  public <T extends JdbcMapInsert> T withRowsAffectedMetadataKey(String s) {
    setRowsAffectedMetadataKey(s);
    return (T) this;
  }

  @FunctionalInterface
  protected interface StatementParam {
    void apply(int index, PreparedStatement statement) throws SQLException;
  }

}
