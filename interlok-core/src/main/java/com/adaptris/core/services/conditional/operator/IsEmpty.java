package com.adaptris.core.services.conditional.operator;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.services.conditional.Condition;
import com.adaptris.core.services.conditional.Operator;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;

/**
 * <p>
 * This {@link Operator} simply tests that the given value evaluates to null or an empty string.
 * </p>
 * <p>
 * The value used in the isEmpty test is the {@link Condition} that this {@link Operator} is
 * configured for; which could be the message payload or a metadata item for example. <br/>
 * </p>
 *
 * @config is-empty
 * @author bklair
 *
 */
@XStreamAlias("is-empty")
@AdapterComponent
@ComponentProfile(summary = "Tests that a value is null or empty", tag = "conditional,operator")
public class IsEmpty implements Operator {

  @InputFieldDefault(value = "false")
  private Boolean ignoreWhitespace;

  @Override
  public boolean apply(AdaptrisMessage message, String object) {
    String contentItem = message.resolve(object);
    if (StringUtils.isEmpty(contentItem)) {
      return true;
    }
    if (ignoreWhitespace() && StringUtils.isBlank(contentItem)) {
      return true;
    }
    return false;
  }

  public Boolean getIgnoreWhitespace() {
    return ignoreWhitespace;
  }

  public void setIgnoreWhitespace(Boolean ignoreWhitespace) {
    this.ignoreWhitespace = ignoreWhitespace;
  }

  private boolean ignoreWhitespace() {
    return BooleanUtils.toBooleanDefaultIfNull(getIgnoreWhitespace(), false);
  }
}
