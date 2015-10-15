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
