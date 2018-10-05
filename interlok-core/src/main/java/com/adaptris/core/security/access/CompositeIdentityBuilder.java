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
package com.adaptris.core.security.access;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * {@link IdentityBuilder} implementation that wraps a list of builders.
 * 
 * @config composite-identity-builder
 */
@XStreamAlias("composite-identity-builder")
public class CompositeIdentityBuilder extends IdentityBuilderImpl {

  @XStreamImplicit
  @NotNull
  @AutoPopulated
  private List<IdentityBuilder> builders;

  public CompositeIdentityBuilder() {
    setBuilders(new ArrayList<IdentityBuilder>());
  }

  public CompositeIdentityBuilder(List<IdentityBuilder> builders) {
    this();
    setBuilders(builders);
  }

  @Override
  public Map<String, Object> build(AdaptrisMessage msg) throws ServiceException {
    Map<String, Object> result = new HashMap<>();
    for (IdentityBuilder builder : getBuilders()) {
      result.putAll(builder.build(msg));
    }
    return result;
  }

  /**
   * @return the builders
   */
  public List<IdentityBuilder> getBuilders() {
    return builders;
  }

  /**
   * @param builders the builders to set
   */
  public void setBuilders(List<IdentityBuilder> builders) {
    this.builders = Args.notNull(builders, "builders");
  }

}
