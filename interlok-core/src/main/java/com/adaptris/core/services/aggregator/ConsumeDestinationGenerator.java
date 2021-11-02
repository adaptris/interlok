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

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ConsumeDestination;
import com.adaptris.validation.constraints.ConfigDeprecated;

/**
 * Generate a {@link ConsumeDestination} based on the message currently being processed.
 * 
 * 
 */
@Deprecated()
@ConfigDeprecated(removalVersion = "4.0.0", groups = Deprecated.class)
public interface ConsumeDestinationGenerator {

  ConsumeDestination generate(AdaptrisMessage msg);
}
