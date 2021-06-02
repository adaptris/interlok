package com.adaptris.core.services.conditional.operator;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.services.conditional.Operator;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * This {@link Operator} Test whether a specified value doesn't match with any value from the list
 * </p>
 * <p>
 * Allows you to add multiple values for comparision
 * </p>
 *
 * @config not-in
 * @author raney
 *
 */

@XStreamAlias("not-in")
@AdapterComponent
@ComponentProfile(summary = "Test whether a specified value doesn't match with any value from the list", tag = "conditional,operator")
public class NotIn extends IsIn {

  @Override
  public boolean apply(AdaptrisMessage message, String object) {
    return !(super.apply(message, object));
  }
  
  public String toString() {
    return "is not in " + getValues();
  }
}
