package com.adaptris.core.jdbc;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * This class contains the INOUT parameters that a stored procedure will require to be executed.
 * 
 * @config jdbc-in-out-parameters
 * @author Aaron McGrath
 * 
 */
@XStreamAlias("jdbc-in-out-parameters")
public class InOutParameters extends JdbcParameterList<InOutParameter> {

  public InOutParameters() {
    parameters = new ArrayList<InOutParameter>();
  }
  
  @XStreamImplicit
  private List<InOutParameter> parameters;
  
  @Override
  public List<InOutParameter> getParameters() {
    return parameters;
  }


}
