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

import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.MessageDrivenDestination;
import com.adaptris.core.util.Args;
import com.adaptris.interlok.types.InterlokMessage;
import com.adaptris.validation.constraints.ConfigDeprecated;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;

import static com.adaptris.core.util.DestinationHelper.logWarningIfNotNull;
import static com.adaptris.core.util.DestinationHelper.mustHaveEither;

public abstract class FileParameter {
  protected transient Logger log = LoggerFactory.getLogger(this.getClass());

  @Valid
  @Getter
  @Deprecated(forRemoval = true)
  @ConfigDeprecated(removalVersion = "4.0.0", message = "Use 'destination-path' instead", groups = Deprecated.class)
  private MessageDrivenDestination destination;

  /**
   * The directory specified as a path.
   */
  @Valid
  @InputFieldHint(expression = true)
  @Getter
  @Setter
  // Needs to be @NotBlank when destination is removed.
  private String url;

  private transient boolean destWarning;

  protected String url(InterlokMessage msg) throws CoreException {

    warnDeprecated();

    if (msg instanceof AdaptrisMessage) {

      if (url != null) {
        return msg.resolve(url);
      }

      return getDestination().getDestination((AdaptrisMessage) msg);
    } else {
      throw new RuntimeException("Message is not instance of Adaptris Message");
    }
  }

  private void warnDeprecated() {
    logWarningIfNotNull(destWarning, () -> destWarning = true, destination,
            "{} uses destination, use 'url' instead", this.getClass().getSimpleName());
    mustHaveEither(url, destination);
  }


  /**
   * Set the destination for the file data input.
   *
   * @param d the destination.
   */
  @Deprecated
  public void setDestination(MessageDrivenDestination d) {
    destination = Args.notNull(d, "destination");
  }

  @Deprecated
  public <T extends FileParameter> T withDestination(MessageDrivenDestination d) {
    setDestination(d);
    return (T) this;
  }
}
