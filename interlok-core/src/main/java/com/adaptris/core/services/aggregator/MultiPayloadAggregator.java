package com.adaptris.core.services.aggregator;

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.MultiPayloadAdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.util.stream.StreamUtil;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
      /*
       * The aggregator expects a collection of messages, whereas
       * here there is one message with multiple payloads.
       */
      List<AdaptrisMessage> messages = new ArrayList<>();
      Set<String> ids = new HashSet<>(message.getPayloadIDs());
      for (String id : ids)
      {
        AdaptrisMessage temp = AdaptrisMessageFactory.getDefaultInstance().newMessage();
        StreamUtil.copyAndClose(message.getInputStream(id), temp.getOutputStream());
        messages.add(temp);
        message.deletePayload(id);
      }
      aggregator.aggregate(target, messages);
      /*
       * With all the payloads now combined, put the resultant
       * payload back into the original message.
       */
      StreamUtil.copyAndClose(target.getInputStream(), message.getOutputStream());
    }
    catch (Exception e)
    {
      log.error("Exception during merging of message payloads!", e);
      throw ExceptionHelper.wrapServiceException(e);
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
