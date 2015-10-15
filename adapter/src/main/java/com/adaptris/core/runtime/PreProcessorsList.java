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
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.adaptris.core.CoreException;

public class PreProcessorsList extends AbstractCollection<ConfigurationPreProcessor> {

  private List<ConfigurationPreProcessor> preProcessors;
  
  public PreProcessorsList() {
    this.setPreProcessors(new ArrayList<ConfigurationPreProcessor>());
  }

  public String process(URL urlToXml) throws CoreException {
    InputStream inputStream = null;
    try {
      inputStream = urlToXml.openConnection().getInputStream();
      String xml = IOUtils.toString(inputStream);
      return this.process(xml);
    } catch (IOException ex) {
      throw new CoreException(ex);
    } finally {
      IOUtils.closeQuietly(inputStream);
    }
  }
  
  public String process(String xml) throws CoreException {
    String returnValue = xml;
    for(ConfigurationPreProcessor preProcessor : this.getPreProcessors())
      returnValue = process(returnValue, preProcessor);
    
    return returnValue;
  }
  
  private String process(String xml, ConfigurationPreProcessor preProcessor) throws CoreException {
    return preProcessor.process(xml);
  }
  
  //*******************************************************************
  // List Operations
  //*******************************************************************
  
  @Override
  public Iterator<ConfigurationPreProcessor> iterator() {
    return this.getPreProcessors().iterator();
  }

  @Override
  public boolean add(ConfigurationPreProcessor preProcessor) {
    return preProcessors.add(preProcessor);
  }

  @Override
  public boolean addAll(Collection<? extends ConfigurationPreProcessor> c) {
    return preProcessors.addAll(c);
  }

  @Override
  public void clear() {
    preProcessors.clear();
  }
  
  @Override
  public int size() {
    return this.getPreProcessors().size();
  }

  public List<ConfigurationPreProcessor> getPreProcessors() {
    return preProcessors;
  }

  public void setPreProcessors(List<ConfigurationPreProcessor> preProcessors) {
    this.preProcessors = preProcessors;
  }

}
