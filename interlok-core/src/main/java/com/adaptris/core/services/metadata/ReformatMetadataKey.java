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

package com.adaptris.core.services.metadata;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.MetadataCollection;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.ServiceException;
import com.adaptris.core.metadata.MetadataFilter;
import com.adaptris.core.metadata.RemoveAllMetadataFilter;

/**
 * Implementation of {@link com.adaptris.core.Service} that reformats matching metadata keys.
 * 
 * 
 */
public abstract class ReformatMetadataKey extends MetadataServiceImpl {

  @AutoPopulated
  @NotNull
  @Valid
  private MetadataFilter keysToModify;

  public ReformatMetadataKey() {
    setKeysToModify(new RemoveAllMetadataFilter());
  }

  protected abstract String reformatKey(String s) throws ServiceException;

  public MetadataFilter getKeysToModify() {
    return keysToModify;
  }

  public void setKeysToModify(MetadataFilter toModify) {
    this.keysToModify = toModify;
  }


  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    MetadataCollection toFilter = getKeysToModify().filter(msg);
    MetadataCollection replacements = buildReplacements(toFilter);
    // toFilter.forEach(e -> { msg.removeMetadata(e); });
    // replacements.forEach(e -> { msg.addMetadata(e); });
    logMetadata("Removing {}", toFilter);
    removeMetadata(msg, toFilter);
    logMetadata("Adding {}", replacements);
    addMetadata(msg, replacements);
  }

  private MetadataCollection buildReplacements(MetadataCollection toFilter) throws ServiceException {
    MetadataCollection replacements = new MetadataCollection();
    for (MetadataElement e : toFilter) {
      String newKey = reformatKey(e.getKey());
      replacements.add(new MetadataElement(newKey, e.getValue()));
    }
    return replacements;
  }

  private void removeMetadata(AdaptrisMessage msg, MetadataCollection toRemove) {
    for (MetadataElement e : toRemove) {
      msg.removeMetadata(e);
    }
  }

  private void addMetadata(AdaptrisMessage msg, MetadataCollection toAdd) {
    for (MetadataElement e : toAdd) {
      msg.addMetadata(e);
    }
  }
}
