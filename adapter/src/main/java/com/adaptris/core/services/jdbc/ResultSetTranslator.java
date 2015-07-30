package com.adaptris.core.services.jdbc;

import java.sql.SQLException;

import com.adaptris.core.AdaptrisComponent;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.jdbc.JdbcResult;

/**
 * Interface used to format output from a {@link JdbcDataQueryService}
 *
 * @author lchan
 *
 */
public interface ResultSetTranslator extends AdaptrisComponent {

  /**
   * Translate the contents of the result set into the AdaptrisMessage object.
   * 
   * @param source the result set from a database query executed by
   *          {@link JdbcDataQueryService}
   * @param target the adaptris message
   * @throws SQLException on errors accessing the result set.
   * @throws ServiceException wrapping any other exception
   */
  void translate(JdbcResult source, AdaptrisMessage target) throws SQLException, ServiceException;

}
