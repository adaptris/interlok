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

import com.adaptris.core.runtime.MessageErrorDigester;

/**
 * <p>
 * <code>ErrorHandlerRegister</code>
 * </p>
 *
 * @author Aaron - 19 Nov 2012
 * @version 1.0
 *
 */
public interface ErrorHandlerRegistrar {

	public void registerParent(ProcessingExceptionHandler handler);

  public void registerDigester(MessageErrorDigester digester);

	public void notifyParent(AdaptrisMessage message);

	public void onChildError(AdaptrisMessage message);

}
