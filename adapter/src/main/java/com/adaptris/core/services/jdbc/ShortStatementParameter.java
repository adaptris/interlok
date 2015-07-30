package com.adaptris.core.services.jdbc;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.math.NumberUtils.toShort;

import com.adaptris.core.ServiceException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link Short} Statement Parameter.
 * 
 * <p>
 * Note that this class pays no heed to the {@link StatementParameter#setQueryClass(String)} setting; if {@code convert-null} is
 * true, then empty/blank/whitespace only values will default to 0.
 * </p>
 * 
 * @config jdbc-short-statement-parameter
 * @author lchan
 * 
 */
@XStreamAlias("jdbc-short-statement-parameter")
public class ShortStatementParameter extends StatementParameter {

  public ShortStatementParameter() {
    super();
    super.setQueryClass(null);
  }


  public ShortStatementParameter(String query, QueryType type) {
    this(query, type, null);
  }

  public ShortStatementParameter(String query, QueryType type, Boolean nullConvert) {
    super(query, (String) null, type, nullConvert);
  }

  @Override
  public Object convertToQueryClass(Object value) throws ServiceException {
    if (isBlank((String) value) && convertNull()) {
      return Short.valueOf(toShort((String) value));
    }
    else {
      return Short.valueOf((String) value);
    }
  }
}
