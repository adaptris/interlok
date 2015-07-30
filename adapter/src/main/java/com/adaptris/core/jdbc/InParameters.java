package com.adaptris.core.jdbc;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * This class contains the IN parameters that a stored procedure will require to be executed.
 * 
 * @config jdbc-in-parameters
 * @author Aaron McGrath
 * 
 */
@XStreamAlias("jdbc-in-parameters")
public class InParameters extends JdbcParameterList<InParameter> {

  public InParameters() {
    parameters = new ArrayList<InParameter>();
  }
  
  @XStreamImplicit
  private List<InParameter> parameters;
  
  @Override
  public List<InParameter> getParameters() {
    return parameters;
  }

}
