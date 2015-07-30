package com.adaptris.core.services.jdbc;

import static org.apache.commons.lang.StringUtils.isBlank;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import com.adaptris.annotation.GenerateBeanInfo;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link java.sql.Time} extension to StatementParameter.
 * <p>
 * This ignores the query-class configuration, and always attempts to format the string into a java.sql.Time using the configured
 * date formatter; if {@code convert-null} is true, then empty/blank/whitespace only values will be substituted by
 * {@link System#currentTimeMillis()}.
 * </p>
 * 
 * @config jdbc-time-statement-parameter
 * 
 * @see StatementParameter
 */
@XStreamAlias("jdbc-time-statement-parameter")
@GenerateBeanInfo
public class TimeStatementParameter extends TimestampStatementParameter {

  public TimeStatementParameter() {
    super();
    setQueryClass(null);
  }

  public TimeStatementParameter(String query, QueryType type, SimpleDateFormat format) {
    this(query, type, null, format);
  }

  public TimeStatementParameter(String query, QueryType type, Boolean nullConvert, SimpleDateFormat format) {
    super(query, type, nullConvert, format);
  }


  @Override
  public Object convertToQueryClass(Object value) throws ServiceException {
    if (isBlank((String) value) && convertNull()) {
      return new java.sql.Time(System.currentTimeMillis());
    }
    else {
      try {
        return new java.sql.Time(sdf.parse((String) value).getTime());
      }
      catch (Exception e) {
        throw new ServiceException("Failed to convert input String [" + value + "] to type [java.sql.Time]", e);
      }
    }
  }
  
  @Override
  public void apply(int parameterIndex, PreparedStatement statement, AdaptrisMessage msg) throws SQLException, ServiceException {
    statement.setTime(parameterIndex, (java.sql.Time) this.convertToQueryClass(this.getQueryValue(msg)));
  }
}
