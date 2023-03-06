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

package com.adaptris.core.services.system;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.adaptris.core.AdaptrisMessage;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link CommandOutputCapture} that overwrites the existing message with the output.
 * 
 * @author lchan
 * 
 */
@JacksonXmlRootElement(localName = "system-command-overwrite-payload")
@XStreamAlias("system-command-overwrite-payload")
public class OverwritePayload implements CommandOutputCapture {

  @Override
  public OutputStream startCapture(AdaptrisMessage msg) throws IOException {
    return new BufferedOutputStream(msg.getOutputStream());
  }

}
