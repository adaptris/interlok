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

import static com.adaptris.core.util.XmlHelper.createDocument;

import org.w3c.dom.Document;

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMarshaller;
import com.adaptris.core.XStreamMarshaller;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link ExceptionReportGenerator} implementation that renders the exception as XML.
 * 
 * 
 * @config xml-exception-report
 */
@XStreamAlias("xml-exception-report")
@ComponentProfile(
    summary = "ExceptionReportGenerator implementation that produces XML (without the stacktrace)",
    since = "3.8.4")
public class XmlExceptionReport implements ExceptionReportGenerator {

  public XmlExceptionReport() {
  }

  @Override
  public Document create(Exception e, String workflow, String location) throws Exception {
    // Just use XStream as the marshaller, if we let them configure it, someone might configure
    // a XStreamJSON which would be quite bad.
    AdaptrisMarshaller m = new XStreamMarshaller();
    ExceptionReport report = createReportObject(e, workflow, location);
    return createDocument(m.marshal(report), (DocumentBuilderFactoryBuilder) null);
  }

  protected ExceptionReport createReportObject(Exception e, String workflow, String location) {
    return new ExceptionReport(e, false).withWorkflow(workflow).withExceptionLocation(location);
  }
}
