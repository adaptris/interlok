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

package com.adaptris.core.interceptor;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.MDC;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.services.AddLoggingContext;
import com.adaptris.core.services.RemoveLoggingContext;
import com.adaptris.core.util.Args;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairList;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
* WorkflowInterceptor implementation that adds a mapped diagnostic context via {@code org.slf4j.MDC#put(String, String)}.
* <p>
* An alternative to this interceptor might be {@link AddLoggingContext} and {@link RemoveLoggingContext} as part of the
* service execution chain.
* </p>
*
* @config logging-context-workflow-interceptor
* @see AddLoggingContext
* @see RemoveLoggingContext
*
*/
@JacksonXmlRootElement(localName = "logging-context-workflow-interceptor")
@XStreamAlias("logging-context-workflow-interceptor")
@AdapterComponent
@ComponentProfile(summary = "Interceptor that adds Logging Context at the start of a workflow, removes it at the end",
tag = "interceptor")
public class LoggingContextWorkflowInterceptor extends WorkflowInterceptorImpl {

private static final GuidGenerator GUID = new GuidGenerator();

@AdvancedConfig
@InputFieldDefault(value = "true")
private Boolean useDefaultKeys;

@AdvancedConfig
@InputFieldDefault(value = "false")
private Boolean addDefaultKeysAsObjectMetadata;

@NotNull
@AutoPopulated
@InputFieldHint(expression = true)
private KeyValuePairList valuesToSet;

public LoggingContextWorkflowInterceptor() {
super();
valuesToSet = new KeyValuePairList();
}

public LoggingContextWorkflowInterceptor(String uid) {
this();
setUniqueId(uid);
}

@Override
public synchronized void workflowStart(AdaptrisMessage inputMsg) { }

@Override
public synchronized void processingStart(AdaptrisMessage inputMsg) {
if(useDefaultKeys()) {
addDefaultKey(inputMsg, CoreConstants.CHANNEL_ID_KEY, parentChannel().getUniqueId());
addDefaultKey(inputMsg, CoreConstants.WORKFLOW_ID_KEY, parentWorkflow().getUniqueId());
addDefaultKey(inputMsg, CoreConstants.MESSAGE_UNIQUE_ID_KEY, inputMsg.getUniqueId());
}
for(KeyValuePair pair: getValuesToSet()) {
MDC.put(pair.getKey(), inputMsg.resolve(pair.getValue()));
}
}

private void addDefaultKey(AdaptrisMessage inputMsg, String key, String value) {
if(!isEmpty(value)) {
MDC.put(key, value);
if(addDefaultKeysAsObjectMetadata()) {
inputMsg.addObjectHeader(key, value);
}
}
}

@Override
public synchronized void workflowEnd(AdaptrisMessage inputMsg, AdaptrisMessage outputMsg) {
for(KeyValuePair pair: getValuesToSet()) {
MDC.remove(pair.getKey());
}
if(useDefaultKeys()) {
MDC.remove(CoreConstants.CHANNEL_ID_KEY);
MDC.remove(CoreConstants.WORKFLOW_ID_KEY);
MDC.remove(CoreConstants.MESSAGE_UNIQUE_ID_KEY);
}
}

@Override
public void init() throws CoreException {
}

@Override
public void start() throws CoreException {}

@Override
public void stop() {}

@Override
public void close() {}

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

/**
* <p>
* Sets whether to write channel, workflow and message id into the Mapped Diagnostic Context. The following
* keys will be added for each message:
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

public void setAddDefaultKeysAsObjectMetadata(Boolean addDefaultKeysAsObjectMetadata) {
this.addDefaultKeysAsObjectMetadata = addDefaultKeysAsObjectMetadata;
}

/**
* <p>
* Return whether the default keys will be added to object metadata.
* </p>
*
* @return whether the default keys will be added to object metadata
*/
public Boolean getAddDefaultKeysAsObjectMetadata() {
return addDefaultKeysAsObjectMetadata;
}

boolean addDefaultKeysAsObjectMetadata() {
return BooleanUtils.toBooleanDefaultIfNull(getAddDefaultKeysAsObjectMetadata(), false);
}
}
