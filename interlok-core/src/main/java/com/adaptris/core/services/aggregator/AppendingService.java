package com.adaptris.core.services.aggregator;

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.interlok.config.DataInputParameter;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.IOUtils;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

@XStreamAlias("appending-service")
@ComponentProfile(summary = "Combine many sources into a single payload using one of the available aggregators", tag = "append,aggregator,service", since = "4.2.0")
public class AppendingService extends ServiceImp
{
	@Getter
	@Setter
	@NotNull
	@Valid
	private List<DataInputParameter> sources;

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
	 * @param message the <code>AdaptrisMessage</code> to process
	 * @throws ServiceException wrapping any underlying <code>Exception</code>s
	 */
	@Override
	public void doService(AdaptrisMessage message) throws ServiceException
	{
		try
		{
			List<AdaptrisMessage> messages = new ArrayList<>();
			for (DataInputParameter p : sources)
			{
				try
				{
					AdaptrisMessage temp;
					Object o = p.extract(message);
					if (o instanceof InputStream)
					{
						temp = AdaptrisMessageFactory.getDefaultInstance().newMessage();
						try (OutputStream o2 = temp.getOutputStream())
						{
							InputStream source = (InputStream)o;
							IOUtils.copy(source, o2);
						}
					}
					else
					{
						temp = AdaptrisMessageFactory.getDefaultInstance().newMessage((String)o);
					}
					messages.add(temp);
				}
				catch (Exception e)
				{
					log.warn("Could not read from source", e);
				}
			}
			aggregator.aggregate(message, messages);
		}
		catch (Exception e)
		{
			log.error("Could not initialise output stream in message", e);
			ExceptionHelper.rethrowServiceException(e);
		}
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
