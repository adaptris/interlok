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


import com.adaptris.annotation.Removal;

/**
 * <p>
 * Implementations of this interface return a <code>String</code> destination
 * (e.g. queue name, URL) to be used by <code>AdaptrisMessageProducer</code>.
 * </p>
 */
@Deprecated
@Removal(version = "5.0.0", message = "To be removed as part of the ongoing removal of Destination objects")
public interface MessageDrivenDestination {

	String getDestination(AdaptrisMessage msg) throws CoreException;

}
