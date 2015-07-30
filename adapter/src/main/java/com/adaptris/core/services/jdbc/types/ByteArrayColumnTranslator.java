package com.adaptris.core.services.jdbc.types;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.copy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;

import com.adaptris.jdbc.JdbcResultRow;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Column Translator implementation for handling byte array types.
 * <p>
 * Note that this is a largely redundant translator and is only included for completeness; the column types that will be used are
 * generally going to be BLOB or CLOB; I wouldn't really expect to see many types that have to be explicitly treated as a byte array
 * </p>
 * 
 * @config jdbc-type-byte-array-column-translator
 * 
 * @author lchan
 * 
 */
@XStreamAlias("jdbc-type-byte-array-column-translator")
public class ByteArrayColumnTranslator implements ColumnTranslator {

  private String characterEncoding;

  public ByteArrayColumnTranslator() {
  }

  @Override
  public String translate(JdbcResultRow rs, int column) throws SQLException, IOException {
    return toString((byte[]) rs.getFieldValue(column));
  }

  @Override
  public String translate(JdbcResultRow rs, String columnName) throws SQLException, IOException {
    return toString((byte[]) rs.getFieldValue(columnName));
  }

  private String toString(byte[] bytes) throws SQLException, IOException {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    ByteArrayInputStream input = new ByteArrayInputStream(bytes);
    try {
      copy(input, output);
    }
    finally {
      closeQuietly(input);
      closeQuietly(output);
    }
    return getCharacterEncoding() != null ? output.toString(getCharacterEncoding()) : output.toString();
  }

  public String getCharacterEncoding() {
    return characterEncoding;
  }

  /**
   * Set the character encoding used to convert the column into a String.
   *
   * @param charEnc the character encoding, if null then the platform encoding is assumed.
   */
  public void setCharacterEncoding(String charEnc) {
    characterEncoding = charEnc;
  }
}
