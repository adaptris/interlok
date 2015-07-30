package com.adaptris.core;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * <code>Workflow</code>s link an <code>AdaptrisMessageConsumer</code>, a
 * <code>ServceCollection</code> and an <code>AdaptrisMessageProducer</code>.
 * They also provide error handling functionality.
 * </p>
 */
public interface Workflow extends AdaptrisMessageListener, StateManagedComponent, EventHandlerAware, ComponentLifecycleExtension {
  /**
   * <p>
   * Metadata key used to store the ID of the workflow for processing bad
   * messages.
   * </p>
   */
  String WORKFLOW_ID_KEY = "workflowId";

  /**
   * <p>
   * Metadata key for the unique ID of the message as it is being processed.
   * When it is retried it will have a new UUID, this metadata is used to link
   * the two messages.
   * </p>
   */
  String PREVIOUS_GUID_KEY = "previousGuid";

  /**
   * <p>
   * Produce the message.
   * </p>
   *
   * @param msg the <code>AdaptrisMessage</code> to produce
   * @throws ProduceException if one is encountered producing either the request
   *           or the reply
   * @throws ServiceException if one is encountered applying Services to a reply
   */
  void doProduce(AdaptrisMessage msg) throws ServiceException, ProduceException;

  /**
   * <p>
   * Handle a 'bad' message. A bad message is one which has caused an
   * <code>Exception</code> in the <code>ServceCollection</code> or
   * <code>AdaptrisMessageProducer</code>.
   * </p>
   *
   * @param msg the original version of the 'bad' message
   * @throws CoreException wrapping any Exception that might occur
   */
  void handleBadMessage(AdaptrisMessage msg) throws CoreException;

  /**
   * <p>
   * Handle an <code>Exception</code> encountered producing a message.
   * </p>
   */
  void handleProduceException(); // throw CoreExc?

  /*
   * The methods below are used by WorkflowList and therefore have to be in the
   * Workflow interface. Other than being in an interface there is no need for
   * them to be public.
   */

  /**
   * <p>
   * Returns this <code>Workflow</code>'s <code>AdaptrisMessageConsumer</code>.
   * </p>
   *
   * @return this <code>Workflow</code>'s <code>AdaptrisMessageConsumer</code>
   */
  AdaptrisMessageConsumer getConsumer();

  /**
   * <p>
   * Returns this <code>Workflow</code>'s <code>AdaptrisMessageProducer</code>.
   * </p>
   *
   * @return this <code>Workflow</code>'s <code>AdaptrisMessageProducer</code>
   */
  AdaptrisMessageProducer getProducer();

  /**
   * <p>
   * Returns what the workflow considers to be it's unique ID of the workflow.
   * </p>
   * <p>
   * This may differ from any 'unique-id' element that has been set.
   * </p>
   *
   * @return the unique ID
   */
  String obtainWorkflowId();

  /**
   * <p>
   * Returns any configured unique-id.
   * </p>
   *
   * @return the unique ID
   */
  @Override
  String getUniqueId();

  /**
   * <p>
   * Returns the {@link ProcessingExceptionHandler} to use.
   * </p>
   *
   * @return the ProcessingExceptionHandler to use
   */
  ProcessingExceptionHandler getMessageErrorHandler();

  /**
   * <p>
   * Sets the {@link ProcessingExceptionHandler} to use.
   * </p>
   *
   * @param meh the ProcessingExceptionHandler to use
   */
  void registerActiveMsgErrorHandler(ProcessingExceptionHandler meh);

  /**
   * <p>
   * Returns a reference to this <code>Workflow</code>'s <code>Channel</code>.
   * </p>
   *
   * @return a reference to this <code>Workflow</code>'s <code>Channel</code>
   */
  Channel obtainChannel();

  /**
   * <p>
   * Sets a reference to this <code>Workflow</code>'s <code>Channel</code>.
   * </p>
   *
   * @param ch this <code>Workflow</code>'s <code>Channel</code>
   */
  void registerChannel(Channel ch) throws CoreException;

  /**
   * Get the last time this workflow was started
   *
   * @return workflow start time
   */
  Date lastStartTime();

  /**
   * Get the last time this workflow was stopped.
   * This is set when the workflow is initialised so it may
   * have been subsequently started.
   *
   * @return channel stop time
   */
  Date lastStopTime();

  /**
   * Return all the configured interceptors.
   * 
   * @return all the configured interceptors.
   */
  List<WorkflowInterceptor> getInterceptors();

  /**
   * Whether or not message-counts should be disabled on this workflow.
   * 
   * @since 3.0.3
   */
  boolean disableMessageCount();
}
