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

package com.adaptris.core.ftp;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Matchers.matches;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.io.FileFilter;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.CoreException;
import com.adaptris.core.FixedIntervalPoller;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.stubs.MockEncoder;
import com.adaptris.core.stubs.MockMessageListener;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.filetransfer.FileTransferClient;
import com.adaptris.filetransfer.FileTransferException;
import com.adaptris.util.TimeInterval;

public class RelaxedFtpConsumerTest extends RelaxedFtpConsumerCase {
  
  private static final String DIR_ROOT = "/";
  
  private RelaxedFtpConsumer consumer;
  private ConfiguredConsumeDestination consumeDestination;

  @Mock private FtpConnection mockFtpConnection;
  
  @Mock private FileTransferClient mockFileTransferClient;
  
  private MockMessageListener messageListener;

  private StandaloneConsumer standaloneConsumer;
  
  private GregorianCalendar calendarNow;
  private GregorianCalendar calendarOneYearAgo;
  
  public RelaxedFtpConsumerTest(String name) {
    super(name);
  }
  
  public void setUp() throws Exception {
    consumer = new RelaxedFtpConsumer();
    
    MockitoAnnotations.initMocks(this);
    
    consumeDestination = new ConfiguredConsumeDestination("myDestination");
    consumer.setDestination(consumeDestination);
    consumer.registerConnection(mockFtpConnection);
    
    consumer.setPoller(new FixedIntervalPoller(new TimeInterval(1L, TimeUnit.SECONDS)));
    consumer.setReacquireLockBetweenMessages(true);
    
    messageListener = new MockMessageListener(10);
    standaloneConsumer = new StandaloneConsumer(consumer);
    standaloneConsumer.registerAdaptrisMessageListener(messageListener);
    standaloneConsumer.setConnection(mockFtpConnection);
    
    when(mockFtpConnection.retrieveConnection(FileTransferConnection.class)).thenReturn(mockFtpConnection);
    when(mockFtpConnection.connect(consumeDestination.getDestination())).thenReturn(mockFileTransferClient);
    when(mockFtpConnection.getDirectoryRoot(consumeDestination.getDestination())).thenReturn(DIR_ROOT);
    
    calendarNow = new GregorianCalendar();
    calendarOneYearAgo = new GregorianCalendar();
    calendarOneYearAgo.add(Calendar.DAY_OF_YEAR, -1);
  }
  
  public void tearDown() throws Exception {
    LifecycleHelper.stop(consumer);
    LifecycleHelper.close(consumer);
  }
  
  /***********************************************************************************************
   * 
   * 
   * TESTS
   * 
   * 
   ***********************************************************************************************/
  
  public void testSingleFileConsume() throws Exception {
    this.setFilesToConsume(
        new String[] { "/MySingleFile.txt" }, 
        new String[] { "My file payload" },
        new long[] { calendarOneYearAgo.getTimeInMillis() }
    );
    
    LifecycleHelper.init(consumer);
    LifecycleHelper.start(consumer);
    
    this.waitForConsumer(1, 3000);
    
    assertEquals(1, messageListener.getMessages().size());
  }
  
  public void testSingleFileWithWindozeWorkAroundConsume() throws Exception {
    this.setFilesToConsume(
        new String[] { "\\MySingleFile.txt" }, 
        new String[] { "My file payload" },
        new long[] { calendarOneYearAgo.getTimeInMillis() }
    );
    
    when(mockFtpConnection.windowsWorkaround()).thenReturn(true);
    
    LifecycleHelper.init(consumer);
    LifecycleHelper.start(consumer);
    
    this.waitForConsumer(1, 3000);
    
    assertEquals(1, messageListener.getMessages().size());
  }
  
  public void testSingleFileWithEncoderConsume() throws Exception {
    String payload = "My file payload";
    String oldname = Thread.currentThread().getName();
    try {
      Thread.currentThread().setName(getName());
      this.setFilesToConsume(new String[]
      {
          "/MySingleFile.txt"
      }, new String[]
      {
          payload
      }, new long[]
      {
          calendarOneYearAgo.getTimeInMillis()
      });
      consumer.setEncoder(new MockEncoder());

      LifecycleHelper.init(consumer);
      LifecycleHelper.start(consumer);

      this.waitForConsumer(1, 3000);

      assertEquals(1, messageListener.getMessages().size());
      assertEquals(payload, messageListener.getMessages().get(0).getContent());
    }
    finally {
      Thread.currentThread().setName(oldname);
    }
  }
  
  public void testMultipleFileConsume() throws Exception {
    this.setFilesToConsume(
        new String[] { "/MySingleFile.txt" , "/MySingleFile2.txt", "/MySingleFile3.txt" }, 
        new String[] { "My file payload", "My file payload 2", "My file payload 3" },
        new long[] { calendarOneYearAgo.getTimeInMillis(), calendarOneYearAgo.getTimeInMillis(), calendarOneYearAgo.getTimeInMillis() }
    );
    
    LifecycleHelper.init(consumer);
    LifecycleHelper.start(consumer);
    
    this.waitForConsumer(3, 5000);
    
    assertEquals(3, messageListener.getMessages().size());
  }
  
  public void testSingleFileConsumeNotOldEnough() throws Exception {
    this.setFilesToConsume(
        new String[] { "/MySingleFile.txt" }, 
        new String[] { "My file payload" },
        new long[] { calendarNow.getTimeInMillis() + 100000 }
    );
    
    consumer.setOlderThan(new TimeInterval(1L, TimeUnit.MILLISECONDS));
    
    LifecycleHelper.init(consumer);
    LifecycleHelper.start(consumer);
    
    this.waitForConsumer(1, 1000);
    
    assertEquals(0, messageListener.getMessages().size());
  }
  
  public void testSingleFileConsumeDeleteFails() throws Exception {
    this.setFilesToConsume(
        new String[] { "/MySingleFile.txt" }, 
        new String[] { "My file payload" },
        new long[] { calendarOneYearAgo.getTimeInMillis() }
    );
    
    doThrow(new FileTransferException("expected")).when(mockFileTransferClient).delete(anyString());
    
    LifecycleHelper.init(consumer);
    LifecycleHelper.start(consumer);
    
    this.waitForConsumer(1, 3000);
    
    assertEquals(1, messageListener.getMessages().size());
  }
  
  public void testSingleFileConsumeDeleteFailsWithExceptionOnFail() throws Exception {
    this.setFilesToConsume(
        new String[] { "/MySingleFile.txt" }, 
        new String[] { "My file payload" },
        new long[] { calendarOneYearAgo.getTimeInMillis() }
    );
    
    consumer.setFailOnDeleteFailure(true);
    doThrow(new FileTransferException("expected")).when(mockFileTransferClient).delete(anyString());
    
    LifecycleHelper.init(consumer);
    LifecycleHelper.start(consumer);
    
    this.waitForConsumer(1, 3000);
    
    assertEquals(1, messageListener.getMessages().size());
  }
  
  public void testIncorrectPathConsume() throws Exception {
    when(mockFtpConnection.connect(consumeDestination.getDestination()))
        .thenThrow(new FileTransferException("testIncorrectPathConsume"));
    
    this.setFilesToConsume(
        new String[] { "/MySingleFile.txt" }, 
        new String[] { "My file payload" },
        new long[] { calendarOneYearAgo.getTimeInMillis() }
    );
    
    LifecycleHelper.init(consumer);
    LifecycleHelper.start(consumer);
    this.waitForConsumer(1, 1000);
    assertEquals(0, messageListener.getMessages().size());
  }
  
  public void testDirFailsIncorrectPathConsume() throws Exception {
    this.setFilesToConsume(
        new String[] { "/MySingleFile.txt" }, 
        new String[] { "My file payload" },
        new long[] { calendarOneYearAgo.getTimeInMillis() }
    );
    
    when(mockFileTransferClient.dir(DIR_ROOT)).thenThrow(new FileTransferException("testDirFailsIncorrectPathConsume"));
    when(mockFileTransferClient.dir(eq(DIR_ROOT), isA(FileFilter.class)))
        .thenThrow(new FileTransferException("testDirFailsIncorrectPathConsume"));
    
    LifecycleHelper.init(consumer);
    LifecycleHelper.start(consumer);
    this.waitForConsumer(1, 1000);
    assertEquals(0, messageListener.getMessages().size());
  }

  public void testSingleFileWithFilterConsume() throws Exception {
    when(mockFtpConnection.additionalDebug()).thenReturn(true); // just for coverage
    
    this.setFilesToConsume(
        new String[] { "/MySingleFile.txt" }, 
        new String[] { "My file payload" },
        new long[] { calendarOneYearAgo.getTimeInMillis() }
    );
    
    consumer.setDestination(new ConfiguredConsumeDestination("myDestination", "myFilter"));
    consumer.setFileFilterImp("org.apache.oro.io.GlobFilenameFilter");
    
    LifecycleHelper.init(consumer);
    LifecycleHelper.start(consumer);
    
    this.waitForConsumer(1, 3000);
    
    assertEquals(1, messageListener.getMessages().size());
  }
  
  public void testWithIncorrectFilterConsume() throws Exception {
    consumer.setDestination(new ConfiguredConsumeDestination("myDestination", "myFilter"));
    consumer.setFileFilterImp("xxx");
    
    try {
      LifecycleHelper.init(consumer);
      fail("Should fail, cannot create the file filter.");
    } catch (CoreException ex) {
      // expected
    }
  }
  
  
  /***********************************************************************************************
   * 
   * 
   * PRIVATE METHODS
   * 
   * 
   ***********************************************************************************************/
  
  private void setFilesToConsume(final String[] fileNames, final String[] filePayloads, final long[] lastModified)
      throws Exception {
    when(mockFileTransferClient.dir(DIR_ROOT)).thenReturn(fileNames);
    when(mockFileTransferClient.dir(matches(DIR_ROOT), (FileFilter) anyObject())).thenReturn(fileNames);
    for (int i = 0; i < fileNames.length; i++) {
      final int count = i;
      when(mockFileTransferClient.get("/" + fileNames[count])).thenReturn(filePayloads[count].getBytes());
      Mockito.doAnswer(new Answer() {

        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
          OutputStream out = (OutputStream) invocation.getArguments()[0];
          out.write(filePayloads[count].getBytes());
          return null;
        }

      }).when(mockFileTransferClient).get(isA(OutputStream.class), eq("/" + fileNames[count]));
      when(mockFileTransferClient.lastModified("/" + fileNames[count])).thenReturn(lastModified[count]);
    }
  }
  
  private void waitForConsumer(final int numMsgs, final int maxWaitTime) throws Exception {
    final int waitInc = 100;
    int waitTime = 0;
    do {
      Thread.sleep(waitInc);
      waitTime += waitInc;
    } while(messageListener.messageCount() < numMsgs && waitTime < maxWaitTime);

    LifecycleHelper.stop(consumer);    
  }

  @Override
  protected FtpConnection createConnectionForExamples() {
    FtpConnection con = new FtpConnection();
    con.setDefaultUserName("default-username-if-not-specified");
    con.setDefaultPassword("default-password-if-not-specified");

    con.setAdditionalDebug(false);
    return con;
  }

  @Override
  protected String getScheme() {
    return "ftp";
  }
}
