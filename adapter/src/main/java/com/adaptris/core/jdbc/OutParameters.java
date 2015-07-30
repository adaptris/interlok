package com.adaptris.core.jdbc;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * This class contains the OUT parameters that a stored procedure will require to be executed.
 * 
 * @config jdbc-out-parameters
 * @author Aaron McGrath
 * 
 */
@XStreamAlias("jdbc-out-parameters")
public class OutParameters extends JdbcParameterList<OutParameter> {

  public OutParameters() {
    parameters = new ArrayList<OutParameter>();
  }
  
  @XStreamImplicit
  private List<OutParameter> parameters;
  
  @Override
  public List<OutParameter> getParameters() {
    return parameters;
  }


}
