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

package com.adaptris.core.services;

import static com.adaptris.util.stream.UnicodeDetectingInputStream.UTF_8;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.util.stream.UnicodeDetectingInputStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Service that removes UTF8 byte order marks that may be present.
 * 
 * <p>
 * This is only really useful when Windows (.NET application or otherwise) generated files are being processed by the adapter. In
 * almost all situations, windows will output a redundant UTF-8 BOM which may cause issues with certain types of XML processing. In
 * the event that no BOM is detected, then nothing is done to the message.
 * </p>
 * <p>
 * This is a workaround for <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4508058">This Sun JVM bug</a>.
 * </p>
 * 
 * @config utf8-bom-remover
 * 
 * @license BASIC
 * @author $Author: lchan $
 */
@XStreamAlias("utf8-bom-remover")
public class Utf8BomRemover extends ServiceImp {

  private static final String DEFAULT_CHAR_ENCODING = "ISO-8859-1";

  public Utf8BomRemover() {

  }

  public void doService(AdaptrisMessage msg) throws ServiceException {
    InputStream msgIn = null;
    OutputStream out = null;
    UnicodeDetectingInputStream utf8 = null;
    try {
      msgIn = msg.getInputStream();
      utf8 = new UnicodeDetectingInputStream(msgIn, DEFAULT_CHAR_ENCODING);
      if (UTF_8.equals(utf8.getEncoding())) {
        out = msg.getOutputStream();
        IOUtils.copy(utf8, out);
      }
    }
    catch (Exception e) {
      throw new ServiceException(e);
    }
    finally {
      IOUtils.closeQuietly(out);
      IOUtils.closeQuietly(utf8);
      IOUtils.closeQuietly(msgIn);
    }
  }

  @Override
  protected void initService() {
  }

  @Override
  protected void closeService() {
  }


  @Override
  public void prepare() throws CoreException {
  }

}
