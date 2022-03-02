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

package com.adaptris.core.config;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import com.adaptris.core.CoreException;
import com.adaptris.core.util.ExceptionHelper;

@Slf4j
public class ConfigPreProcessors extends AbstractCollection<ConfigPreProcessor> {

  private List<ConfigPreProcessor> preProcessors;
  
  public ConfigPreProcessors() {
    this.setPreProcessors(new ArrayList<ConfigPreProcessor>());
  }

  public ConfigPreProcessors(Collection<? extends ConfigPreProcessor> existing) {
    this();
    addAll(existing);
  }

  public ConfigPreProcessors(ConfigPreProcessor... processors) {
    this(Arrays.asList(processors));
  }

  public String process(URL urlToXml) throws CoreException {
    try (InputStream inputStream = urlToXml.openConnection().getInputStream()){
      String xml = IOUtils.toString(inputStream, Charset.defaultCharset());
      return this.process(xml);
    } catch (Exception ex) {
      log.error("Could not read from/find XML configuration file : {} ", urlToXml.getPath());
      throw ExceptionHelper.wrapCoreException(ex);
    }
  }
  
  public String process(String xml) throws CoreException {
    String returnValue = xml;
    for(ConfigPreProcessor preProcessor : this.getPreProcessors())
      returnValue = process(returnValue, preProcessor);
    
    return returnValue;
  }
  
  private String process(String xml, ConfigPreProcessor preProcessor) throws CoreException {
    return preProcessor.process(xml);
  }
  
  //*******************************************************************
  // List Operations
  //*******************************************************************
  
  @Override
  public Iterator<ConfigPreProcessor> iterator() {
    return this.getPreProcessors().iterator();
  }

  @Override
  public boolean add(ConfigPreProcessor preProcessor) {
    return preProcessors.add(preProcessor);
  }

  @Override
  public boolean addAll(Collection<? extends ConfigPreProcessor> c) {
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

  public List<ConfigPreProcessor> getPreProcessors() {
    return preProcessors;
  }

  public void setPreProcessors(List<ConfigPreProcessor> preProcessors) {
    this.preProcessors = preProcessors;
  }

}
