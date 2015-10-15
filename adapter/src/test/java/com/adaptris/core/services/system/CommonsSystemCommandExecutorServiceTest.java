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

import static org.apache.commons.lang.StringUtils.strip;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.Executor;
import org.apache.commons.lang.StringUtils;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.GeneralServiceExample;
import com.adaptris.core.ServiceException;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.TimeInterval;
import com.adaptris.util.system.Os;

public class CommonsSystemCommandExecutorServiceTest extends GeneralServiceExample {

  private static final String VALUE_ENV = "SOME_VALUE";
  private static final String KEY_ENV = "SOME_ENV";

  private static final String KEY_CMD = "CmdMetadataKey";
  private static final String EXE_ECHO = "echo";

  private static final String ECHO_PARAM = "Hello World";

  private static final List<String> CMD_QUOTED_WINDOWS = Arrays.asList(new String[]
  {
      "c:\\windows\\system32\\ping.exe", "127.0.0.1", "-n", "1"
  });

  private static final List<String> CMD_QUOTED_UNIX = Arrays.asList(new String[]
  {
      "/bin/sleep", "1"
  });


  private static final List<String> CMD_TIMEOUT_WINDOWS = Arrays.asList(new String[]
  {
      "c:\\windows\\system32\\ping.exe", "127.0.0.1", "-n", "20"
  });

  // private static final List<String> CMD_TIMEOUT_WINDOWS = Arrays.asList(new String[]
  // {
  // "cmd", "/c", "choice", "/c", "YN", "/D", "Y", "/T", "10"
  // });
  // private static final List<String> CMD_TIMEOUT_WINDOWS = Arrays.asList(new String[]
  // {
  // "cmd", "/c", "pause"
  // });
  // private static final List<String> CMD_TIMEOUT_WINDOWS = Arrays.asList(new String[]
  // {
  // "cmd", "/c", "timeout", "10"
  // });
  private static final List<String> CMD_TIMEOUT_UNIX = Arrays.asList(new String[]
  {
      "bash", "-c", "/bin/sleep 10"
  });

  private static final List<String> CMD_ECHO_WINDOWS = Arrays.asList(new String[]
  {
      "cmd", "/c"
  });

  // Normally we would just use the echo command directly.
  // it doesn't play nicely when trying to echo out an environment variable.
  // This is just the easiest way to do it.
  private static final List<String> CMD_ECHO_UNIX = Arrays.asList(new String[]
  {
      "bash", "-c"
  });

  private String oldThreadName;

  public CommonsSystemCommandExecutorServiceTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  protected void setUp() throws Exception {
    oldThreadName = Thread.currentThread().getName();
  }

  protected void tearDown() throws Exception {
    Thread.currentThread().setName(oldThreadName);
  }

  public void testSetCommandBuilder() throws Exception {
    SystemCommandExecutorService service = new SystemCommandExecutorService();
    assertNotNull(service.getCommandBuilder());
    assertEquals(DefaultCommandBuilder.class, service.getCommandBuilder().getClass());
    
    CommandBuilder dummy = new CommandBuilder() {
      @Override
      public CommandLine createCommandLine(AdaptrisMessage msg) throws CoreException {
        return new CommandLine("echo");
      }

      @Override
      public Executor configure(Executor exec) throws CoreException {
        return exec;
      }

      @Override
      public Map<String, String> createEnvironment(AdaptrisMessage msg) throws CoreException {
        return null;
      }
    };
    service.setCommandBuilder(dummy);
    assertEquals(dummy, service.getCommandBuilder());

    try {
      service.setCommandBuilder(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    assertEquals(dummy, service.getCommandBuilder());
  }

  public void testSetOutputCapture() throws Exception {
    SystemCommandExecutorService service = new SystemCommandExecutorService();
    assertNotNull(service.getOutputCapture());
    assertEquals(IgnoreOutput.class, service.getOutputCapture().getClass());

    service.setOutputCapture(new OverwritePayload());
    assertEquals(OverwritePayload.class, service.getOutputCapture().getClass());
    try {
      service.setOutputCapture(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    assertEquals(OverwritePayload.class, service.getOutputCapture().getClass());
  }


  public void testDoService() throws Exception {
    Thread.currentThread().setName(getName());
    SystemCommandExecutorService service = new SystemCommandExecutorService(createEchoCommand(false), new OverwritePayload());
    AdaptrisMessage msg = createMessage();
    try {
      execute(service, msg);
    } catch (ServiceException e) {
      System.err.println(getName() + "=" + msg.getStringPayload());
      e.printStackTrace(System.err);
      throw e;
    }
  }

  public void testDoService_WithWorkingDir() throws Exception {
    Thread.currentThread().setName(getName());
    DefaultCommandBuilder builder = createEchoCommand(false);
    builder.setWorkingDirectory(".");
    SystemCommandExecutorService service = new SystemCommandExecutorService(builder, new IgnoreOutput());
    AdaptrisMessage msg = createMessage();
    execute(service, msg);
  }

  public void testDoService_WithStaticEnvironment() throws Exception {
    Thread.currentThread().setName(getName());
    DefaultCommandBuilder builder = createEchoCommandWithEnvVar(KEY_ENV);
    builder.getEnvironmentProperties().add(new KeyValuePair(KEY_ENV, VALUE_ENV));

    SystemCommandExecutorService service = new SystemCommandExecutorService(builder, new OverwritePayload());

    AdaptrisMessage msg = createMessage();
    execute(service, msg);
    assertEquals(VALUE_ENV, StringUtils.strip(msg.getStringPayload()));
  }

  public void testDoService_WithMetadataEnvironment() throws Exception {
    Thread.currentThread().setName(getName());
    DefaultCommandBuilder builder = createEchoCommandWithEnvVar(KEY_ENV);

    builder.addEnvironmentMetadataKey(KEY_ENV);
    builder.addEnvironmentMetadataKey(VALUE_ENV);

    SystemCommandExecutorService service = new SystemCommandExecutorService(builder, new OverwritePayload());

    AdaptrisMessage msg = createMessage();
    execute(service, msg);
    assertEquals(VALUE_ENV, strip(msg.getStringPayload()));
  }

  public void testDoService_CaptureOutput() throws Exception {
    Thread.currentThread().setName(getName());
    SystemCommandExecutorService service = new SystemCommandExecutorService(createEchoCommand(false), new OverwritePayload());
    AdaptrisMessage msg = createMessage();
    execute(service, msg);
    assertEquals("Hello World", strip(msg.getStringPayload()));
  }
  
  public void testDoService_CaptureOutputToMetaData() throws Exception {
    final String METADATA_KEY = "commandOutput";
    
    Thread.currentThread().setName(getName());
    AddMetaDataValue addMetaDataValue = new AddMetaDataValue();
    addMetaDataValue.setMetadataKey(METADATA_KEY);
    addMetaDataValue.setStrip(true);
    SystemCommandExecutorService service = new SystemCommandExecutorService(createEchoCommand(false), addMetaDataValue);
    AdaptrisMessage msg = createMessage();
    execute(service, msg);
    assertEquals("Hello World", msg.getMetadataValue(METADATA_KEY));
  }

  public void testDoService_MetadataArguments_CaptureOutput() throws Exception {
    Thread.currentThread().setName(getName());
    SystemCommandExecutorService service = new SystemCommandExecutorService(createEchoCommand(true), new OverwritePayload());
    AdaptrisMessage msg = createMessage();
    execute(service, msg);
    assertEquals("Hello World", strip(msg.getStringPayload()));
  }

  public void testDoService_Failure() throws Exception {
    Thread.currentThread().setName(getName());
    DefaultCommandBuilder builder = createEchoCommand(false);
    builder.setSuccessExitCode(1);
    SystemCommandExecutorService service = new SystemCommandExecutorService(builder, new OverwritePayload());
    AdaptrisMessage msg = createMessage();
    try {
      execute(service, msg);
      fail();
    }
    catch (ServiceException expected) {
    }
  }

  public void testDoService_ExceedsTimeout() throws Exception {
    Thread.currentThread().setName(getName());
    SystemCommandExecutorService service = new SystemCommandExecutorService(createTimeoutCommand(), new OverwritePayload());
    service.setTimeout(new TimeInterval(2L, TimeUnit.SECONDS));
    AdaptrisMessage msg = createMessage();
    try {
      execute(service, msg);
      fail();
    }
    catch (ServiceException expected) {
      System.err.println(getName() + "=" + msg.getStringPayload());
      expected.printStackTrace(System.err);
    }
  }

  public void testDoService_Quoted() throws Exception {
    Thread.currentThread().setName(getName());
    SystemCommandExecutorService service = new SystemCommandExecutorService(createQuotedCommand(), new OverwritePayload());
    AdaptrisMessage msg = createMessage();
    try {
      execute(service, msg);
    }
    catch (ServiceException e) {
      System.err.println(getName() + "=" + msg.getStringPayload());
      e.printStackTrace(System.err);
      throw e;
    }
  }

  private AdaptrisMessage createMessage() throws Exception {
    AdaptrisMessage result = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    String cmd = EXE_ECHO + " " + ECHO_PARAM;
    result.addMetadata(KEY_CMD, cmd);
    result.addMetadata(KEY_ENV, VALUE_ENV);
    return result;
  }

  private DefaultCommandBuilder createEchoCommand(boolean useMetadata) {
    // Standard command is echo "Hello World"
    DefaultCommandBuilder builder = createBaseCommandBuilder();
    String cmd = EXE_ECHO + " " + ECHO_PARAM;

    if (!useMetadata) {
      builder.getArguments().add(new StaticCommandArgument(cmd));
    }
    else {
      builder.getArguments().add(new MetadataCommandArgument(KEY_CMD));
    }
    return builder;
  }

  private DefaultCommandBuilder createBaseCommandBuilder() {
    return createCommandBuilder((Os.isFamily(Os.WINDOWS_FAMILY)) ? CMD_ECHO_WINDOWS : CMD_ECHO_UNIX);
  }

  private DefaultCommandBuilder createEchoCommandWithEnvVar(String envVar) {
    DefaultCommandBuilder builder = createBaseCommandBuilder();
    String cmd = EXE_ECHO + " " + formatEnvironmentVariable(envVar);
    builder.getArguments().add(new StaticCommandArgument(cmd));
    return builder;
  }

  private DefaultCommandBuilder createTimeoutCommand() throws Exception {
    return createCommandBuilder((Os.isFamily(Os.WINDOWS_FAMILY)) ? CMD_TIMEOUT_WINDOWS : CMD_TIMEOUT_UNIX);
  }

  private DefaultCommandBuilder createQuotedCommand() throws Exception {
    DefaultCommandBuilder builder = createCommandBuilder((Os.isFamily(Os.WINDOWS_FAMILY)) ? CMD_QUOTED_WINDOWS : CMD_QUOTED_UNIX);
    builder.setQuoteHandling(true);
    return builder;
  }

  private DefaultCommandBuilder createCommandBuilder(List<String> commands) {
    DefaultCommandBuilder builder = new DefaultCommandBuilder();
    builder.setExecutablePath(commands.get(0));
    if (commands.size() > 1) {
      for (int i = 1; i < commands.size(); i++) {
        builder.getArguments().add(new StaticCommandArgument(commands.get(i)));
      }
    }
    return builder;
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    DefaultCommandBuilder builder = new DefaultCommandBuilder();
    builder.setExecutablePath("iptables");
    builder.getArguments().add(new StaticCommandArgument("-L"));
    builder.getArguments().add(new MetadataCommandArgument("iptables-chain"));
    builder.getArguments().add(new StaticCommandArgument("-n"));
    return new SystemCommandExecutorService(builder, new OverwritePayload());
  }

  private String formatEnvironmentVariable(String envVar) {
    if (Os.isFamily(Os.WINDOWS_FAMILY)) {
      return "%" + envVar + "%";
    }
    return "$" + envVar;
  }

  @Override
  protected String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o) + "<!--" + "\nThis service allows you to execute an arbitrary system command."
        + "\nIt is considered dangerous if there is insufficient validation of possible command variants."
        + "\nNote that the command is not checked, and will be executed with the same rights as the user"
        + "\nrunning the adapter." + "\n\nIn this example we are executing the command 'iptables -L INPUT -n' where"
        + "\nINPUT is derived from the metadata key 'iptables-chain' and the other parameters are static."
        + "\nAll output is captured, and overrides the existing payload, which will basically give"
        + "\nyou a list of all the iptables input rules."
        + "\nNote that of course, you generally cannot execute iptables unless you are root; so this example"
        + "\nis somewhat meaningless." + "\n-->\n";

  }
}
