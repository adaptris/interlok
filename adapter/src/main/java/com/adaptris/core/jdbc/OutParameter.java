package com.adaptris.core.jdbc;

import com.adaptris.core.AdaptrisMessage;


/**
 * This class represents a single OUT parameter for a Stored Procedure.
 * <p>
 * This parameter will extract data from the matching OUT parameter from the Stored Procedure and apply that data into the
 * AdaptrisMessage.
 * </p>
 * 
 * @author Aaron McGrath
 * 
 */
public interface OutParameter extends JdbcParameter {
  
  void applyOutputParam(Object dbValue, AdaptrisMessage msg) throws JdbcParameterException;

}
