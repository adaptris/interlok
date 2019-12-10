package com.adaptris.core.services.aggregator;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.MultiPayloadAdaptrisMessage;
import com.adaptris.core.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * Combine multiple standard Adaptris messages into a single
 * multi-payload Adaptris message.
 */
public class MultiPayloadMessageAggregator extends MessageAggregatorImpl
{
	private static final transient Logger log = LoggerFactory.getLogger(MultiPayloadMessageAggregator.class);

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
		for (AdaptrisMessage message : messages)
		{
			log.info("Adding message payload [{}]", message.getUniqueId());

			multiMessage.addPayload(message.getUniqueId(), message.getPayload());
		}

		log.info("Finished adding {} messages", messages.size());
	}
}
