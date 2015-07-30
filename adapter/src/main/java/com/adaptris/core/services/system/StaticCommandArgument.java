package com.adaptris.core.services.system;

import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Returns a fixed value for this command line argument.
 * 
 * @config system-command-static-argument
 * 
 * @author sellidge
 */
@XStreamAlias("system-command-static-argument")
public class StaticCommandArgument implements CommandArgument {

  private String value;

  public StaticCommandArgument() {

  }
  
  public StaticCommandArgument(String arg) {
    this();
    setValue(arg);
  }

  @Override
  public String retrieveValue(AdaptrisMessage msg) {
    return getValue();
  }

  public String getValue() {
    return value;
  }

  /**
   * The value to be returned
   * @param value
   */
  public void setValue(String value) {
    this.value = value;
  }
}
