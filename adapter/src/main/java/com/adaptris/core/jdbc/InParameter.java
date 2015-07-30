package com.adaptris.core.jdbc;

import com.adaptris.core.AdaptrisMessage;

/**
 * Represents a single IN parameter for a Stored Procedure.
 * <p>
 * This parameter will extract data from the AdaptrisMessage and will pass that data as in IN parameter to the Stored Procedure.
 * </p>
 *
 * @author Aaron McGrath
 *
 */
public interface InParameter extends JdbcParameter {

  Object applyInputParam(AdaptrisMessage msg) throws JdbcParameterException;
  
}
