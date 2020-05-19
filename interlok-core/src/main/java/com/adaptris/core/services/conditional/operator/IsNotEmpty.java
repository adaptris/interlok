package com.adaptris.core.services.conditional.operator;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.services.conditional.Condition;
import com.adaptris.core.services.conditional.Operator;
import com.thoughtworks.xstream.annotations.XStreamAlias;


/**
 * <p>
 * This {@link Operator} simply tests that the given value does not evaluate to null or an empty string.
 * </p>
 * <p>
 * The value used in the isNotEmpty test is the {@link Condition} that this {@link Operator} is
 * configured for; which could be the message payload or a metadata item for example. <br/>
 * </p>
 *
 * @config is-not-empty
 * @author raney
 *
 */
@XStreamAlias("is-not-empty")
@AdapterComponent
@ComponentProfile(summary = "Tests that a value is not null or not empty", tag = "conditional,operator")
public class IsNotEmpty extends IsEmpty {

  @Override
  public boolean apply(AdaptrisMessage message, String object) {
    return !(super.apply(message, object));
  }
}