/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.adaptris.core.common;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.MessageDrivenDestination;
import com.adaptris.core.util.Args;
import com.adaptris.interlok.types.InterlokMessage;

public abstract class FileParameter {
  protected transient Logger log = LoggerFactory.getLogger(this.getClass());

  @Valid
  @NotNull(message = "destination may not be null")
  private MessageDrivenDestination destination;


  protected String url(InterlokMessage msg) throws CoreException {
    Args.notNull(getDestination(), "destination");
    if (msg instanceof AdaptrisMessage) {
      return getDestination().getDestination((AdaptrisMessage) msg);
    } else {
      throw new RuntimeException("Message is not instance of Adaptris Message");
    }
  }

  public MessageDrivenDestination getDestination() {
    return destination;
  }

  /**
   * Set the destination for the file data input.
   *
   * @param d the destination.
   */
  public void setDestination(MessageDrivenDestination d) {
    destination = Args.notNull(d, "destination");
  }

  public <T extends FileParameter> T withDestination(MessageDrivenDestination d) {
    setDestination(d);
    return (T) this;
  }
}
