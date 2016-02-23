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

package com.adaptris.core.services.aggregator;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.ExceptionHelper;

/**
 * Abstract implementation of {@link MessageAggregator}.
 * 
 * @author lchan
 * 
 */
public abstract class MessageAggregatorImpl implements MessageAggregator {

  @AdvancedConfig
  private Boolean overwriteMetadata;

  /**
   * @return the overwriteMetadata
   */
  public Boolean getOverwriteMetadata() {
    return overwriteMetadata;
  }

  /**
   * Whether or not to overwrite original metadata with metadata from the split messages.
   * 
   * @param b the overwriteMetadata to set, default is null (false)
   */
  public void setOverwriteMetadata(Boolean b) {
    this.overwriteMetadata = b;
  }

  protected boolean overwriteMetadata() {
    return getOverwriteMetadata() != null ? getOverwriteMetadata().booleanValue() : false;
  }

  protected void overwriteMetadata(AdaptrisMessage src, AdaptrisMessage target) {
    if (overwriteMetadata()) {
      target.setMetadata(src.getMetadata());
    }
  }

  @Deprecated
  protected static void rethrowCoreException(Exception e) throws CoreException {
    ExceptionHelper.rethrowCoreException(e);
  }
}
