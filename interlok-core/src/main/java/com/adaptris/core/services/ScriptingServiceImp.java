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
import javax.validation.constraints.NotBlank;
import org.apache.commons.lang3.BooleanUtils;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.BranchingServiceCollection;
import com.adaptris.core.CoreException;
import com.adaptris.core.DynamicPollingTemplate;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LoggingHelper;
import com.adaptris.core.util.ScriptingUtil;
import lombok.Getter;
import lombok.Setter;

/**
 * Base class for enabling JSR223 enabled scripting languages.
 *
 *
 * @author lchan
 *
 */
public abstract class ScriptingServiceImp extends ServiceImp implements DynamicPollingTemplate.TemplateProvider {

  /**
   * Set the JSR223 language the the script is written in.
   * <p>
   * Depending on the language choice, you may need additional libraries that are not normally
   * shipped with a standard Interlok distribution.
   * </p>
   */
  @NotBlank
  @Getter
  @Setter
  private String language;
  private transient ScriptEngine engine;

  /**
   * Specify whether or not this service is branching so it can be used as part of a
   * {@link BranchingServiceCollection}.
   *
   */
  @InputFieldDefault(value = "false")
  @AdvancedConfig
  @Getter
  @Setter
  private Boolean branchingEnabled;

  private transient boolean nashornWarningLogged = false;

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
  }


  @Override
  protected void closeService() {
  }

  @Override
  public void prepare() throws CoreException {
    try {
      Args.notBlank(getLanguage(), "language");
      ScriptEngineManager engineManager = new ScriptEngineManager(this.getClass().getClassLoader());
      if (ScriptingUtil.dependsOnNashorn(engineManager, getLanguage())) {
        LoggingHelper.logWarning(nashornWarningLogged, () -> nashornWarningLogged = true,
            "Nashorn script engine is deprecated and will be removed in Java 17. "
                + "If you want to keep using a javascript engine you should consider using GraalJS instead.");
      }
      String error = String.format("getEngineByName('%s')", getLanguage());
      engine = Args.notNull(engineManager.getEngineByName(getLanguage()), error);
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }


  @Override
  public boolean isBranching() {
    return BooleanUtils.toBooleanDefaultIfNull(getBranchingEnabled(), false);
  }

}
