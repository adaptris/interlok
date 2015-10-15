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

package com.adaptris.core.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;

import com.adaptris.core.CoreException;
import com.adaptris.core.management.BootstrapProperties;
import com.adaptris.core.util.ExceptionHelper;

public class DummyConfigurationPreProcessor2 extends AbstractConfigurationPreProcessor {

  public DummyConfigurationPreProcessor2(BootstrapProperties properties) {
    super(properties);
  }

  @Override
  public String process(String xml) throws CoreException {
    return xml;
  }

  @Override
  public String process(URL urlToXml) throws CoreException {
    String xml = null;
    try (InputStream in = urlToXml.openStream()) {
      xml = IOUtils.toString(in);
    }
    catch (IOException e) {
      ExceptionHelper.rethrowCoreException(e);
    }
    return xml;
  }

}
