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

import com.adaptris.annotation.*;
import com.adaptris.core.*;
import com.adaptris.core.services.splitter.PoolingMessageSplitterService;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairList;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.MDC;

import javax.validation.constraints.NotNull;

/**
 * Add a mapped diagnostic context via {@link MDC#put(String, String)}.
 *
 * <p>
 * It can be useful to use a mapped diagnostic context to provide additional information into your logfile if the underlying logging
 * system supports it (e.g. logback or log4j2)
 * </p>
 * <p>
 * As the diagnostic logging context is thread based; bear in mind that you will lose the context if part of the service execution
 * chain contains a something like {@link PoolingMessageSplitterService} or similar (i.e. something with an underlying thread pool
 * that acts on the message).
 * </p>
 *
 * <p>This is designed to work in conjunction with {@link com.adaptris.core.interceptor.LoggingContextWorkflowInterceptor} as it will
 * add the following items based on object metadata, this is particular useful for above highlighted the scenario with {@link PoolingMessageSplitterService}.
 * </p>
 * <ul>
 *     <li>channelid</li>
 *     <li>workflowid</li>
 *     <li>messageuniqueid</li>
 * </ul>

 * 
 * @config add-extended-logging-context-service
 * 
 */
@XStreamAlias("add-extended-logging-context-service")
@AdapterComponent
@ComponentProfile(summary = "Add a mapped diagnostic context for logging; useful for filtering", tag = "service,logging,debug")
@DisplayOrder(order = { "useDefaultKeys", "valuesToSet"})
public class AddExtendedLoggingContext extends ServiceImp {

  @AdvancedConfig
  @InputFieldDefault(value = "true")
  private Boolean useDefaultKeys;

  @NotNull
  @AutoPopulated
  @InputFieldHint(expression = true)
  private KeyValuePairList valuesToSet;

  public AddExtendedLoggingContext() {
    super();
    setValuesToSet(new KeyValuePairList());
  }


  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      if(useDefaultKeys()){
        addFromObjectHeader(msg, CoreConstants.CHANNEL_ID_KEY);
        addFromObjectHeader(msg, CoreConstants.WORKFLOW_ID_KEY);
        addFromObjectHeader(msg, CoreConstants.MESSAGE_UNIQUE_ID_KEY);
      }
      for(KeyValuePair pair: getValuesToSet()) {
        MDC.put(msg.resolve(pair.getKey()), msg.resolve(pair.getValue()));
      }
    }
    catch (IllegalArgumentException | IllegalStateException e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  private void addFromObjectHeader(AdaptrisMessage msg, String key){
    if(msg.getObjectHeaders().containsKey(key)){
      MDC.put(key, String.valueOf(msg.getObjectHeaders().get(key)));
    }
  }

  @Override
  protected void initService() throws CoreException {
  }

  @Override
  protected void closeService() {
  }

  @Override
  public void prepare() throws CoreException {
  }

  public KeyValuePairList getValuesToSet() {
    return valuesToSet;
  }

  /**
   * Set the list of values to set
   *
   * @param values the mapping to add
   */
  public void setValuesToSet(KeyValuePairList values) {
    valuesToSet = Args.notNull(values, "values");
  }

  AddExtendedLoggingContext withValuesToSet(KeyValuePairList values){
    setValuesToSet(values);
    return this;
  }

  /**
   * <p>
   * Sets whether to write channel, workflow and message id into the Mapped Diagnostic Context based on object metadata.
   * The following keys will be added:
   * </p>
   * <ul>
   *     <li>channelid</li>
   *     <li>workflowid</li>
   *     <li>messageuniqueid</li>
   * </ul>
   *
   * @param useDefaultKeys whether to populate the Mapped Diagnostic Context
   */
  public void setUseDefaultKeys(Boolean useDefaultKeys) {
    this.useDefaultKeys = useDefaultKeys;
  }

  /**
   * <p>
   * Return whether the default keys will be populated
   * </p>
   *
   * @return whether the default keys will be populated
   */
  public Boolean getUseDefaultKeys() {
    return useDefaultKeys;
  }

  boolean useDefaultKeys() {
    return BooleanUtils.toBooleanDefaultIfNull(getUseDefaultKeys(), true);
  }

  AddExtendedLoggingContext withUseDefaultKeys(Boolean useDefaultKeys){
    setUseDefaultKeys(useDefaultKeys);
    return this;
  }

}
