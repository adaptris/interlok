package com.adaptris.core.services.jdbc;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.math.NumberUtils.toLong;

import com.adaptris.core.ServiceException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link Long} Statement Parameter.
 * 
 * <p>
 * Note that this class pays no heed to the {@link StatementParameter#setQueryClass(String)} setting; if {@code convert-null} is
 * true, then empty/blank/whitespace only values will default to 0.
 * </p>
 * 
 * @config jdbc-long-statement-parameter
 * @author lchan
 * 
 */
@XStreamAlias("jdbc-long-statement-parameter")
public class LongStatementParameter extends StatementParameter {

  public LongStatementParameter() {
    super();
    super.setQueryClass(null);
  }

  public LongStatementParameter(String query, QueryType type) {
    this(query, type, null);
  }

  public LongStatementParameter(String query, QueryType type, Boolean nullConvert) {
    super(query, (String) null, type, nullConvert);
  }

  @Override
  public Object convertToQueryClass(Object value) throws ServiceException {
    if (isBlank((String) value) && convertNull()) {
      return Long.valueOf(toLong((String) value));
    }
    else {
      return Long.valueOf((String) value);
    }
  }
}
