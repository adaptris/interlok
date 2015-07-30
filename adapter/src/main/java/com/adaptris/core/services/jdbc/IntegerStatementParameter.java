package com.adaptris.core.services.jdbc;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.math.NumberUtils.toInt;

import com.adaptris.core.ServiceException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link Integer} Statement Parameter.
 * 
 * <p>
 * Note that this class pays no heed to the {@link StatementParameter#setQueryClass(String)} setting; if {@code convert-null} is
 * true, then empty/blank/whitespace only values will default to 0.
 * </p>
 * 
 * @config jdbc-integer-statement-parameter
 * @author lchan
 * 
 */
@XStreamAlias("jdbc-integer-statement-parameter")
public class IntegerStatementParameter extends StatementParameter {

  public IntegerStatementParameter() {
    super();
    super.setQueryClass(null);
  }

  public IntegerStatementParameter(String query, QueryType type) {
    this(query, type, null);
  }

  public IntegerStatementParameter(String query, QueryType type, Boolean nullConvert) {
    super(query, (String) null, type, nullConvert);
  }

  @Override
  public Object convertToQueryClass(Object value) throws ServiceException {
    if (isBlank((String) value) && convertNull()) {
      return Integer.valueOf(toInt((String) value));
    }
    else {
      return Integer.valueOf((String) value);
    }
  }
}
