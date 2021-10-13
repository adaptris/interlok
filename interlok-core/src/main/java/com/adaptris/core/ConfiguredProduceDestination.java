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

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.validation.constraints.ConfigDeprecated;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Basic implementation of <code>ProduceDestination</code> that has a configured <code>String</code> destination.
 * </p>
 * @config configured-produce-destination
 */
// Should be deprecated at some point, as using configured-produce-destination where we have messageDrivenDestinations
// just looks shit.
@XStreamAlias("configured-produce-destination")
@DisplayOrder(order = {"destination"})
@Deprecated()
@ConfigDeprecated(removalVersion = "4.0.0", groups = Deprecated.class)
public final class ConfiguredProduceDestination extends ConfiguredDestination {

  public ConfiguredProduceDestination() {
    super();
  }

  public ConfiguredProduceDestination(String s) {
    super(s);
  }
}
