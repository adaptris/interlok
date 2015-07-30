package com.adaptris.core.jdbc;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.jdbc.ParameterValueType;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Stored Procedure parameter implementation
 * <p>
 * This implementation can only be used as an IN parameter; the entire payload (as a byte[]) is the parameter.
 * </p>
 * <p>
 * Additionally you will set one or both of "name" and/or "order". "name" will map this parameter to a Stored Procedure parameter
 * using the Stored Procedures method signature. "order" will map this parameter according to the parameter number using the Stored
 * Procedures method signature. Note that the "order" starts from 1 and not 0, so the first parameter would be order 1. You will
 * also need to set the data type of the parameter; you may use any of the string types defined in {@link ParameterValueType}
 * </p>
 * 
 * @config jdbc-byte-payload-parameter
 * @author Aaron McGrath
 * 
 */
@XStreamAlias("jdbc-byte-payload-parameter")
public class JdbcBytePayloadParameter extends AbstractParameter {


  @Override
  public Object applyInputParam(AdaptrisMessage msg) throws JdbcParameterException {
    return msg.getPayload();
  }

  @Override
  public void applyOutputParam(Object dbValue, AdaptrisMessage msg) throws JdbcParameterException {
    throw new JdbcParameterException(this.getClass().getName() + " cannot be applied to Jdbc output parameters.");
  }
}
