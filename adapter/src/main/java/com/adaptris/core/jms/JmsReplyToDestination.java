package com.adaptris.core.jms;

import static org.apache.commons.lang.StringUtils.isEmpty;

import javax.jms.Destination;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ProduceDestination;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Implementation of <code>ProduceDestination</code> which resolves the JMS Destination from the JMSReplyTo object metadata.
 * </p>
 * 
 * @config jms-reply-to-destination
 */
@XStreamAlias("jms-reply-to-destination")
public class JmsReplyToDestination implements ProduceDestination {

  private transient Logger log = LoggerFactory.getLogger(this.getClass());

  private String objectMetadataKey;

  // separator char here if required

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public JmsReplyToDestination() {
  }

  /**
   * Creates a new <code>ProduceDestination</code> based on object metadata
   * <p>
   * As this uses object metadata, the toString() method is called on the object
   * that is returned from metadata
   * </p>
   *
   * @param msg the message to process
   * @return the <code>String</code> destination name
   * @throws CoreException wrapping any underlying <code>Exception</code>s
   */
  @Override
  public String getDestination(AdaptrisMessage msg) throws CoreException {

    Object o = retrieveJmsDestination(msg);
    return o != null ? o.toString() : null;
  }

  /**
   *
   * Gets the JMS Destination as stored in object metadata.
   * @return The JMS Destination object
   * @param msg the AdaptrisMessage
   */
  public Destination retrieveJmsDestination(AdaptrisMessage msg)
      throws CoreException {
    Object result = null;
    String keyToUse = deriveMetadataKey();
    if (msg.getObjectMetadata().containsKey(keyToUse)) {
      result = msg.getObjectMetadata().get(keyToUse);
    }
    else {
      log.warn(keyToUse + " not found in object metadata");
    }
    log.debug("Destination [" + result + "]");
    return (Destination) result;
  }

  private String deriveMetadataKey() {
    return isEmpty(getObjectMetadataKey()) ? JmsConstants.OBJ_JMS_REPLY_TO_KEY : getObjectMetadataKey();
  }

  public String getObjectMetadataKey() {
    return objectMetadataKey;
  }

  /**
   * Set the object metadata key that will be used to derive the destination.
   *
   * @param objectMetadataKey
   * @see JmsConstants#OBJ_JMS_REPLY_TO_KEY
   */
  public void setObjectMetadataKey(String objectMetadataKey) {
    this.objectMetadataKey = objectMetadataKey;
  }
}
