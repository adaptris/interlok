package com.adaptris.core.services.aggregator;

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.MultiPayloadAdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.IOUtils;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@XStreamAlias("multi-payload-aggregator-service")
@ComponentProfile(summary = "Combine the many payloads of a multi-payload message into a single payload using one of the available aggregators", tag = "multi-payload,aggregator,service", since = "4.2.0")
public class MultiPayloadAggregator extends ServiceImp
{
	@Getter
	@Setter
	@NotNull
	@Valid
	private MessageAggregator aggregator;

	/**
	 * <p>
	 * Apply the service to the message.
	 * </p>
	 *
	 * @param m the <code>AdaptrisMessage</code> to process
	 * @throws ServiceException wrapping any underlying <code>Exception</code>s
	 */
	@Override
	public void doService(AdaptrisMessage m) throws ServiceException
	{
		if (!(m instanceof MultiPayloadAdaptrisMessage))
		{
			log.error("Original message must have multiple payloads!");
			throw new ServiceException("Original message must have multiple payloads!");
		}

		try
		{
			MultiPayloadAdaptrisMessage message = (MultiPayloadAdaptrisMessage)m;
			AdaptrisMessage target = AdaptrisMessageFactory.getDefaultInstance().newMessage();

			List<AdaptrisMessage> messages = new ArrayList<>();
			for (String id : message.getPayloadIDs())
			{
				AdaptrisMessage temp = AdaptrisMessageFactory.getDefaultInstance().newMessage();
				IOUtils.copy(message.getInputStream(id), temp.getOutputStream());
				messages.add(temp);
				message.deletePayload(id);
			}

			aggregator.aggregate(target, messages);

			IOUtils.copy(target.getInputStream(), message.getOutputStream());
		}
		catch (Exception e)
		{
			log.error("Exception during merging of message payloads!", e);
			ExceptionHelper.wrapServiceException(e);
		}


//		try (OutputStream os = target.getOutputStream())
//		{
//			for (String id : message.getPayloadIDs())
//			{
//				try (InputStream is = message.getInputStream(id))
//				{
//					IOUtils.copy(is, os);
////					if (appendNewLine())
////					{
////						os.write('\n');
////					}
//				}
//			}
//			if (copyMetadata())
//			{
//				for (MetadataElement metadata : message.getMetadata())
//				{
//					target.addMetadata(metadata);
//				}
//			}
//		}
//		catch (IOException e)
//		{
//			log.error("Exception during merging of message payloads!", e);
//			ExceptionHelper.wrapServiceException(e);
//		}
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public void prepare() throws CoreException
	{
		/* empty; unused */
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	protected void initService() throws CoreException
	{
		/* empty; unused */
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	protected void closeService()
	{
		/* empty; unused */
	}
}
