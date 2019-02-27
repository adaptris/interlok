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

import com.adaptris.annotation.ComponentProfile;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link ExceptionReportGenerator} implementation that renders the exception as XML including the
 * stacktrace
 * 
 * 
 * @config xml-with-stacktrace-exception-report
 */
@XStreamAlias("xml-with-stacktrace-exception-report")
@ComponentProfile(
    summary = "ExceptionReportGenerator implementation that produces XML (includes the stacktrace)",
    since = "3.8.4")
public class XmlReportWithStacktrace extends XmlExceptionReport {

  public XmlReportWithStacktrace() {
  }

  protected ExceptionReport createReportObject(Exception e, String workflow, String location) {
    // Note that XStream does have a custom handler for StacktraceElements so they will
    // come out as <trace>class.method(class.java:lineno)</trace>
    return new ExceptionReport(e, true).withWorkflow(workflow).withExceptionLocation(location);
  }
}
