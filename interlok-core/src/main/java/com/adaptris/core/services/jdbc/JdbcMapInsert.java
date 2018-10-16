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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringUtils;
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

    String() {
      @Override
      Object attemptConvert(String s) {
        return s;
      }

    },
    Integer() {
      @Override
      Object attemptConvert(String s) throws NumberFormatException {
        return java.lang.Integer.valueOf(s);
      }
    },
    Long() {
      @Override
      Object attemptConvert(String s) throws NumberFormatException {
        return java.lang.Long.valueOf(s);
      }
    },
    /**
     * Handles {@link Boolean} via {@link BooleanUtils#toBooleanObject(String)}.
     * 
     */
    Boolean() {
      @Override
      Object attemptConvert(String s) throws NullPointerException {
        boolean b = BooleanUtils.toBooleanObject(s); // commons-lang auto-box NullPointerException
        return java.lang.Boolean.valueOf(b);
      }
    },
    BigInteger() {
      @Override
      Object attemptConvert(String s) throws NumberFormatException {
        return java.math.BigInteger.valueOf(java.lang.Long.valueOf(s));
      }
    },
    BigDecimal() {
      @Override
      Object attemptConvert(String s) throws NumberFormatException {
        return java.math.BigDecimal.valueOf(java.lang.Double.valueOf(s));
      }
    },
    Float() {
      @Override
      Object attemptConvert(String s) throws NumberFormatException {
        return java.lang.Float.valueOf(s);
      }
    },
    Double() {
      @Override
      Object attemptConvert(String s) throws NumberFormatException {
        return java.lang.Double.valueOf(s);
      }
    },
    /** Handles {@link java.sql.Date} via {@link java.sql.Date#valueOf(String)} */
    Date() {
      @Override
      Object attemptConvert(String s) {
        return java.sql.Date.valueOf(s);
      }
    },
    /** Handles {@link java.sql.Timestamp} via {@link java.sql.Timestamp#valueOf(String)} */
    Timestamp() {
      @Override
      Object attemptConvert(String s) {
        return java.sql.Timestamp.valueOf(s);
      }
    },
    /** Handles {@link java.sql.Time} via {@link java.sql.Time#valueOf(String)} */
    Time() {
      @Override
      Object attemptConvert(String s) {
        return java.sql.Time.valueOf(s);

      }
    };

    final Object convert(String s) {
      try {
        return attemptConvert(s);
      }
      catch (Exception quietly) {

      }
      return s;
    }

    abstract Object attemptConvert(String s) throws Exception;

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

  public JdbcMapInsert withTable(String s) {
    setTable(s);
    return this;
  }


  public KeyValuePairSet getFieldMappings() {
    return fieldMappings;
  }

  /**
   * Set the converters for various fields in the map.
   * <p>
   * In the event that the database doesn't auto-convert types (e.g. MySQL will convert {@code 2017-01-01} into a DATE if that is
   * the column type); you can specify the java type that the string should be converted to; if the conversion fails, then it
   * remains a string, if the type is not supported then it is assumed to be a full qualified classname with a String constructor.
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

  public JdbcMapInsert withMappings(KeyValuePairSet s) {
    setFieldMappings(s);
    return this;
  }

  protected Object toObject(String key, String value) {
    Object result = null;
    KeyValuePair kp = fieldMappings().getKeyValuePair(key);
    // There's a mapping (yay?).
    if (kp != null) {
      result = attemptBasicConversion(kp, value);
      if (result == null) {
        result = reflectConversion(kp.getValue(), value);
      }
    }
    return result != null ? result : value;
  }

  private Object attemptBasicConversion(KeyValuePair kp, String value) {
    Object result = null;
    try {
      BasicType type = BasicType.valueOf(kp.getValue());
      result = type.convert(value);
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

  protected void handleInsert(String tableName, Connection conn, Map<String, String> obj) throws ServiceException {
    PreparedStatement insertStmt = null;
    try {
      InsertWrapper inserter = new InsertWrapper(tableName, obj);
      log.trace("INSERT [{}]", inserter.statement);
      insertStmt = inserter.addParams(prepareStatement(conn, inserter.statement), obj);
      insertStmt.executeUpdate();
    }
    catch (SQLException e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
    finally {
      JdbcUtil.closeQuietly(insertStmt);
    }
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

    public String statement() {
      return statement;
    }

    public PreparedStatement addParams(PreparedStatement statement, Map<String, String> obj) throws SQLException {
      int paramIndex = 1;
      statement.clearParameters();
      for (Iterator<String> i = columns.iterator(); i.hasNext(); paramIndex++) {
        String key = i.next();
        statement.setObject(paramIndex, toObject(key, obj.get(key)));
      }
      return statement;
    }
  }

  private static Object reflectConversion(String classname, String value) {
    Object result = null;
    try {
      result = Class.forName(classname).getDeclaredConstructor(new Class[]
      {
          classname.getClass()
      }).newInstance(new Object[]
      {
          value
      });
    }
    catch (Exception e) {
      result = null;
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

  public JdbcMapInsert withColumnBookend(Character c) {
    setColumnBookendCharacter(c);
    return this;
  }

  protected String columnBookend() {
    return StringUtils.defaultIfEmpty(CharUtils.toString(getColumnBookendCharacter()), "");
  }
}
