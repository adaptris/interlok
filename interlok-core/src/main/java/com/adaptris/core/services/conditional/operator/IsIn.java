package com.adaptris.core.services.conditional.operator;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.adaptris.core.services.conditional.Operator;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * This {@link Operator} tests whether a specified value matches any value in a list
 * </p>
 * <p>
 * Allows you to add multiple values for comparision
 * </p>
 *
 * @config is-in
 * @author raney
 *
 */

@XStreamAlias("is-in")
@AdapterComponent
@ComponentProfile(summary = "Test whether a specified value matches any value in a list", tag = "conditional,operator")
public class IsIn implements Operator {

  @InputFieldHint
  @XStreamImplicit(itemFieldName = "value")
  @Size (min = 1)
  private List<String> values = new ArrayList<>();

  @Override
  public boolean apply(AdaptrisMessage message, String object) {
    return getValues().contains(message.resolve(object));
  }

  public List<String> getValues() {
    return values;
  }

  public void setValues(List<String> values) {
    this.values =  Args.notNull(values,"values");
  }
  
  public String toString() {
    return "is in " + getValues();
  }
}
