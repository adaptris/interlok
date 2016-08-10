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

package com.adaptris.core.services.exception;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.ServiceList;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.StandardProcessingExceptionHandler;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.core.stubs.MockChannel;
import com.adaptris.core.stubs.MockMessageConsumer;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.XmlUtils;
import com.adaptris.util.text.xml.InsertNode;
import com.adaptris.util.text.xml.ReplaceNode;

@SuppressWarnings("deprecation")
public class ExceptionReportServiceTest extends ExceptionServiceExample {

  private static final String XPATH_ORIGINAL_NODE = "/Root/OriginalNode";
  private static final String XPATH_ROOT = "/Root";
  private static String RAW_DATA = "This is Some Data";
  private static String XML_PAYLOAD = "<Root><OriginalNode>" + RAW_DATA + "</OriginalNode></Root>";

  public ExceptionReportServiceTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
  }

  public void testNoObjectMetadata() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
    ExceptionReportService service = new ExceptionReportService(new SimpleExceptionReport(), new ReplaceNode(XPATH_ORIGINAL_NODE));
    execute(service, msg);
    assertEquals(XML_PAYLOAD, msg.getContent());
    XmlUtils xml = XmlHelper.createXmlUtils(msg);
    assertEquals(RAW_DATA, xml.getSingleTextItem(XPATH_ORIGINAL_NODE));
  }

  public void testNonXml() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(RAW_DATA);
    msg.addObjectHeader(CoreConstants.OBJ_METADATA_EXCEPTION, new Exception("This is the exception"));
    ExceptionReportService service = new ExceptionReportService(new SimpleExceptionReport(), new ReplaceNode(XPATH_ORIGINAL_NODE));
    try {
      execute(service, msg);
      fail("success with non-xml payload");
    }
    catch (ServiceException expected) {
      ;
    }
  }

  public void testReplaceNode() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
    msg.addObjectHeader(CoreConstants.OBJ_METADATA_EXCEPTION, new Exception("This is the exception"));
    ExceptionReportService service = new ExceptionReportService(new SimpleExceptionReport(), new ReplaceNode(XPATH_ORIGINAL_NODE));
    execute(service, msg);
    assertNotSame(XML_PAYLOAD, msg.getContent());
    XmlUtils xml = XmlHelper.createXmlUtils(msg);
    assertNotSame(RAW_DATA, xml.getSingleNode(XPATH_ORIGINAL_NODE));
  }

  public void testInsertNode() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
    msg.addObjectHeader(CoreConstants.OBJ_METADATA_EXCEPTION, new Exception("This is the exception"));
    ExceptionReportService service = new ExceptionReportService(new SimpleExceptionReport(), new InsertNode(XPATH_ROOT));
    execute(service, msg);
    assertNotSame(XML_PAYLOAD, msg.getContent());
    XmlUtils xml = XmlHelper.createXmlUtils(msg);
    assertEquals(RAW_DATA, xml.getSingleTextItem(XPATH_ORIGINAL_NODE));
    assertNotNull(xml.getSingleNode(XPATH_ROOT + "/Exception"));
  }

  public void testBug2220() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
    msg.addObjectHeader(CoreConstants.OBJ_METADATA_EXCEPTION, new Exception("I had problems parsing <ABCDE>"));
    ExceptionReportService service = new ExceptionReportService(new SimpleExceptionReport(), new InsertNode(XPATH_ROOT));
    execute(service, msg);
    assertNotSame(XML_PAYLOAD, msg.getContent());
    XmlUtils xml = XmlHelper.createXmlUtils(msg);
    assertEquals(RAW_DATA, xml.getSingleTextItem(XPATH_ORIGINAL_NODE));
    assertNotNull(xml.getSingleNode(XPATH_ROOT + "/Exception"));
  }

  public void testBug2356() throws Exception {
    ServiceImp failingService = new ThrowExceptionService(new ConfiguredException("Fail"));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
    ExceptionReportService service = new ExceptionReportService(new SimpleExceptionReport(), new InsertNode(XPATH_ROOT));
    MockMessageProducer mockProducer = new MockMessageProducer();

    StandardProcessingExceptionHandler speh = new StandardProcessingExceptionHandler(new ServiceList(new Service[]
    {
        service, new StandaloneProducer(mockProducer)
    }));
    MockChannel channel = new MockChannel();
    MockMessageConsumer consumer = new MockMessageConsumer();
    StandardWorkflow wf = new StandardWorkflow();
    wf.getServiceCollection().add(failingService);
    wf.setConsumer(consumer);
    channel.setMessageErrorHandler(speh);
    channel.getWorkflowList().add(wf);
    try {
      channel.prepare();
      channel.requestStart();
      consumer.submitMessage(msg);
      assertEquals(1, mockProducer.getMessages().size());
      AdaptrisMessage failedMessage = mockProducer.getMessages().get(0);
      assertNotSame(XML_PAYLOAD, failedMessage.getContent());
      XmlUtils xml = XmlHelper.createXmlUtils(failedMessage);
      assertEquals(RAW_DATA, xml.getSingleTextItem(XPATH_ORIGINAL_NODE));
      assertNotNull(xml.getSingleNode(XPATH_ROOT + "/Exception"));
      String xmlElement = xml.getSingleTextItem(XPATH_ROOT + "/Exception");
      assertTrue(xmlElement.contains("com.adaptris.core.services.exception.ThrowExceptionService.doService"));
    }
    finally {
      channel.requestClose();
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new ExceptionReportService(new SimpleExceptionReport(), new InsertNode("/path/to/parent/node"));
  }

  @Override
  protected String createBaseFileName(Object o) {
    return super.createBaseFileName(o) + "-" + ((ExceptionReportService) o).getExceptionGenerator().getClass().getSimpleName();
  }
}
