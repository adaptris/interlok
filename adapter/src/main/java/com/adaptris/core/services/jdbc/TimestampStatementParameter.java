package com.adaptris.core.services.jdbc;

import static org.apache.commons.lang.StringUtils.isBlank;

import java.text.SimpleDateFormat;

import com.adaptris.annotation.GenerateBeanInfo;
import com.adaptris.core.ServiceException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A {@link java.sql.Timestamp} extension to StatementParameter.
 * <p>
 * This ignores the query-class configuration, and always attempts to format the string into a java.sql.Timestamp using the
 * configured date formatter; if {@code convert-null} is true, then empty/blank/whitespace only values will be substituted by
 * {@link System#currentTimeMillis()}.
 * </p>
 * 
 * @config jdbc-timestamp-statement-parameter
 */
@XStreamAlias("jdbc-timestamp-statement-parameter")
@GenerateBeanInfo
public class TimestampStatementParameter extends StatementParameter {
  private String dateFormat;

  protected transient SimpleDateFormat sdf = null;

  public TimestampStatementParameter() {
    super();
    super.setQueryClass(null);
  }

  public TimestampStatementParameter(String query, QueryType type, SimpleDateFormat format) {
    this(query, type, null, format);
  }

  public TimestampStatementParameter(String query, QueryType type, Boolean nullConvert, SimpleDateFormat format) {
    super(query, (String) null, type, nullConvert);
    setDateFormat(format.toPattern());
  }

  /**
   * Set the format of the date.
   *
   * @param format the format of the date that is parse-able by SimpleDateFormat
   */
  public void setDateFormat(String format) {
      dateFormat = format;
      sdf = new SimpleDateFormat(format);
  }

  /**
   * Get the configured Date Format.
   *
   * @return the date format.
   */
  public String getDateFormat() {
    return dateFormat;
  }

  @Override
  public Object convertToQueryClass(Object value) throws ServiceException {
    if (isBlank((String) value) && convertNull()) {
      return new java.sql.Timestamp(System.currentTimeMillis());
    }
    else {
      try {
        return new java.sql.Timestamp(sdf.parse((String) value).getTime());
      }
      catch (Exception e) {
        throw new ServiceException("Failed to convert input String [" + value + "] to type [java.sql.Timestamp]", e);
      }
    }
  }
}
