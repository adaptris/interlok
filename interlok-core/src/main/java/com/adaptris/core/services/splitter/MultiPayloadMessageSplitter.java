package com.adaptris.core.services.splitter;

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.MultiPayloadAdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Split a multi-payload Adaptris message into its various payloads
 * and return a list of standard Adaptris messages. This will copy all
 * additional metadata; all split messages will have identical metadata
 * to the original.
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
 * @config multi-payload-splitter
 * @see MultiPayloadAdaptrisMessage
 * @since 3.10
 */
@XStreamAlias("multi-payload-splitter")
@ComponentProfile(summary = "Split a multi-payload message so that each payload can be serviced independently", tag = "multi-payload,splitter", since="3.10")
public class MultiPayloadMessageSplitter extends MessageSplitterImp
{
	private static final transient Logger log = LoggerFactory.getLogger(MultiPayloadMessageSplitter.class.getName());

	@Override
	public Iterable<AdaptrisMessage> splitMessage(AdaptrisMessage msg)
	{
		List<AdaptrisMessage> splitMessages = new ArrayList<>();

		if (!(msg instanceof MultiPayloadAdaptrisMessage))
		{
			log.warn("Message [{}] is not a multi-payload message!", msg.getUniqueId());
			splitMessages.add(msg);
		}
		else
		{
			MultiPayloadAdaptrisMessage message = (MultiPayloadAdaptrisMessage)msg;
			for (String id : message.getPayloadIDs())
			{
				message.switchPayload(id);
				try
				{
					AdaptrisMessage splitMessage = DefaultMessageFactory.getDefaultInstance().newMessage(message, null);
					splitMessage.setPayload(message.getPayload());
					splitMessages.add(splitMessage);
				}
				catch (CloneNotSupportedException e)
				{
					log.error("Could not clone message [{}]", id, e);
				}
			}
		}
		log.info("Split multi-payload message into {} standard messages", splitMessages.size());

		return splitMessages;
	}
}
