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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.validation.constraints.NotBlank;

import org.apache.commons.lang3.BooleanUtils;

import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.DynamicPollingTemplate;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;

/**
 * Base class for enabling JSR223 enabled scripting languages.
 *
 *
 * @author lchan
 *
 */
public abstract class ScriptingServiceImp extends ServiceImp implements DynamicPollingTemplate.TemplateProvider {

  protected static final String NASHORN_ENGINE = "Oracle Nashorn";
  protected static final String GRAAL_JS_ENGINE = "Graal.js";
  protected static final String NASHORN = "Nashorn";

  @NotBlank
  private String language;
  private transient ScriptEngine engine;

  @InputFieldDefault(value = "false")
  private Boolean branchingEnabled;

  public ScriptingServiceImp() {
    super();
  }

  @Override
  public final void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      Bindings vars = engine.createBindings();
      vars.put("message", msg);
      vars.put("log", log);
      try (Reader input = createReader()) {
        engine.eval(input, vars);
      }
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  protected abstract Reader createReader() throws IOException;

  @Override
  protected void initService() throws CoreException {
    try {
      Args.notBlank(getLanguage(), "language");
      ScriptEngineManager engineManager = new ScriptEngineManager(this.getClass().getClassLoader());
      checkEngine(engineManager);
      String error = String.format("getEngineByName('%s')", getLanguage());
      engine = Args.notNull(engineManager.getEngineByName(getLanguage()), error);
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  protected final void checkEngine(ScriptEngineManager engineManager) {
    if (nashornNames(engineManager).contains(getLanguage())) {
      // Nashorn engine javascript short names ("js", "JS", "JavaScript", "javascript", "ECMAScript", "ecmascript") are also used by GraalJS
      // engine so we only warn the user if he uses directly nashorn or if GraalJS can not be found.
      if (isNashorn(getLanguage()) || !hasGraalJsEngine(engineManager)) {
        log.warn(
            "Nashorn script engine is deprecated and will be removed in Java 17. If you want to keep using a javascript engine you should consider using GraalJS instead.");
      }
    }
  }

  private List<String> nashornNames(ScriptEngineManager engineManager) {
    // List of short names which may be used to identify the Nashorn ScriptEngine.
    return engineManager.getEngineFactories().stream().filter(e -> NASHORN_ENGINE.equals(e.getEngineName()))
        .flatMap(e -> e.getNames().stream())
        .collect(Collectors.toList());
  }

  private boolean isNashorn(String language) {
    return NASHORN.equalsIgnoreCase(language);
  }

  private boolean hasGraalJsEngine(ScriptEngineManager engineManager) {
    return Optional.ofNullable(engineManager.getEngineByName(GRAAL_JS_ENGINE)).isPresent();
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
    language = Args.notBlank(s, "language");
  }

  @Override
  public void prepare() throws CoreException {
  }


  @Override
  public boolean isBranching() {
    return BooleanUtils.toBooleanDefaultIfNull(getBranchingEnabled(), false);
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
    branchingEnabled = branching;
  }

}
