/*
 * Copyright 2015 Adaptris Ltd.
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
 */

package com.adaptris.core.services.path;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.common.Execution;
import com.adaptris.core.common.StringPayloadDataInputParameter;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.config.DataInputParameter;
import com.adaptris.interlok.config.DataOutputParameter;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * <p>
 * This service allows you to configure an regular expression which will be executed on source data, the result of which can be
 * saved to multiple locations.
 * </p>
 *
 * 
 * @since 3.2.1
 * @author amcgrath
 * @config regexp-service
 * 
 */
@XStreamAlias("regexp-service")
@AdapterComponent
@ComponentProfile(summary = "Extract data via a regular expression and store it", tag = "service,xml")
@DisplayOrder(order = {"regexpSource", "executions"})
public class RegexpService extends ServiceImp {

  @NotNull
  @AutoPopulated
  @Valid
  private DataInputParameter<String> regexpSource;

  @NotNull
  @Valid
  @AutoPopulated
  @XStreamImplicit(itemFieldName = "regexp-execution")
  private List<Execution> executions;

  private transient List<RegexpWrapper> executors;

  public RegexpService() {
    this.setExecutions(new ArrayList<Execution>());
    this.setRegexpSource(new StringPayloadDataInputParameter());
  }

  // @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      for (RegexpWrapper qe : executors) {
        qe.execute(getRegexpSource().extract(msg), msg);
      }
    } catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  @Override
  public void prepare() throws CoreException {}


  @Override
  protected void initService() throws CoreException {
    executors = new ArrayList<>();
    for (Execution execution : this.getExecutions()) {
      executors.add(new RegexpWrapper(execution.getSource(), execution.getTarget()));
    }
  }

  @Override
  protected void closeService() {}

  public DataInputParameter<String> getRegexpSource() {
    return regexpSource;
  }

  public void setRegexpSource(DataInputParameter<String> src) {
    this.regexpSource = Args.notNull(src, "source");
  }

  public List<Execution> getExecutions() {
    return executions;
  }

  public void setExecutions(List<Execution> list) {
    this.executions = Args.notNull(list, "regexp executions");
  }


  private class RegexpWrapper {
    private transient DataInputParameter<String> input;
    private transient DataOutputParameter<String> output;
    private transient Pattern pattern = null;

    RegexpWrapper(DataInputParameter<String> in, DataOutputParameter<String> out) {
      input = in;
      output = out;
    }

    void execute(String src, AdaptrisMessage msg) throws InterlokException {
      String sourcePattern = input.extract(msg);
      if (pattern == null || !pattern.pattern().equals(sourcePattern)) {
        pattern = Pattern.compile(sourcePattern);
      }
      Matcher matcher = pattern.matcher(src);
      if (matcher.find()) {
        output.insert(matcher.group(1), msg);
      }
    }
  }
}
