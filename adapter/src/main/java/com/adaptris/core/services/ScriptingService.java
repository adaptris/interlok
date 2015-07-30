package com.adaptris.core.services;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.BranchingServiceCollection;
import com.adaptris.core.CoreException;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Supports arbitary scripting languges that are supported by JSR223.
 * <p>
 * You should take care when configuring this class; it can present an audit trail issue when used in combination with
 * {@link com.adaptris.core.services.dynamic.DynamicServiceLocator} or
 * {@link com.adaptris.core.services.dynamic.DynamicServiceExecutor} if your script executes arbitrary system commands. In that
 * situation, all commands will be executed with the current users permissions may be subject to the virtual machines security
 * manager.
 * </p>
 * <p>
 * The script is executed and the AdaptrisMessage that is due to be processed is bound against the key "message". This can be used
 * as a standard variable within the script
 * </p>
 * <p>
 * Note that this class can be used as the selector as part of a {@link BranchingServiceCollection}. If used as such, then you need
 * to remember to invoke {@link AdaptrisMessage#setNextServiceId(String)} as part of the script and {@link #setBranching(Boolean)}
 * should be true.
 * <p>
 * 
 * @config scripting-service
 * 
 * @license STANDARD
 * @author lchan
 * 
 */
@XStreamAlias("scripting-service")
public class ScriptingService extends ScriptingServiceImp {

  @NotBlank
  private String scriptFilename;
  public ScriptingService() {
    super();
  }

  public ScriptingService(String uniqueId) {
    this();
    setUniqueId(uniqueId);
  }


  public String getScriptFilename() {
    return scriptFilename;
  }

  /**
   * Set the contents of the script.
   *
   * @param s the script
   */
  public void setScriptFilename(String s) {
    this.scriptFilename = s;
  }

  @Override
  public void init() throws CoreException {
    if (isEmpty(getScriptFilename())) {
      throw new CoreException("script filename is null");
    }
    File f = new File(getScriptFilename());
    if (!f.exists() || !f.isFile() || !f.canRead()) {
      throw new CoreException(getScriptFilename() + " is not accessible");
    }
    super.init();
  }

  @Override
  protected Reader createReader() throws IOException {
    return new FileReader(getScriptFilename());
  }

  @Override
  public boolean isEnabled(License license) throws CoreException {
    return license.isEnabled(LicenseType.Standard);
  }

}
