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

package com.adaptris.core.services.system;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.Executor;
import org.apache.commons.lang3.BooleanUtils;
import org.hibernate.validator.constraints.NotBlank;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.util.Args;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.NumberUtils;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Default implementation of {@link CommandBuilder}
 * 
 * 
 * @author lchan
 * 
 */
@XStreamAlias("default-system-command-builder")
@DisplayOrder(order = {"executablePath", "arguments", "workingDirectory", "quoteHandling", "successExitCode"})
public class DefaultCommandBuilder implements CommandBuilder {

  @NotNull
  @AutoPopulated
  @AdvancedConfig
  private List<String> environmentMetadataKeys;
  @NotNull
  @AutoPopulated
  @AdvancedConfig
  private KeyValuePairSet environmentProperties;
  @NotNull
  @AutoPopulated
  private List<CommandArgument> arguments;
  @NotBlank
  private String executablePath;
  @AdvancedConfig
  private String workingDirectory;
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean quoteHandling;
  @AdvancedConfig
  private Integer successExitCode;

  public DefaultCommandBuilder() {
    setEnvironmentMetadataKeys(new ArrayList<String>());
    setEnvironmentProperties(new KeyValuePairSet());
    setArguments(new ArrayList<CommandArgument>());
  }

  @Override
  public CommandLine createCommandLine(AdaptrisMessage msg) {
    CommandLine commandLine = new CommandLine(getExecutablePath());
    for (CommandArgument argument : getArguments()) {
      commandLine.addArgument(argument.retrieveValue(msg), quoteHandling());
    }
    return commandLine;
  }

  @Override
  public Map<String, String> createEnvironment(AdaptrisMessage msg) {
    Map<String, String> env = new HashMap<String, String>();
    for (KeyValuePair kvp : getEnvironmentProperties()) {
      env.put(kvp.getKey(), kvp.getValue());
    }
    for (String key : environmentMetadataKeys) {
      if (msg.headersContainsKey(key)) {
        env.put(key, msg.getMetadataValue(key));
      }
    }
    return env.size() == 0 ? null : env;
  }

  @Override
  public Executor configure(Executor exe) {
    if (!isEmpty(getWorkingDirectory())) {
      File wd = new File(getWorkingDirectory());
      exe.setWorkingDirectory(wd);
    }
    exe.setExitValue(successExitValue());
    return exe;
  }

  public List<String> getEnvironmentMetadataKeys() {
    return environmentMetadataKeys;
  }

  /**
   * Specifies any metadata keys that should be specified as Environment Variables.
   * <p>
   * Each key, if available as metadata, will become the environment variable with each corresponding value, the value.
   * </p>
   * 
   * @param l
   */
  public void setEnvironmentMetadataKeys(List<String> l) {
    environmentMetadataKeys = Args.notNull(l, "environmentMetadataKeys");

  }

  public void addEnvironmentMetadataKey(String key) {
    environmentMetadataKeys.add(Args.notNull(key, "environmentMetadataKey"));
  }

  public String getWorkingDirectory() {
    return workingDirectory;
  }

  /**
   * Specify the working directory for the process.
   * 
   * @param wd the working directory; defaults to null
   */
  public void setWorkingDirectory(String wd) {
    this.workingDirectory = wd;
  }

  public KeyValuePairSet getEnvironmentProperties() {
    return environmentProperties;
  }

  /**
   * Specifies any fixed value environment variables that are necessary.
   * 
   * @param env
   */
  public void setEnvironmentProperties(KeyValuePairSet env) {
    this.environmentProperties = Args.notNull(env, "environment properties");

  }

  public String getExecutablePath() {
    return executablePath;
  }

  /**
   * Specifies the executable to be invoke.
   * <p>
   * This can be an absolute path or else simply the name of an executable on the PATH. Examples might be "/bin/ls" or "echo" on
   * Unix. Note that this defaults to null, which might create undefined behaviour.
   * </p>
   * 
   * @param executable
   */
  public void setExecutablePath(String executable) {
    this.executablePath = executable;
  }

  public List<CommandArgument> getArguments() {
    return arguments;
  }

  /**
   * Specifies a list of command line arguments to be passed into the executable
   * 
   * @param l the arguments
   */
  public void setArguments(List<CommandArgument> l) {
    arguments = Args.notNull(l, "commandArguments");

  }

  public void addArgument(CommandArgument arg) {
    arguments.add(Args.notNull(arg, "commandArgument"));
  }


  public Integer getSuccessExitCode() {
    return successExitCode;
  }

  /**
   * Set the exit code value that is considered successful.
   * 
   * @param i the exit code that is successful; defaults to 0 if not explicitly set.
   */
  public void setSuccessExitCode(Integer i) {
    this.successExitCode = i;
  }

  int successExitValue() {
    return NumberUtils.toIntDefaultIfNull(getSuccessExitCode(), 0);
  }

  public Boolean getQuoteHandling() {
    return quoteHandling;
  }

  /**
   * Specify whether to handle quotes or not.
   * <p>
   * If any argument doesn't include spaces or quotes, return it as is. If it contains double quotes, use single quotes - else
   * surround the argument by double quotes.
   * </p>
   * 
   * @see CommandLine#addArgument(String, boolean)
   * @param b true or false, if not specified defaults to false.
   */
  public void setQuoteHandling(Boolean b) {
    this.quoteHandling = b;
  }

  boolean quoteHandling() {
    return BooleanUtils.toBooleanDefaultIfNull(getQuoteHandling(), false);
  }
}
