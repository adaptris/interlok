package com.adaptris.core.services.jdbc.types;

import java.util.Formatter;

/**
 * Abstract column Translator implementation for handling column types that might need to have formatting applied to it.
 *
 * @author lchan
 *
 */
public abstract class FormattableColumnTranslator implements ColumnTranslator {

  private String format = null;

  public FormattableColumnTranslator() {
  }

  public FormattableColumnTranslator(String format) {
    this();
    setFormat(format);
  }

  public String getFormat() {
    return format;
  }

  /**
   * Format the object using {@link String#format(String, Object...)} if there is a format to apply otherwise
   * {@link String#valueOf(Object)}.
   *
   * @param d the object to format.
   */
  protected String toString(Object d) {
    if (getFormat() != null) {
      return String.format(getFormat(), d);
    }
    return String.valueOf(d);
  }

  /**
   * /** Set the format to be used to "format" the object.
   * <p>
   * If non-null then this will be used to format the column value; it relies exclusively on {@link Formatter} via
   * {@link String#format(String, Object...)} to format it into something meaningful. Note that no validation is performed on the
   * output or format; as a result runtime exceptions may be thrown.
   * </p>
   * 
   * @see Formatter
   * @param s the format to apply.
   */
  public void setFormat(String s) {
    format = s;
  }

}
