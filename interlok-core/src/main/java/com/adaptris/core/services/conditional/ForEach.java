package com.adaptris.core.services.conditional;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.MultiPayloadAdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.services.StopProcessingService;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.LifecycleHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A for-each implementation that iterates over the payloads in a
 * multi-payload message {@link MultiPayloadAdaptrisMessage}. For each
 * payload then execute the given service (list). The default is to use
 * a single thread to iterate over the payloads, but a thread pool can
 * be used to parallelize the loop.
 *
 * <pre>{@code
 * <for-each-payload>
 *   <unique-id>for-each-id</unique-id>
 *   <then>
 *     <service class="shared-service">
 *       <lookup-name>for-each-service-list-id</lookup-name>
 *       <unique-id>for-each-service-list-id</unique-id>
 *     </service>
 *   </then>
 *   <thread-count>1</thread-count>
 * </for-each-payload>
 * }</pre>
 * 
 * <p>
 * Note: If your service list for each payload contains a {@link StopProcessingService} it will not stop the processing of each payload.
 * </p>
 *
 * @author amanderson
 * @config for-each-payload
 * @see MultiPayloadAdaptrisMessage
 * @since 3.10
 */
@XStreamAlias("for-each-payload")
@AdapterComponent
@ComponentProfile(
		summary = "Runs the configured service/list for each multi-payload message payload.",
		tag = "for,each,for each,for-each,then,multi-payload",
		since = "3.10.0")
@DisplayOrder(order = {"then", "threadCount"})
public class ForEach extends ServiceImp
{
	private static final transient Logger log = LoggerFactory.getLogger(ForEach.class.getName());

	private static final int DEFAULT_THREAD_COUNT = 1; // default is single threaded

	@NotNull
	@Valid
	private ThenService then;

	@NotNull
	@Valid
	@AdvancedConfig
	private Integer threadCount = DEFAULT_THREAD_COUNT;

	/**
	 * Get the for-each-then service.
	 *
	 * @return The service.
	 */
	public ThenService getThen()
	{
		return then;
	}

	/**
	 * Set the for-each-then service.
	 *
	 * @param thenService The service.
	 */
	public void setThen(ThenService thenService)
	{
		this.then = thenService;
	}

	/**
	 * Get the number of threads to use.
	 *
	 * @return The number of threads.
	 */
	public Integer getThreadCount()
	{
		return threadCount;
	}

	/**
	 * Set the number of threads to use.
	 *
	 * If set to 0 then as many threads as there are payloads will be
	 * used.
	 *
	 * @param threadCount The number of threads.
	 */
	public void setThreadCount(Integer threadCount)
	{
		Args.notNull(threadCount, "threadCount");
		if (threadCount < 0)
		{
			log.warn("{} is a stupid thread count; will use payload count instead!", threadCount);
			threadCount = 0;
		}
		this.threadCount = threadCount;
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public void doService(AdaptrisMessage msg) throws ServiceException
	{
		ThreadPoolExecutor executor = null;

		try
		{
			log.info("Starting for-each");
			if (!(msg instanceof MultiPayloadAdaptrisMessage))
			{
				log.warn("Message [{}] is not a multi-payload message!", msg.getUniqueId());
				iterate(msg);
			}
			else
			{
				MultiPayloadAdaptrisMessage message = (MultiPayloadAdaptrisMessage)msg;

				int threads = threadCount;
				if (threads == 0)
				{
					// use as many threads as necessary
					threads = message.getPayloadCount();
				}
				log.trace("Using {} thread{}", threads, threads > 1 ? "s" : "");
				executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(threads);

				for (String id : message.getPayloadIDs())
				{
					try
					{
						message.switchPayload(id);
						AdaptrisMessage each = DefaultMessageFactory.getDefaultInstance().newMessage(message, null);
						each.setPayload(message.getPayload());

						executor.execute(() -> iterate(each));
					}
					catch (CloneNotSupportedException e)
					{
						log.error("Could not clone message [{}]", id, e);
					}
				}
			}
		}
		finally
		{
			if (executor != null)
			{
				executor.shutdown();
				try
				{
					while (!executor.awaitTermination(1, TimeUnit.MILLISECONDS))
					{
						/* wait for all threads to complete */
					}
				}
				catch (InterruptedException e)
				{
					log.warn("Interrupted while waiting for tasks to finish!", e);
				}
			}
			log.info("Finished for-each");
		}
	}

	/**
	 * Perform a single iteration of the then service on the given message.
	 *
	 * @param message The message to iterate over.
	 */
	private void iterate(AdaptrisMessage message)
	{
		String id = message.getUniqueId();
		try
		{
			log.debug("Iterating over message [{}}]", id);
			then.getService().doService(message);
		}
		catch (Exception e)
		{
			log.error("Message [{}}] failed!", id, e);
		}
		finally
		{
			log.debug("Done with message [{}]", id);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initService() throws CoreException
	{
		LifecycleHelper.init(then);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void closeService()
	{
		LifecycleHelper.close(then);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void prepare() throws CoreException
	{
		LifecycleHelper.prepare(then);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start() throws CoreException
	{
		LifecycleHelper.start(then);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void stop()
	{
		LifecycleHelper.stop(then);
	}
}
