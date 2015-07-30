/*
 * $RCSfile: EventHandlerWrapperStub.java,v $
 * $Revision: 1.10 $
 * $Date: 2008/07/01 16:36:09 $
 * $Author: lchan $
 */
package com.adaptris.core.stubs;

import com.adaptris.core.Adapter;
import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.AdaptrisMessageConsumer;
import com.adaptris.core.AdaptrisMessageProducer;
import com.adaptris.core.Channel;
import com.adaptris.core.ChannelList;
import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.ConsumeDestination;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ServiceList;
import com.adaptris.core.StandardProcessingExceptionHandler;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.core.WorkflowList;
import com.adaptris.core.jms.JmsConnection;
import com.adaptris.core.jms.PtpConsumer;
import com.adaptris.core.jms.PtpProducer;
import com.adaptris.core.services.metadata.AddMetadataService;


/**
 * <p>
 * Stub implementation of <code>EventHandlerWrapper</code> for GUI / client
 * testing. All the methods return the same basic single <code>Channel</code>,
 * single <code>Workflow</code> (with a couple of dummy
 * <code>AddMetadataService</code>s) <code>Adapter</code>.
 * </p>
 */
public class EventHandlerWrapperStub {

  /**
   * <p>
   * Sends request to <code>Adapter</code> directly, obtains reply directly.
   * </p>
   * @param adapterEventDestination <code>Adapter</code>'s event in destination
   * @param replyToDestination destination to reply to
   * @return a snapshot of the <code>Adapter</code>
   * @throws CoreException wrapping any underlying <code>Exception</code>s
   */
  public Adapter requestAdapter(ProduceDestination adapterEventDestination,
    ProduceDestination replyToDestination) throws CoreException {

    return createAdapter();
  }


  /**
   * <p>
   * Sends request to <code>Adapter</code> via hub, obtains reply via hub.
   * </p>
   * @param destinationId the unique id of the <code>Adapter</code>
   * @param sourceId the unique id of the requesting entity
   * @return a snapshot of the <code>Adapter</code>
   * @throws CoreException wrapping any underlying <code>Exception</code>s
   */
  public Adapter requestAdapter(String destinationId, String sourceId)
    throws CoreException {

    return createAdapter(destinationId);
  }


  /**
   * <p>
   * Sends request to <code>Adapter</code> via hub, obtains reply directly.
   * </p>
   * @param destinationId the unique id of the <code>Adapter</code>
   * @param replyToQueue destination to reply to
   * @return a snapshot of the <code>Adapter</code>
   * @throws CoreException wrapping any underlying <code>Exception</code>s
   */
  public Adapter requestAdapter(String destinationId,
    ProduceDestination replyToQueue) throws CoreException {

    return createAdapter(destinationId);
  }


  /**
   * <p>
   * Sends request to <code>Adapter</code> directly, obtains reply via hub.
   * </p>
   * @param adapterEventDestination <code>Adapter</code>'s event in queue
   * @param sourceId the unique id of the requesting entity
   * @return a snapshot of the <code>Adapter</code>
   * @throws CoreException wrapping any underlying <code>Exception</code>s
   */
  public Adapter requestAdapter(ProduceDestination adapterEventDestination,
    String sourceId) throws CoreException {

    return createAdapter();
  }


  private Adapter createAdapter() throws CoreException {
    Adapter adapter = null;

    try {
      MetadataElement element1 = new MetadataElement("key1", "val1");
      MetadataElement element2 = new MetadataElement("key2", "val2");
      MetadataElement element3 = new MetadataElement("key3", "val3");

      AddMetadataService service1 = new AddMetadataService();
      AddMetadataService service2 = new AddMetadataService();

      service1.addMetadataElement(element1);
      service1.addMetadataElement(element2);
      service2.addMetadataElement(element3);

      ServiceList services = new ServiceList();
      services.addService(service1);
      services.addService(service2);

      StandardWorkflow workflow = new StandardWorkflow();
      workflow.setServiceCollection(services);

      AdaptrisConnection consume = new JmsConnection();
      AdaptrisConnection produce = new JmsConnection();

      Channel channel = new Channel();
      channel.setConsumeConnection(consume);
      channel.setProduceConnection(produce);


      AdaptrisMessageConsumer consumer = new PtpConsumer();
      consume.addMessageConsumer(consumer);

      ConsumeDestination consumeDest
        = new ConfiguredConsumeDestination("incoming");

      consumer.setDestination(consumeDest);
      AdaptrisMessageProducer producer = new PtpProducer();
      produce.addMessageProducer(producer);

      ProduceDestination produceDest
        = new ConfiguredProduceDestination("outgoing");

      producer.setDestination(produceDest);

      workflow.setConsumer(consumer);
      workflow.setProducer(producer);

      WorkflowList workflowList = new WorkflowList();
      workflowList.add(workflow);

      channel.setWorkflowList(workflowList);

      StandardProcessingExceptionHandler errorHandler = new StandardProcessingExceptionHandler();

      new ConfiguredProduceDestination("error");

      ChannelList channelList = new ChannelList();
      channelList.addChannel(channel);

      adapter = new Adapter();
      adapter.setMessageErrorHandler(errorHandler);
      adapter.setChannelList(channelList);
      adapter.setUniqueId("testadapter");

    }
    catch (Exception e) {
      throw new CoreException(e);
    }

    return adapter;
  }


  private Adapter createAdapter(String uniqueId) throws CoreException {
    Adapter result = createAdapter();
    result.setUniqueId(uniqueId);

    return result;
  }
}