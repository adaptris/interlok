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

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
* {@link ExceptionReportGenerator} implementation that inserts the entire stack trace of the exception as the configured element.
*
* <p>
* Currently the only {@link ExceptionReportGenerator} implementation, this can be used as part of a {@link ExceptionReportService}
* to add the stack trace of the exception wrapped as an XML element of your choosing (as dictated by
* {@link #setElementName(String)}). The data in the element itself is wrapped in a CDATA tag.
* </p>
*
* @config simple-exception-report
* @author lchan
*/
@JacksonXmlRootElement(localName = "simple-exception-report")
@XStreamAlias("simple-exception-report")
@DisplayOrder(order = {"elementName"})
public class SimpleExceptionReport implements ExceptionReportGenerator {

private transient Logger logR = LoggerFactory.getLogger(this.getClass());

@InputFieldDefault(value = "Exception")
private String elementName;

public SimpleExceptionReport() {
}

public SimpleExceptionReport(String elementName) {
this();
setElementName(elementName);
}

public Document create(Exception e, String workflow, String location) throws Exception {
StringWriter sw = new StringWriter();
PrintWriter pw = new PrintWriter(sw, true);
pw.println("<" + elementName() + ">");
pw.println("<![CDATA[");
e.printStackTrace(pw);
pw.println("]]>");
pw.println("</" + elementName() + ">");
pw.close();
logR.trace("Created Exception Report " + sw.toString());
return createDocument(sw.toString(), (DocumentBuilderFactoryBuilder) null);
}

public String getElementName() {
return elementName;
}

/**
* Set the element name for the stack trace.
*
* @param s the element name, if not specified defaults to {@code Exception}
*/
public void setElementName(String s) {
this.elementName = s;
}

String elementName() {
return StringUtils.defaultIfBlank(getElementName(), "Exception");
}
}
