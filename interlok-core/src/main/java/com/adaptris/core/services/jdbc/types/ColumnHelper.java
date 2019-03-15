package com.adaptris.core.services.jdbc.types;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.adaptris.jdbc.JdbcResultRow;
import com.adaptris.jdbc.ParameterValueType;

public abstract class ColumnHelper {
  private static final Map<ParameterValueType, ColumnTranslator> AUTOMATIC_COLUMN_TRANSLATORS;
  private static final ColumnTranslator DEFAULT_TRANSLATOR = new StringColumnTranslator();

  static {
    Map<ParameterValueType, ColumnTranslator> translators = new HashMap<>();
    translators.put(ParameterValueType.BLOB, new BlobColumnTranslator());
    translators.put(ParameterValueType.BOOLEAN, new BooleanColumnTranslator());
    translators.put(ParameterValueType.CLOB, new ClobColumnTranslator());
    translators.put(ParameterValueType.DATE, new DateColumnTranslator());
    translators.put(ParameterValueType.DOUBLE, new DoubleColumnTranslator());
    translators.put(ParameterValueType.FLOAT, new FloatColumnTranslator());
    translators.put(ParameterValueType.INTEGER, new IntegerColumnTranslator());
    translators.put(ParameterValueType.TIME, new TimeColumnTranslator());
    translators.put(ParameterValueType.TIMESTAMP, new TimestampColumnTranslator());
    AUTOMATIC_COLUMN_TRANSLATORS = Collections.unmodifiableMap(translators);
  }

  /**
   * Attempt to translate a column into a string using a best guess against the type.
   *
   */
  public static String translate(JdbcResultRow rs, int column) throws Exception {
    ColumnTranslator ct =
        AUTOMATIC_COLUMN_TRANSLATORS.getOrDefault(rs.getFieldType(column), DEFAULT_TRANSLATOR);
    return ct.translate(rs, column);
  }

  /**
   * Translate a column into a string using toString().
   *
   */
  public static String toString(JdbcResultRow rs, int column) throws Exception {
    String value = null;
    Object o = rs.getFieldValue(column);
    if (o instanceof byte[]) {
      value = new String((byte[]) o);
    } else {
      value = o.toString();
    }
    return value;
  }
}
