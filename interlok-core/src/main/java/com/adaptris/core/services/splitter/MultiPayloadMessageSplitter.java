package com.adaptris.core.services.splitter;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.MultiPayloadAdaptrisMessage;
import com.adaptris.core.MultiPayloadMessageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class MultiPayloadMessageSplitter extends MessageSplitterImp
{
	private static final transient Logger log = LoggerFactory.getLogger(MultiPayloadMessageSplitter.class.getName());

	@Override
	public Iterable<AdaptrisMessage> splitMessage(AdaptrisMessage msg) throws CoreException
	{
		List<AdaptrisMessage> splitMessages = new ArrayList<>();

		if (!(msg instanceof MultiPayloadMessageFactory))
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
