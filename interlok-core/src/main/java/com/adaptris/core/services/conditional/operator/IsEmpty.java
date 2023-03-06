package com.adaptris.core.services.conditional.operator;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.services.conditional.Condition;
import com.adaptris.core.services.conditional.Operator;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

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
@JacksonXmlRootElement(localName = "is-empty")
@XStreamAlias("is-empty")
@AdapterComponent
@ComponentProfile(summary = "Tests that a value is null or empty", tag = "conditional,operator")
public class IsEmpty implements Operator {

  private Boolean ignoreWhitespace;

  @Override
  public boolean apply(AdaptrisMessage message, String object) {
    String contentItem = message.resolve(object);
    if (StringUtils.isEmpty(contentItem)) {
      return true;
    }
    if (isIgnoreWhitespace() && StringUtils.isBlank(contentItem)) {
      return true;
    }
    return false;
  }

  private boolean isIgnoreWhitespace() {
    return BooleanUtils.toBooleanDefaultIfNull(getIgnoreWhitespace(), false);
  }

  public Boolean getIgnoreWhitespace() {
    return ignoreWhitespace;
  }

  public void setIgnoreWhitespace(Boolean ignoreWhitespace) {
    this.ignoreWhitespace = ignoreWhitespace;
  }
  
  public String toString() {
    return "is empty";
  }
}
