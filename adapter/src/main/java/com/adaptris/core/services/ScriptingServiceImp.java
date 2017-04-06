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

import java.io.IOException;
import java.io.Reader;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.io.IOUtils;
import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;

/**
 * Base class for enabling JSR223 enabled scripting languages.
 * 
 * 
 * @author lchan
 * 
 */
public abstract class ScriptingServiceImp extends ServiceImp {

  private transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  @NotBlank
  private String language;
  private transient ScriptEngineManager fatController;
  private transient ScriptEngine engine;
  @InputFieldDefault(value = "false")
  @Deprecated
  private Boolean branching;

  @InputFieldDefault(value = "false")
  private Boolean branchingEnabled;

  public ScriptingServiceImp() {
    super();
  }

  @Override
  public final void doService(AdaptrisMessage msg) throws ServiceException {
    Reader input = null;
    try {
      Bindings vars = engine.createBindings();
      vars.put("message", msg);
      vars.put("log", log);
      input = createReader();
      engine.eval(input, vars);
    }
    catch (Exception e) {
      throw new ServiceException(e);
    }
    finally {
      IOUtils.closeQuietly(input);
    }
  }

  protected abstract Reader createReader() throws IOException;

  @Override
  protected void initService() throws CoreException {
    if (language == null) {
      throw new CoreException("Language may not be null");
    }
    fatController = new ScriptEngineManager(this.getClass().getClassLoader());
    engine = fatController.getEngineByName(getLanguage());
    if (engine == null) {
      throw new CoreException("Could not find a ScriptEngine instance for [" + getLanguage() + "]");
    }
    if (getBranching() != null) {
      log.warn("[branching] is deprecated, use [branching-enabled] instead");
    }
  }

  @Override
  protected void closeService() {
  }


  @Override
  public void start() throws CoreException {
    super.start();
  }

  @Override
  public void stop() {
    super.stop();
  }

  public String getLanguage() {
    return language;
  }

  /**
   * Set the language the the script is written in.
   *
   * @param s a JSR223 supported language.
   */
  public void setLanguage(String s) {
    language = s;
  }

  @Override
  public void prepare() throws CoreException {
  }


  @Override
  public boolean isBranching() {
    boolean be = false;
    if (getBranching() != null) {
      be = getBranching().booleanValue();
    } else {
      be = getBranchingEnabled() != null ? getBranchingEnabled().booleanValue() : false;
    }
    return be;
  }

  /**
   * @deprecated since 3.4.0
   */
  @Deprecated
  public Boolean getBranching() {
    return branching;
  }

  /**
   * Specify whether or not this service is branching.
   * 
   * @param branching true to cause {@link #isBranching()} to return true; default is false.
   * @see com.adaptris.core.Service#isBranching()
   * @since 3.0.3
   * @deprecated since 3.4.0 as it causes problems with the UI.
   */
  @Deprecated
  public void setBranching(Boolean branching) {
    this.branching = branching;
  }


  public Boolean getBranchingEnabled() {
    return branchingEnabled;
  }

  /**
   * Specify whether or not this service is branching.
   * 
   * @param branching true to cause {@link #isBranching()} to return true; default is false.
   * @see com.adaptris.core.Service#isBranching()
   * @since 3.4.0
   */
  public void setBranchingEnabled(Boolean branching) {
    this.branchingEnabled = branching;
  }

}
