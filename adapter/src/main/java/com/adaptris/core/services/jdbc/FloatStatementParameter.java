package com.adaptris.core.services.jdbc;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.math.NumberUtils.toFloat;

import com.adaptris.core.ServiceException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link Float} Statement Parameter.
 * 
 * <p>
 * Note that this class pays no heed to the {@link StatementParameter#setQueryClass(String)} setting; if {@code convert-null} is
 * true, then empty/blank/whitespace only values will default to 0.
 * </p>
 * 
 * @config jdbc-float-statement-parameter
 * @author lchan
 * 
 */
@XStreamAlias("jdbc-float-statement-parameter")
public class FloatStatementParameter extends StatementParameter {

  public FloatStatementParameter() {
    super();
    super.setQueryClass(null);
  }

  public FloatStatementParameter(String query, QueryType type) {
    this(query, type, null);
  }

  public FloatStatementParameter(String query, QueryType type, Boolean nullConvert) {
    super(query, (String) null, type, nullConvert);
  }

  @Override
  public Object convertToQueryClass(Object value) throws ServiceException {
    if (isBlank((String) value) && convertNull()) {
      return Float.valueOf(toFloat((String) value));
    }
    else {
      return Float.valueOf((String) value);
    }
  }
}
