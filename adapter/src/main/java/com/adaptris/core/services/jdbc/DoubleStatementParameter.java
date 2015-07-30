package com.adaptris.core.services.jdbc;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.math.NumberUtils.toDouble;

import com.adaptris.core.ServiceException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link Double} Statement Parameter.
 * 
 * <p>
 * Note that this class pays no heed to the {@link StatementParameter#setQueryClass(String)} setting; if {@code convert-null} is
 * true, then empty/blank/whitespace only values will default to 0.
 * </p>
 * 
 * @config jdbc-double-statement-parameter
 * @author lchan
 * 
 */
@XStreamAlias("jdbc-double-statement-parameter")
public class DoubleStatementParameter extends StatementParameter {

  public DoubleStatementParameter() {
    super();
    super.setQueryClass(null);
  }

  public DoubleStatementParameter(String query, QueryType type) {
    this(query, type, null);
  }

  public DoubleStatementParameter(String query, QueryType type, Boolean nullConvert) {
    super(query, (String) null, type, nullConvert);
  }

  @Override
  public Object convertToQueryClass(Object value) throws ServiceException {
    if (isBlank((String) value) && convertNull()) {
      return Double.valueOf(toDouble((String) value));
    }
    else {
      return Double.valueOf((String) value);
    }
  }
}
