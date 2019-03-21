/*******************************************************************************
 * Copyright 2019 Adaptris Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.adaptris.core.services.dynamic;

import java.io.InputStream;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.apache.commons.io.IOUtils;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.common.ConstantDataInputParameter;
import com.adaptris.core.common.MetadataDataInputParameter;
import com.adaptris.core.common.StringPayloadDataInputParameter;
import com.adaptris.core.util.Args;
import com.adaptris.interlok.config.DataInputParameter;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Extract the service to execute based on the configured {@link DataInputParameter}
 * 
 * <p>
 * Wraps {@code DataInputParameter<String>} so you can use any of those implementations as the
 * source of your executable service.
 * </p>
 * 
 * @config dynamic-service-from-data-input
 * @see DynamicServiceExecutor
 * @see ConstantDataInputParameter
 * @see StringPayloadDataInputParameter
 * @see MetadataDataInputParameter
 *
 */
@XStreamAlias("dynamic-service-from-data-input")
@ComponentProfile(summary = "Extract the service to execute from a DataInputParameter")
public class ServiceFromDataInputParameter implements ServiceExtractor {

  @NotNull
  @Valid
  private DataInputParameter<String> input;

  public ServiceFromDataInputParameter() {

  }

  public ServiceFromDataInputParameter(DataInputParameter<String> input) {
    this();
    setInput(input);
  }

  @Override
  public InputStream getInputStream(AdaptrisMessage m) throws Exception {
    Args.notNull(input, "input");
    return IOUtils.toInputStream(input.extract(m), m.getContentEncoding());
  }

  public DataInputParameter<String> getInput() {
    return input;
  }

  public void setInput(DataInputParameter<String> input) {
    this.input = Args.notNull(input, "input");
  }

}
