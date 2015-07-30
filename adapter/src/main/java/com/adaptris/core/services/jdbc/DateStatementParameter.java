package com.adaptris.core.services.jdbc;

import static org.apache.commons.lang.StringUtils.isBlank;

import java.text.SimpleDateFormat;

import com.adaptris.annotation.GenerateBeanInfo;
import com.adaptris.core.ServiceException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A {@link java.sql.Date} extension to StatementParameter.
 * <p>
 * This ignores the query-class configuration, and always attempts to format the string into a java.sql.Date using the configured
 * date formatter; if {@code convert-null} is true, then empty/blank/whitespace only values will be substituted by
 * {@link System#currentTimeMillis()}.
 * </p>
 * 
 * @config jdbc-date-statement-parameter
 * 
 * 
 * @author sellidge
 */
@XStreamAlias("jdbc-date-statement-parameter")
@GenerateBeanInfo
public class DateStatementParameter extends TimestampStatementParameter {

  public DateStatementParameter() {
    super();
    super.setQueryClass(null);
  }

  public DateStatementParameter(String query, QueryType type, SimpleDateFormat format) {
    this(query, type, null, format);
  }

  public DateStatementParameter(String query, QueryType type, Boolean nullConvert, SimpleDateFormat format) {
    super(query, type, nullConvert, format);
  }


  @Override
  public Object convertToQueryClass(Object value) throws ServiceException {
    if (isBlank((String) value) && convertNull()) {
      return new java.sql.Date(System.currentTimeMillis());
    }
    else {
      try {
        return new java.sql.Date(sdf.parse((String) value).getTime());
      }
      catch (Exception e) {
        throw new ServiceException("Failed to convert input String [" + value
            + "] to type [java.sql.Date]", e);
      }
    }
  }
}
