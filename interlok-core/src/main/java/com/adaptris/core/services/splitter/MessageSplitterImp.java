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

package com.adaptris.core.services.splitter;

import java.util.Iterator;

import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreConstants;

/**
 * Partial implementation of MessageSplitter that handles
 * {@link MessageSplitter#splitMessage(AdaptrisMessage)}.
 *
 * @author lchan
 * @author $Author: lchan $
 */
public abstract class MessageSplitterImp implements MessageSplitter {

  protected transient Logger logR = LoggerFactory.getLogger(this.getClass());

  @AdvancedConfig
  private AdaptrisMessageFactory messageFactory;
  @InputFieldDefault(value = "true")
  private Boolean copyMetadata;
  @InputFieldDefault(value = "false")
  private Boolean copyObjectMetadata;

  public MessageSplitterImp() {
  }


  /**
   * Copy metadata from the parent to the child.
   *
   * @param parent the parent AdaptrisMessage
   * @param child the child AdaptrisMessage
   * @see #setCopyMetadata(Boolean)
   * @see #setCopyObjectMetadata(Boolean)
   */
  protected void copyMetadata(AdaptrisMessage parent, AdaptrisMessage child) {
    if (copyMetadata()) {
      child.setMetadata(parent.getMetadata());
    }
    child.addMetadata(CoreConstants.PARENT_UNIQUE_ID_KEY, parent.getUniqueId());
    if (copyObjectMetadata()) {
      child.getObjectHeaders().putAll(parent.getObjectHeaders());
    }
  }

  /**
   * Select the {@linkplain com.adaptris.core.AdaptrisMessageFactory} instance to use to create new messages.
   *
   * @param msg the AdaptrisMessage to derive the message factory from.
   * @return an {@linkplain com.adaptris.core.AdaptrisMessageFactory} instance
   * @see AdaptrisMessage#getFactory()
   * @see #setMessageFactory(AdaptrisMessageFactory)
   */
  protected AdaptrisMessageFactory selectFactory(AdaptrisMessage msg) {
    /*
     * If the user configured a message factory, use that. We cannot just use the MessageFactory
     * from the message because if that's a FileBackedMessageFactory then the messages resulting from 
     * the split will also be File Backed and we don't want that unless it's explicitly being 
     * configured that way (why would you want that? no-one knows).
     */
    if (getMessageFactory() == null) {
      return msg.getFactory();
    } else {
      return getMessageFactory();
    }
  }

  boolean copyMetadata() {
    return BooleanUtils.toBooleanDefaultIfNull(getCopyMetadata(), true);
  }

  boolean copyObjectMetadata() {
    return BooleanUtils.toBooleanDefaultIfNull(getCopyObjectMetadata(), false);
  }

  /**
   * @return the messageFactory, null if not specified.
   */
  public AdaptrisMessageFactory getMessageFactory() {
    return messageFactory;
  }

  /**
   * Set the message factory used when creating AdaptrisMessage instances.
   *
   * @param f the messageFactory to set
   */
  public void setMessageFactory(AdaptrisMessageFactory f) {
    messageFactory = f;
  }

  /**
   * <p>
   * Sets whether to copy metadata from the original message to the split
   * messages.
   * </p>
   *
   * @param b whether to copy metadata from the original message to the split
   *          messages (default true)
   */
  public void setCopyMetadata(Boolean b) {
    copyMetadata = b;
  }

  /**
   * <p>
   * Returns whether to copy metadata from the original message to the split
   * messages.
   * </p>
   *
   * @return whether to copy metadata from the original message to the split
   *         messages
   */
  public Boolean getCopyMetadata() {
    return copyMetadata;
  }

  public Boolean getCopyObjectMetadata() {
    return copyObjectMetadata;
  }

  /**
   * Whether or not to preserve object metadata to the split messages.
   *
   * @param b true to preserve object metadata (default false)
   */
  public void setCopyObjectMetadata(Boolean b) {
    copyObjectMetadata = b;
  }

  protected abstract class SplitMessageIterator
      implements com.adaptris.core.util.CloseableIterable<AdaptrisMessage>, Iterator<AdaptrisMessage> {
    protected final AdaptrisMessage msg;
    protected final AdaptrisMessageFactory factory;
    private AdaptrisMessage nextMessage;
    private boolean iteratorAvailable = true;
    
    public SplitMessageIterator(AdaptrisMessage msg, AdaptrisMessageFactory factory) {
      this.msg = msg;
      this.factory = factory;
    }

    @Override
    public Iterator<AdaptrisMessage> iterator() {
      if (!iteratorAvailable) {
        throw new IllegalStateException("iterator() no longer available");
      }
      iteratorAvailable = false;
      return this;
    }

    @Override
    public boolean hasNext() {
      if (nextMessage == null) {
        try {
          nextMessage = constructAdaptrisMessage();
        } catch (Exception e) {
          logR.warn("Could not construct next AdaptrisMessage", e);
          throw new RuntimeException("Could not construct next AdaptrisMessage", e);
        }
      }

      return nextMessage != null;
    }

    @Override
    public AdaptrisMessage next() {
      AdaptrisMessage ret = nextMessage;
      nextMessage = null;
      return ret;
    }

    protected abstract AdaptrisMessage constructAdaptrisMessage() throws Exception;

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }

  }
}
