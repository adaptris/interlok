package com.adaptris.core.services.splitter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.GenerateBeanInfo;
import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageProducer;
import com.adaptris.core.CoreException;
import com.adaptris.core.NullConnection;
import com.adaptris.core.NullMessageProducer;
import com.adaptris.core.ProduceException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Splits incoming {@link AdaptrisMessage}s into several <code>AdaptrisMessage</code>s using an implementation of
 * {@link MessageSplitter}.
 * </p>
 * <p>
 * This implementation simply uses the configured producer and connection to produce the split message.
 * </p>
 * 
 * @config basic-message-splitter-service
 * 
 * @license BASIC
 */
@GenerateBeanInfo
@XStreamAlias("basic-message-splitter-service")
public class BasicMessageSplitterService extends MessageSplitterServiceImp {

  @NotNull
  @AutoPopulated
  @Valid
  private AdaptrisConnection connection;
  @NotNull
  @AutoPopulated
  @Valid
  private AdaptrisMessageProducer producer;

  /**
   * <p>
   * Creates a new instance. Defaults to copying all metadata from the original
   * message to the new, split messages.
   * </p>
   */
  public BasicMessageSplitterService() {
    super();
    setConnection(new NullConnection());
    setProducer(new NullMessageProducer());
  }

  /**
   *
   * @see MessageSplitterServiceImp#handleSplitMessage(AdaptrisMessage)
   */
  @Override
  protected void handleSplitMessage(AdaptrisMessage msg) throws ServiceException {
    try {
      producer.produce(msg);
    } catch (ProduceException e) {
      throw new ServiceException(e);
    }
  }

  /** @see com.adaptris.core.AdaptrisComponent#init() */
  @Override
  public void init() throws CoreException {
    super.init();
    connection.addExceptionListener(this); // back ref
    connection.addMessageProducer(producer);
    LifecycleHelper.init(connection);
    LifecycleHelper.init(producer);
  }

  /** @see com.adaptris.core.AdaptrisComponent#start() */
  @Override
  public void start() throws CoreException {
    LifecycleHelper.start(producer);
    LifecycleHelper.start(connection);
  }

  /** @see com.adaptris.core.AdaptrisComponent#stop() */
  @Override
  public void stop() {
    LifecycleHelper.stop(producer);
    LifecycleHelper.stop(connection);
  }

  /** @see com.adaptris.core.AdaptrisComponent#close() */
  public void close() {
    LifecycleHelper.close(producer);
    LifecycleHelper.close(connection);
  }

  /**
   * <p>
   * Sets the <code>AdaptrisConnection</code> to use for producing split
   * messages.
   * </p>
   *
   * @param conn the <code>AdaptrisConnection</code> to use for producing split
   *          messages, may not be null
   */
  public void setConnection(AdaptrisConnection conn) {
    if (conn == null) {
      throw new IllegalArgumentException("param is null");
    }
    connection = conn;
  }

  /**
   * <p>
   * Returns the <code>AdaptrisConnection</code> to use for producing split
   * messages.
   * </p>
   *
   * @return the <code>AdaptrisConnection</code> to use for producing split
   *         messages
   */
  public AdaptrisConnection getConnection() {
    return connection;
  }

  /**
   * <p>
   * Sets the <code>AdaptrisMessageProducer</code> to use for producing split
   * messages.
   * </p>
   *
   * @param prod the <code>AdaptrisMessageProducer</code> to use for producing
   *          split messages, may not be null
   */
  public void setProducer(AdaptrisMessageProducer prod) {
    if (prod == null) {
      throw new IllegalArgumentException("param is null");
    }
    producer = prod;
  }

  /**
   * <p>
   * Returns the <code>AdaptrisMessageProducer</code> to use for producing split
   * messages.
   * </p>
   *
   * @return the <code>AdaptrisMessageProducer</code> to use for producing split
   *         messages
   */
  public AdaptrisMessageProducer getProducer() {
    return producer;
  }

  @Override
  public boolean isEnabled(License license) throws CoreException {
    return license.isEnabled(LicenseType.Basic) && getConnection().isEnabled(license) && getProducer().isEnabled(license);
  }
}
