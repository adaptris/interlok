/*
 * $RCSfile: NullMessageConsumer.java,v $
 * $Revision: 1.7 $
 * $Date: 2006/04/03 07:40:34 $
 * $Author: lchan $
 */
package com.adaptris.core;

import com.adaptris.util.license.License;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Null implementation of <code>AdaptrisMessageConsumer</code>.
 * </p>
 * 
 * @config null-message-consumer
 */
@XStreamAlias("null-message-consumer")
public class NullMessageConsumer extends AdaptrisMessageConsumerImp {
	
	public NullMessageConsumer() {
		setMessageFactory(null);
	}

  /** @see com.adaptris.core.AdaptrisComponent#close() */
  public void close() {
    // do nothing
  }

  /** @see com.adaptris.core.AdaptrisComponent#init() */
  public void init() throws CoreException {
    // do nothing
  }

  /** @see com.adaptris.core.AdaptrisComponent#start() */
  public void start() throws CoreException {
    // do nothing
  }

  /** @see com.adaptris.core.AdaptrisComponent#stop() */
  public void stop() {
    // do nothing
  }
  /**
   * 
   * @see com.adaptris.core.AdaptrisComponent#isEnabled(License)
   */
  public boolean isEnabled(License license) throws CoreException {
    return true;
  }
  
}
