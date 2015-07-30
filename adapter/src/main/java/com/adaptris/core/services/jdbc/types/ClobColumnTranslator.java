package com.adaptris.core.services.jdbc.types;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.copy;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.sql.Clob;
import java.sql.SQLException;

import com.adaptris.jdbc.JdbcResultRow;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Column Translator implementation for handling CLOB types
 * 
 * @config jdbc-type-clob-column-translator
 * 
 * @author lchan
 * 
 */
@XStreamAlias("jdbc-type-clob-column-translator")
public class ClobColumnTranslator implements ColumnTranslator {

  public ClobColumnTranslator() {

  }

  @Override
  public String translate(JdbcResultRow rs, int column) throws SQLException, IOException {
    return toString((Clob) rs.getFieldValue(column));
  }

  @Override
  public String translate(JdbcResultRow rs, String columnName) throws SQLException, IOException {
    return toString((Clob) rs.getFieldValue(columnName));
  }

  private String toString(Clob clob) throws SQLException, IOException {
    StringWriter output = new StringWriter();
    Reader input = clob.getCharacterStream();
    try {
      copy(input, output);
    }
    finally {
      closeQuietly(input);
      closeQuietly(output);
    }
    return output.toString();
  }
}
