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

package com.adaptris.core.jms.activemq;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.jms.JMSException;

import org.apache.activemq.blob.BlobUploadStrategy;
import org.apache.activemq.command.ActiveMQBlobMessage;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("example-no-op-blob-upload-strategy")
public class ExampleNoOpBlobUploadStrategy implements BlobUploadStrategy {

  public URL uploadFile(ActiveMQBlobMessage arg0, File arg1) throws JMSException, IOException {
    throw new UnsupportedOperationException("Not possible, just an example");
  }

  public URL uploadStream(ActiveMQBlobMessage arg0, InputStream arg1) throws JMSException, IOException {
    throw new UnsupportedOperationException("Not possible, just an example");
  }

}
