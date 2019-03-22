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

package com.adaptris.core.services.dynamic;

import static com.adaptris.util.text.mime.MimeConstants.ENCODING_7BIT;
import static com.adaptris.util.text.mime.MimeConstants.ENCODING_8BIT;
import static com.adaptris.util.text.mime.MimeConstants.ENCODING_BASE64;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceList;
import com.adaptris.core.XStreamMarshaller;
import com.adaptris.core.common.MetadataDataInputParameter;
import com.adaptris.core.services.LogMessageService;
import com.adaptris.core.services.metadata.AddMetadataService;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.text.mime.MultiPartOutput;
import com.adaptris.util.text.mime.SelectByContentId;
import com.adaptris.util.text.mime.SelectByPosition;

public class DynamicServiceExecutorTest extends DynamicServiceExample {

  private static final String DEFAULT_DEST = "TheDestination";
  private static final String DEFAULT_SRC = "TheSource";
  private static final String DEFAULT_TYPE = "TheType";

  private static enum ExtractorImpl {

    DEFAULT {

      @Override
      boolean matches(ServiceExtractor e) {
        return e instanceof DefaultServiceExtractor;
      }

      @Override
      ServiceExtractor create() {
        return new DefaultServiceExtractor();
      }

      @Override
      String getExampleCommentHeader() throws Exception {
        return "\n<!--" + "\nUsing this extractor implementation, the expectation is that the message payload contains"
            + "\na well formed service which can be unmarshalled and executed " + "e.g. something like:\n"
            + createMessage(new ServiceList(new Service[]
            {
              new LogMessageService()
            })).getContent() + "\n-->\n";
      }
    },
    URL {
      @Override
      boolean matches(ServiceExtractor e) {
        return e instanceof ServiceFromUrl;
      }

      @Override
      ServiceExtractor create() {
        return new ServiceFromUrl("file:///path/to/store/%message{SOURCE}/%message{DEST}/%message{MSG_TYPE}.xml");
      }

      @Override
      String getExampleCommentHeader() throws Exception {
        return "\n<!--" + "\nUsing this extractor implementation, the expectation is that a resolvable URL"
            + "\nwill contain a well formed service that can be unmarshalled and executed."
            + "\ne.g. something like:\n"
            + createMessage(new ServiceList(new Service[]
            {
              new LogMessageService()
            })).getContent() + "\n-->\n";
      }
      
    },
    DATA_INPUT {
      @Override
      boolean matches(ServiceExtractor e) {
        return e instanceof ServiceFromDataInputParameter;
      }

      @Override
      ServiceExtractor create() {
        return new ServiceFromDataInputParameter(new MetadataDataInputParameter("metadataKey"));
      }

      @Override
      String getExampleCommentHeader() throws Exception {
        return "\n<!--"
            + "\nUsing this extractor implementation, the expectation is that the configured"
            + "\nDataInputParameter<String> will resolve to a "
            + "\nwill contain a well formed service that can be unmarshalled and executed."
            + "\ne.g. something like:\n"
            + createMessage(new ServiceList(new Service[]
            {
              new LogMessageService()
            })).getContent() + "\n-->\n";
      }
    },
    MIME {

      @Override
      boolean matches(ServiceExtractor e) {
        return e instanceof MimeServiceExtractor;

      }

      @Override
      ServiceExtractor create() {
        return new MimeServiceExtractor(new SelectByPosition(0));
      }

      @Override
      String getExampleCommentHeader() throws Exception {
        return "\n<!--" + "\nUsing this extractor implementation, the expectation is that the message payload contains"
            + "\nmultiple MIME parts one of which is a well formed service which can be unmarshalled and executed"
            + "\ne.g. something like:\n" + createMimeMessage(new ServiceList(new Service[]
            {
              new LogMessageService()
            }), ENCODING_8BIT).getContent() + "\n-->\n";
      }

    };
    abstract boolean matches(ServiceExtractor e);

    abstract ServiceExtractor create();

    abstract String getExampleCommentHeader() throws Exception;
  };

  public DynamicServiceExecutorTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
  }

  @Override
  protected void tearDown() throws Exception {
  }

  @Override
  protected String getExampleCommentHeader(Object object) {
    String result = super.getExampleCommentHeader(object);
    DynamicServiceExecutor service = (DynamicServiceExecutor) object;
    try {
      for (ExtractorImpl ex : ExtractorImpl.values()) {
        if (ex.matches(service.getServiceExtractor())) {
          result += ex.getExampleCommentHeader();
        }
      }
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
    return result;
  }

  @Override
  protected List<DynamicServiceExecutor> retrieveObjectsForSampleConfig() {
    List<DynamicServiceExecutor> result = new ArrayList<DynamicServiceExecutor>();
    for (ExtractorImpl ex : ExtractorImpl.values()) {
      result.add(new DynamicServiceExecutor(ex.create()));
    }
    return result;
  }

  @Override
  protected String createBaseFileName(Object object) {
    DynamicServiceExecutor obj = (DynamicServiceExecutor) object;
    return super.createBaseFileName(obj) + "-" + obj.getServiceExtractor().getClass().getSimpleName();
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return null;
  }

  public void testSetExtractor() {
    DynamicServiceExecutor service = new DynamicServiceExecutor();
    assertEquals(DefaultServiceExtractor.class, service.getServiceExtractor().getClass());

    service.setServiceExtractor(new MimeServiceExtractor());
    assertEquals(MimeServiceExtractor.class, service.getServiceExtractor().getClass());

    try {
      service.setServiceExtractor(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    assertEquals(MimeServiceExtractor.class, service.getServiceExtractor().getClass());
  }

  public void testDoService_DefaultServiceExtractor() throws Exception {
    AddMetadataService metadataService = new AddMetadataService();
    metadataService.addMetadataElement(new MetadataElement(getName(), getName()));
    DynamicServiceExecutor dynamicService = createService();
    AdaptrisMessage msg = createMessage(metadataService);
    execute(dynamicService, msg);
    assertEquals(getName(), msg.getMetadataValue(getName()));
  }

  public void testDoService_DefaultServiceExtractor_WithMarshaller() throws Exception {
    AddMetadataService metadataService = new AddMetadataService();
    metadataService.addMetadataElement(new MetadataElement(getName(), getName()));
    DynamicServiceExecutor dynamicService = createService();
    dynamicService.setMarshaller(new XStreamMarshaller());
    AdaptrisMessage msg = createMessage(new ServiceList(new Service[]
    {
      metadataService
    }));
    execute(dynamicService, msg);
    assertEquals(getName(), msg.getMetadataValue(getName()));
  }

  public void testDoService_DefaultServiceExtractor_NotService() throws Exception {
    DynamicServiceExecutor dynamicService = createService();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(getName());
    try {
      execute(dynamicService, msg);
      fail();
    }
    catch (ServiceException expected) {
      expected.printStackTrace();
    }
  }


  public void testDoService_DefaultServiceExtractor_SwallowException() throws Exception {
    DynamicServiceExecutor dynamicService = createService();
    dynamicService.setTreatNotFoundAsError(false);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(getName());
    execute(dynamicService, msg);
  }

  public void testDoService_MimeServiceExtractor_NullSelector() throws Exception {
    AddMetadataService metadataService = new AddMetadataService();
    metadataService.addMetadataElement(new MetadataElement(getName(), getName()));
    DynamicServiceExecutor dynamicService = createService();
    dynamicService.setServiceExtractor(new MimeServiceExtractor());
    AdaptrisMessage msg = createMimeMessage(new ServiceList(new Service[]
    {
      metadataService
    }), ENCODING_BASE64);
    try {
      execute(dynamicService, msg);
      fail();
    }
    catch (ServiceException expected) {

    }
  }

  public void testDoService_MimeServiceExtractor_ByPosition() throws Exception {
    AddMetadataService metadataService = new AddMetadataService();
    metadataService.addMetadataElement(new MetadataElement(getName(), getName()));
    DynamicServiceExecutor dynamicService = createService();
    dynamicService.setServiceExtractor(new MimeServiceExtractor(new SelectByPosition(0)));
    AdaptrisMessage msg = createMimeMessage(new ServiceList(new Service[]
    {
      metadataService
    }), ENCODING_7BIT);
    execute(dynamicService, msg);
    assertEquals(getName(), msg.getMetadataValue(getName()));
  }

  public void testDoService_MimeServiceExtractor_ByContentId_NotFound() throws Exception {
    AddMetadataService metadataService = new AddMetadataService();
    metadataService.addMetadataElement(new MetadataElement(getName(), getName()));
    DynamicServiceExecutor dynamicService = createService();
    dynamicService.setServiceExtractor(new MimeServiceExtractor(new SelectByContentId("Blah")));
    AdaptrisMessage msg = createMimeMessage(new ServiceList(new Service[]
    {
      metadataService
    }), ENCODING_BASE64);
    try {
      execute(dynamicService, msg);
      fail();
    }
    catch (ServiceException expected) {

    }
  }

  public void testDoService_MimeServiceExtractor_ByContentId() throws Exception {
    AddMetadataService metadataService = new AddMetadataService();
    metadataService.addMetadataElement(new MetadataElement(getName(), getName()));
    DynamicServiceExecutor dynamicService = createService();
    dynamicService.setServiceExtractor(new MimeServiceExtractor(new SelectByContentId("Service")));
    AdaptrisMessage msg = createMimeMessage(new ServiceList(new Service[]
    {
      metadataService
    }), ENCODING_8BIT);
    execute(dynamicService, msg);
    assertEquals(getName(), msg.getMetadataValue(getName()));
  }

  private DynamicServiceExecutor createService() {
    DynamicServiceExecutor service = new DynamicServiceExecutor();
    service.registerEventHandler(null);
    return service;
  }

  public static AdaptrisMessage createMessage(Service s) throws Exception {
    String xml = DefaultMarshaller.getDefaultMarshaller().marshal(s);
    return AdaptrisMessageFactory.getDefaultInstance().newMessage(xml);
  }

  public static AdaptrisMessage createMimeMessage(Service s, String encoding) throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    String xml = DefaultMarshaller.getDefaultMarshaller().marshal(s);
    MultiPartOutput output = new MultiPartOutput(new GuidGenerator().getUUID());
    output.getMimeHeader().addHeader("Subject", "This is the Subject");
    output.addPart(xml, encoding, "Service");
    output.addPart("pack my jug with a dozen liquor jugs", encoding, "part2");
    try (OutputStream out = msg.getOutputStream()) {
      output.writeTo(out);
    }
    return msg;
  }

}
