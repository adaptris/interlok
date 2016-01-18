/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.adaptris.core.runtime;

import static com.adaptris.core.Workflow.WORKFLOW_ID_KEY;
import static org.apache.commons.lang.StringUtils.isEmpty;

import javax.management.MalformedObjectNameException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisComponent;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Record any error'd adaptris messages and make these available through JMX.
 * 
 * @config standard-message-error-digester
 * @author lchan
 * 
 */
@XStreamAlias("standard-message-error-digester")
@AdapterComponent
@ComponentProfile(summary = "The default message error digester that exposes some minimum metrics via JMX",
    tag = "error-handling,base")
public class StandardMessageErrorDigester extends MessageErrorDigesterImp {

  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());
  static final int MAX_MESSAGES = 100;

  /**
   * The maximum number of messages you want to hold in this digest.
   */
  private int digestMaxSize;
  private String uniqueId;
  private transient MessageErrorDigest messageErrorDigest;
  private transient int totalErrorCount = 0;

  static {
    RuntimeInfoComponentFactory.registerComponentFactory(new JmxFactory());
  }

  public StandardMessageErrorDigester() {
    messageErrorDigest = new MessageErrorDigest();
    setDigestMaxSize(MAX_MESSAGES);
  }

  public StandardMessageErrorDigester(String uniqueId) {
    this(uniqueId, MAX_MESSAGES);
  }

  public StandardMessageErrorDigester(String uniqueId, int max) {
    this();
    setUniqueId(uniqueId);
    setDigestMaxSize(max);
  }

  @Override
  public void init() throws CoreException {
    super.init();
    messageErrorDigest.setMaxMessages(getDigestMaxSize());
  }

  @Override
  public void digest(AdaptrisMessage msg) {
    MessageDigestErrorEntry messageError = new MessageDigestErrorEntry(msg.getUniqueId(), msg.getMetadataValue(WORKFLOW_ID_KEY));
    messageError.extractInfo(msg);
    messageErrorDigest.add(messageError);
    totalErrorCount++;
  }

  public void setDigestMaxSize(int max) {
    digestMaxSize = max;
  }

  public int getDigestMaxSize() {
    return digestMaxSize;
  }

  public int getTotalErrorCount() {
    return totalErrorCount;
  }

  public MessageErrorDigest getDigest() {
    return messageErrorDigest;
  }

  public boolean remove(MessageDigestErrorEntry entry) {
    return messageErrorDigest.remove(entry);
  }

  public boolean remove(String msgId) {
    return messageErrorDigest.remove(new MessageDigestErrorEntry(msgId, null));
  }

  /**
   * @return the uniqueId
   */
  public String getUniqueId() {
    return uniqueId;
  }

  /**
   * @param id the uniqueId to set
   */
  public void setUniqueId(String id) {
    uniqueId = id;
  }

  private static class JmxFactory extends RuntimeInfoComponentFactory {

    @Override
    protected boolean isSupported(AdaptrisComponent e) {
      if (e != null && e instanceof StandardMessageErrorDigester) {
        return !isEmpty(((StandardMessageErrorDigester) e).getUniqueId());
      }
      return false;
    }

    @Override
    protected RuntimeInfoComponent createComponent(ParentRuntimeInfoComponent parent, AdaptrisComponent e)
        throws MalformedObjectNameException {
      return new StandardMessageErrorDigesterJmx((AdapterManager) parent, (StandardMessageErrorDigester) e);
    }

  }
}
