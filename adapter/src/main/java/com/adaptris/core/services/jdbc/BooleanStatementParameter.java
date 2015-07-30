package com.adaptris.core.services.jdbc;

import static org.apache.commons.lang.BooleanUtils.toBoolean;

import com.adaptris.core.ServiceException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link Boolean} Statement Parameter.
 * 
 * <p>
 * Note that this class pays no heed to the {@link StatementParameter#setQueryClass(String)} setting. Additionally,
 * {@link StatementParameter#setConvertNull(Boolean)} has no meaning; null/empty/whitespace values are implicitly false.
 * </p>
 * 
 * @config jdbc-boolean-statement-parameter
 * @author lchan
 * 
 */
@XStreamAlias("jdbc-boolean-statement-parameter")
public class BooleanStatementParameter extends StatementParameter {

  public BooleanStatementParameter() {
    super();
    super.setQueryClass(null);
    super.setConvertNull(null);
  }

  public BooleanStatementParameter(String query, QueryType type) {
    super(query, (String) null, type);
  }

  @Override
  public Object convertToQueryClass(Object value) throws ServiceException {
    return Boolean.valueOf(toBoolean((String)value));
  }
}
