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

import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.util.TimeInterval;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Service that runs the specified system executable with the provided arguments, optionally capturing the output.
 * 
 * <p>
 * Note that no checking is peformed on the command to be executed; it will be executed as-is. If used in combination with
 * {@link com.adaptris.core.services.dynamic.DynamicServiceExecutor} or {@link
 * com.adaptris.core.services.dynamic.DynamicServiceLocator} then you might have a large security hole if it is improperly
 * configured or validated.
 * </p>
 * <p>
 * The following behaviour is non-configurable:
 * <ul>
 * <li>The exitcode is stored against the metadata key {@value #COMMAND_RETURN_VALUE_METADATA_KEY} if the service does not throw an
 * exception.</li>
 * <li>If a timeout occurs then a ServiceException is thrown, output that was captured before the timeout should still be
 * available</li>
 * </ul>
 * </p>
 * 
 * @config system-command-executor
 * 
 */
@XStreamAlias("system-command-executor")
public class SystemCommandExecutorService extends ServiceImp {
  
  public static final String COMMAND_RETURN_VALUE_METADATA_KEY = "SystemCommandExecutorService.ReturnValue";
  private static final TimeInterval DEFAULT_TIMEOUT = new TimeInterval(30L, TimeUnit.SECONDS);

  @Valid
  private TimeInterval timeout;
  @NotNull
  @AutoPopulated
  private CommandBuilder commandBuilder;
  @NotNull
  @Valid
  @AutoPopulated
  private CommandOutputCapture outputCapture;

  
  public SystemCommandExecutorService() {
    this(new DefaultCommandBuilder(), new IgnoreOutput());
  }

  public SystemCommandExecutorService(CommandBuilder builder, CommandOutputCapture capture) {
    setCommandBuilder(builder);
    setOutputCapture(capture);
  }

  /**
   * Invokes the command line executable
   * @see com.adaptris.core.Service#doService(com.adaptris.core.AdaptrisMessage)
   */
  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {

    try (OutputStream out = getOutputCapture().startCapture(msg)) {
      Executor cmd = getCommandBuilder().configure(new DefaultExecutor());
      ExecuteWatchdog watchdog = new ExecuteWatchdog(timeoutMs());
      cmd.setWatchdog(watchdog);
      CommandLine cl = getCommandBuilder().createCommandLine(msg);
      Map<String, String> env = getCommandBuilder().createEnvironment(msg);

      PumpStreamHandler pump = new PumpStreamHandler(out);
      cmd.setStreamHandler(pump);
      log.trace("Executing {}", cl);
      int exit = cmd.execute(cl, env);
      msg.addMetadata(COMMAND_RETURN_VALUE_METADATA_KEY, "" + exit);
    }
    catch (Exception e) {
      rethrowServiceException(e);
    }
  }
  

  @Override
  protected void initService() throws CoreException {
  }

  @Override
  protected void closeService() {
  }

  public CommandBuilder getCommandBuilder() {
    return commandBuilder;
  }

  /**
   * Set the command builder.
   * 
   * @param builder the {@link CommandBuilder} implementation
   * @see DefaultCommandBuilder
   */
  public void setCommandBuilder(CommandBuilder builder) {
    if (builder == null) {
      throw new IllegalArgumentException("Command Builder is null");
    }
    this.commandBuilder = builder;
  }

  @Override
  public void prepare() throws CoreException {
  }

  /**
   * Specifies a maximum time for the executable to run, after which it will be terminated.
   * 
   * @param t the timeout; default if not configured is 30 seconds.
   */
  public void setTimeout(TimeInterval t) {
    this.timeout = t;
  }

  long timeoutMs() {
    return getTimeout() != null ? getTimeout().toMilliseconds() : DEFAULT_TIMEOUT.toMilliseconds();
  }

  public TimeInterval getTimeout() {
    return timeout;
  }

  public CommandOutputCapture getOutputCapture() {
    return outputCapture;
  }

  public void setOutputCapture(CommandOutputCapture outputCapture) {
    if (outputCapture == null) {
      throw new IllegalArgumentException("Command Output Capture is null");
    }
    this.outputCapture = outputCapture;
  }
}
