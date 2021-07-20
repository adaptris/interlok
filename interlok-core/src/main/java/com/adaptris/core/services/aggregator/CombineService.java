package com.adaptris.core.services.aggregator;

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.InputFieldDefault;
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
import org.apache.commons.lang3.ObjectUtils;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Using the existing aggregators, append many data sources together
 * into a single message payload. See implementations of
 * {@link MessageAggregator}.
 *
 * @config combine-service
 * @since 4.2.0
 */
@XStreamAlias("combine-service")
@ComponentProfile(summary = "Combine many sources into a single payload using one of the available aggregators", tag = "combine,append,aggregator,service", since = "4.2.0")
public class CombineService extends ServiceImp
{
  /**
   * The data sources from which to append.
   */
  @Getter
  @Setter
  @NotNull
  @Valid
  private List<DataInputParameter> sources;

  /**
   * How the data sources should be appended.
   */
  @Getter
  @Setter
  @Valid
  @InputFieldDefault("appending-message-aggregator")
  private MessageAggregator aggregator;

  /**
   * Append the data sources together to create the new payload.
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
            try (InputStream source = (InputStream)o; OutputStream o2 = temp.getOutputStream())
            {
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
      aggregator().aggregate(message, messages);
    }
    catch (Exception e)
    {
      log.error("Could not initialise output stream in message", e);
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  private MessageAggregator aggregator()
  {
    return ObjectUtils.defaultIfNull(aggregator, new AppendingMessageAggregator());
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
