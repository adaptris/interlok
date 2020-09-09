package com.adaptris.core.services.aggregator;

import java.util.Collection;
import javax.validation.Valid;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.MultiPayloadAdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.Setter;

/**
 * Combine multiple standard Adaptris messages into a single
 * multi-payload Adaptris message.
 *
 * <pre>{@code
 * <split-join-service>
 *   <unique-id>split-join-id</unique-id>
 *   <service class="shared-service">
 *     <lookup-name>for-each-service-list-id</lookup-name>
 *     <unique-id>for-each-service-list-id</unique-id>
 *   </service>
 *   <splitter class="multi-payload-splitter"/>
 *   <aggregator class="multi-payload-aggregator">
 *     <replace-original-message>false</replace-original-message>
 *   </aggregator>
 * </split-join-service>
 * }</pre>
 *
 * @author amanderson
 * @config multi-payload-aggregator
 * @see MultiPayloadAdaptrisMessage
 * @since 3.10
 */
@XStreamAlias("multi-payload-aggregator")
@ComponentProfile(summary = "Combine many Adaptris messages into a single multi-payload message with each payload separate", tag = "multi-payload,aggregator", since = "3.10")
public class MultiPayloadMessageAggregator extends MessageAggregatorImpl {
  private static final transient Logger log =
      LoggerFactory.getLogger(MultiPayloadMessageAggregator.class);

  /**
   * et whether to replace the original multi-payload message payload.
   * <p>
   * If true then the original message will only contain the payloads from the collection of
   * messages, otherwise it will append the collection of messages while maintaining the original
   * message payload; default is true unless otherwise specified
   * </p>
   *
   */
  @Valid
  @AdvancedConfig
  @Getter
  @Setter
  @InputFieldDefault(value = "true")
  private Boolean replaceOriginalMessage;

  private boolean replaceOriginal() {
    return BooleanUtils.toBooleanDefaultIfNull(getReplaceOriginalMessage(), true);
  }

  /**
   * Joins multiple {@link AdaptrisMessage}s into a single MultiPayloadAdaptrisMessage object.
   *
   * @param original The message to insert all the messages into.
   * @param messages The list of messages to join.
   * @throws CoreException Wrapping any other exception
   */
  @Override
  public void joinMessage(AdaptrisMessage original, Collection<AdaptrisMessage> messages)
      throws CoreException {
    aggregate(original, messages);
  }

  @Override
  public void aggregate(AdaptrisMessage original, Iterable<AdaptrisMessage> msgs)
      throws CoreException {

    if (!(original instanceof MultiPayloadAdaptrisMessage)) {
      throw new CoreException("Original not a multi-payload message, cannot merge");
    }
    log.trace("Adding messages to existing message [{}]", original.getUniqueId());
    MultiPayloadAdaptrisMessage multiMessage = (MultiPayloadAdaptrisMessage) original;
    String originalId = multiMessage.getCurrentPayloadId();
    long count = 0;
    for (AdaptrisMessage message : msgs) {
      if (filter(message)) {
        count++;
        log.trace("Adding message payload [{}]", message.getUniqueId());
        multiMessage.addPayload(message.getUniqueId(), message.getPayload());
      }
    }
    if (replaceOriginal()) {
      multiMessage.deletePayload(originalId);
    }
    log.trace("Finished adding {} messages", count);
  }
}
