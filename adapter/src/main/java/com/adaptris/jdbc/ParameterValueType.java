package com.adaptris.jdbc;

import java.sql.Types;

/**
 * This class represents a list of data types that may be used as Stored Procedure parameter IN, OUT and INOUT parameter values.
 * 
 * <p>
 * It just maps {@link java.sql.Types} as a friendly enum
 * </p>
 * 
 * @author Aaron McGrath
 * 
 */

public enum ParameterValueType {

  BIT(Types.BIT), TINYINT(Types.TINYINT), SMALLINT(Types.SMALLINT), INTEGER(Types.INTEGER), BIGINT(Types.BIGINT), FLOAT(Types.FLOAT), 
  REAL(Types.REAL), DOUBLE(Types.DOUBLE), NUMERIC(Types.NUMERIC), DECIMAL(Types.DECIMAL), CHAR(Types.CHAR), VARCHAR(Types.VARCHAR), 
  LONGVARCHAR(Types.LONGNVARCHAR), DATE(Types.DATE), TIME(Types.TIME), TIMESTAMP(Types.TIMESTAMP), BINARY(Types.BINARY), 
  VARBINARY(Types.VARBINARY), LONGVARBINARY(Types.LONGVARBINARY), NULL(Types.NULL), OTHER(Types.OTHER), 
  JAVA_OBJECT(Types.JAVA_OBJECT), DISTINCT(Types.DISTINCT), STRUCT(Types.STRUCT), ARRAY(Types.ARRAY), BLOB(Types.BLOB), 
  CLOB(Types.CLOB), REF(Types.REF), DATALINK(Types.DATALINK), BOOLEAN(Types.BOOLEAN), ROWID(Types.ROWID), NCHAR(Types.NCHAR), 
  NVARCHAR(Types.NVARCHAR), LONGNVARCHAR(Types.LONGNVARCHAR), NCLOB(Types.NCLOB), SQLXML(Types.SQLXML);

  private int value;

  private ParameterValueType(int c) {
    value = c;
  }

  public int getValue() {
    return value;
  }

}
