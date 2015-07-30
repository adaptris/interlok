package com.adaptris.core.services;

import java.io.IOException;
import java.io.Reader;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.io.IOUtils;
import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;

/**
 * Base class for enabling JSR223 enabled scripting languages.
 * 
 * 
 * @author lchan
 * 
 */
public abstract class ScriptingServiceImp extends ServiceImp {

  @NotBlank
  private String language;
  private transient ScriptEngineManager fatController;
  private transient ScriptEngine engine;
  private Boolean branching;

  public ScriptingServiceImp() {
    super();
  }

  @Override
  public final void doService(AdaptrisMessage msg) throws ServiceException {
    Reader input = null;
    try {
      Bindings vars = engine.createBindings();
      vars.put("message", msg);
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
  public void init() throws CoreException {
    if (language == null) {
      throw new CoreException("Language may not be null");
    }
    fatController = new ScriptEngineManager(this.getClass().getClassLoader());
    engine = fatController.getEngineByName(getLanguage());
    if (engine == null) {
      throw new CoreException("Could not find a ScriptEngine instance for [" + getLanguage() + "]");
    }
  }

  @Override
  public void close() {
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
  public boolean isEnabled(License license) throws CoreException {
    return license.isEnabled(LicenseType.Standard);
  }

  @Override
  public boolean isBranching() {
    return getBranching() != null ? getBranching().booleanValue() : false;
  }

  public Boolean getBranching() {
    return branching;
  }

  /**
   * Specify whether or not this service is branching.
   * 
   * @param branching true to cause {@link #isBranching()} to return true; default is false.
   * @see Service#isBranching()
   * @since 3.0.3
   */
  public void setBranching(Boolean branching) {
    this.branching = branching;
  }

}
