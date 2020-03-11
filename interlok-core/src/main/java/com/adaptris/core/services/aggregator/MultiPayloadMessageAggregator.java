package com.adaptris.core.services.aggregator;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.MultiPayloadAdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Collection;

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
public class MultiPayloadMessageAggregator extends MessageAggregatorImpl
{
	private static final transient Logger log = LoggerFactory.getLogger(MultiPayloadMessageAggregator.class);

	@NotNull
	@Valid
	@AdvancedConfig
	private Boolean replaceOriginalMessage = true;

	/**
	 * Set whether to replace the original multi-payload message
	 * payload. If true then the original message will only
	 * contain the payloads from the collection of messages,
	 * otherwise it will append the collection of messages while
	 * maintaining the original message payload.
	 *
	 * @param replaceOriginalMessage Whether to replace the original message payload.
	 */
	public void setReplaceOriginalMessage(Boolean replaceOriginalMessage)
	{
		this.replaceOriginalMessage = Args.notNull(replaceOriginalMessage, "replaceOriginalMessage");
	}

	/**
	 * Get whether to replace the original multi-payload message
	 * payload. If true then the original message will only
	 * contain the payloads from the collection of messages,
	 * otherwise it will append the collection of messages while
	 * maintaining the original message payload.
	 *
	 * @return Whether to replace the original message payload.
	 */
	public Boolean getReplaceOriginalMessage()
	{
		return replaceOriginalMessage;
	}

	/**
	 * Joins multiple {@link AdaptrisMessage}s into a single MultiPayloadAdaptrisMessage object.
	 *
	 * @param original  The message to insert all the messages into.
	 * @param messages The list of messages to join.
	 * @throws CoreException Wrapping any other exception
	 */
	@Override
	public void joinMessage(AdaptrisMessage original, Collection<AdaptrisMessage> messages) throws CoreException
	{
		if (!(original instanceof MultiPayloadAdaptrisMessage))
		{
			throw new ServiceException("Multi-payload message aggregator cannot merge multiple messages into a message that isn't a multi-payload adaptris message!");
		}

		log.info("Adding messages to existing message [{}]", original.getUniqueId());

		MultiPayloadAdaptrisMessage multiMessage = (MultiPayloadAdaptrisMessage)original;
		String originalId = multiMessage.getCurrentPayloadId();
		for (AdaptrisMessage message : messages)
		{
			log.info("Adding message payload [{}]", message.getUniqueId());

			multiMessage.addPayload(message.getUniqueId(), message.getPayload());
		}
		if (replaceOriginalMessage)
		{
			multiMessage.deletePayload(originalId);
		}

		log.info("Finished adding {} messages", messages.size());
	}
}
