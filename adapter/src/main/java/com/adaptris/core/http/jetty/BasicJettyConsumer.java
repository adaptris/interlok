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

package com.adaptris.core.http.jetty;

import static com.adaptris.core.CoreConstants.HTTP_METHOD;
import static com.adaptris.core.CoreConstants.JETTY_QUERY_STRING;
import static com.adaptris.core.CoreConstants.JETTY_URI;
import static com.adaptris.core.CoreConstants.JETTY_URL;
import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageConsumerImp;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.WorkflowImp;
import com.adaptris.core.WorkflowInterceptor;
import com.adaptris.core.http.client.RequestMethodProvider.RequestMethod;
import com.adaptris.util.TimeInterval;

/**
 * This is the abstract class for all implementations that make use of Jetty to receive messages.
 * 
 * 
 * @author lchan
 * @author $Author: lchan $
 */
public abstract class BasicJettyConsumer extends AdaptrisMessageConsumerImp {
  private static final TimeInterval DEFAULT_MAX_WAIT_TIME = new TimeInterval(600L, TimeUnit.SECONDS);
  private static final TimeInterval DEFAULT_INTERMEDIATE_WAIT_TIME = new TimeInterval(1L, TimeUnit.SECONDS);
  private static final String COMMA = ",";
  private static final List<String> HTTP_METHODS;
  
  private transient Servlet jettyServlet;
  private transient ServletWrapper servletWrapper = null;
  private transient List<String> acceptedMethods = new ArrayList<>(HTTP_METHODS);

  @InputFieldDefault(value = "false")
  private Boolean additionalDebug;
  private TimeInterval maxWaitTime;

  private long warnAfterMessageHangMillis = 20000;

  static {
    List<String> methods = new ArrayList<>();
    for (RequestMethod m : RequestMethod.values()) {
      methods.add(m.name());
    }
    HTTP_METHODS = Collections.unmodifiableList(methods);
  }

  public BasicJettyConsumer() {
    super();
    jettyServlet = new BasicServlet();
  }


  /**
   * Create an AdaptrisMessage from the incoming servlet request and response.
   * 
   * @param request the HttpServletRequest
   * @param response the HttpServletResponse
   * @return an AdaptrisMessage instance.
   * @throws IOException
   * @throws ServletException
   * @see HttpServlet#service(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
   */
  public abstract AdaptrisMessage createMessage(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException;

  private boolean submitToWorkflow(AdaptrisMessage msg) {
    boolean waitForCompletion = false;
    if (retrieveAdaptrisMessageListener() instanceof WorkflowImp) {
      List<WorkflowInterceptor> interceptors = ((WorkflowImp) retrieveAdaptrisMessageListener()).getInterceptors();
      for (WorkflowInterceptor i : interceptors) {
        if (i.getClass().equals(JettyPoolingWorkflowInterceptor.class)) {
          waitForCompletion = true;
          break;
        }
      }
    }
    retrieveAdaptrisMessageListener().onAdaptrisMessage(msg);
    return waitForCompletion;
  }

  protected void logHeaders(HttpServletRequest req) {
    if (additionalDebug()) {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      PrintWriter p = new PrintWriter(out);
      p.println("Received HTTP Headers");
      p.println("URL " + req.getRequestURL());
      p.println("URI " + req.getRequestURI());
      p.println("Query " + req.getQueryString());
      for (Enumeration e = req.getHeaderNames(); e.hasMoreElements();) {
        String key = (String) e.nextElement();
        Enumeration values = req.getHeaders(key);
        StringBuffer sb = new StringBuffer();
        while (values.hasMoreElements()) {
          sb.append(values.nextElement()).append(",");
        }
        String s = sb.toString();
        p.println(key + ": " + s.substring(0, s.lastIndexOf(",")));
      }
      p.close();
      log.trace(out.toString());
    }
  }

  private ServletWrapper asServletWrapper() throws CoreException {
    if (servletWrapper == null) {
      String destination = ensureIsPath(getDestination().getDestination());
      servletWrapper = new ServletWrapper(jettyServlet, destination);
    }
    return servletWrapper;
  }

  // Ensure that if someone types in http://localhost:8080/fred/blah/blah then
  // we make sure that we return /fred/blah/blah
  private String ensureIsPath(String s) throws CoreException {
    String result = s;
    try {
      URI uri = new URI(s);
      result = uri.getPath();
    }
    catch (URISyntaxException e) {
      throw new CoreException(e);
    }
    return result;
  }

  /**
   * 
   * @see com.adaptris.core.AdaptrisComponent#init()
   */
  public void init() throws CoreException {
    if (!isEmpty(getDestination().getFilterExpression())) {
      acceptedMethods = asList(getDestination().getFilterExpression());
      log.trace("Acceptable HTTP methods set to : {}", acceptedMethods);
    }
  }

  /**
   * 
   * @see com.adaptris.core.AdaptrisComponent#start()
   */
  public void start() throws CoreException {
    retrieveConnection(JettyServletRegistrar.class).addServlet(asServletWrapper());
  }

  /**
   * 
   * @see com.adaptris.core.AdaptrisComponent#stop()
   */
  public void stop() {
    String loggingUrl = "";
    try {
      loggingUrl = asServletWrapper().getUrl();
      retrieveConnection(JettyServletRegistrar.class).removeServlet(asServletWrapper());
    }
    catch (CoreException e) {
      log.warn("Could not remove servlet from jetty engine; Path=[{}]", loggingUrl, e);
    }
  }

  /**
   * 
   * @see com.adaptris.core.AdaptrisComponent#close()
   */
  public void close() {
  }

  /**
   * @return the maxWaitTime
   */
  public TimeInterval getMaxWaitTime() {
    return maxWaitTime;
  }

  /**
   * Set the max wait time for an individual worker in a workflow to finish.
   * <p>
   * This setting only has an impact if the consumer is the entry point for a {@link com.adaptris.core.PoolingWorkflow} instance. In
   * the event that the wait time is exceeded, then the internal {@link javax.servlet.http.HttpServlet} instance commits the
   * response in its current state and returns control back to the Jetty engine.
   * </p>
   * <p>
   * </p>
   * 
   * @param maxWait the maxWaitTime to set (default 10 minutes)
   */
  public void setMaxWaitTime(TimeInterval maxWait) {
    maxWaitTime = maxWait;
  }

  long maxWaitTime() {
    return getMaxWaitTime() != null ? getMaxWaitTime().toMilliseconds() : DEFAULT_MAX_WAIT_TIME.toMilliseconds();
  }


  private static List<String> asList(String commaSepList) {
    List<String> result = new ArrayList<String>();
    if (commaSepList != null) {
      StringTokenizer st = new StringTokenizer(commaSepList, COMMA);
      while (st.hasMoreTokens()) {
        result.add(st.nextToken().trim().toUpperCase());
      }
    }
    return result;
  }
  
  
  /**
   * Basic implementation.
   * 
   * @author lchan
   * 
   */
  private class BasicServlet extends HttpServlet {

    private static final long serialVersionUID = 2007082301L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
      process(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
      process(request, response);
    }

    private void process(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
      String oldName = renameThread();
      try {
        if (isMethodAcceptable(request.getMethod())) {
          processRequest(request, response);
        }
        else {
          write405(response);
        }
      }
      finally {
        Thread.currentThread().setName(oldName);
      }
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
      AdaptrisMessage msg = createMessage(request, response);
      msg.addMetadata(JETTY_URL, request.getRequestURL().toString());
      msg.addMetadata(JETTY_URI, request.getRequestURI());
      msg.addMetadata(HTTP_METHOD, request.getMethod());
      if (!isEmpty(request.getQueryString())) {
        msg.addMetadata(JETTY_QUERY_STRING, request.getQueryString());
      }
      msg.addObjectHeader(CoreConstants.JETTY_RESPONSE_KEY, response);
      msg.addObjectHeader(CoreConstants.JETTY_REQUEST_KEY, request);
      JettyConsumerMonitor monitor = new JettyConsumerMonitor();
      monitor.setStartTime(new Date().getTime());
      msg.addObjectHeader(JettyPoolingWorkflowInterceptor.MESSAGE_MONITOR, monitor);
      boolean waitFor = submitToWorkflow(msg);
      if (waitFor) {
        try {
          synchronized (monitor) {
            while ((!monitor.isMessageComplete()) && ((new Date().getTime() - monitor.getStartTime()) < maxWaitTime()))
              monitor.wait(DEFAULT_INTERMEDIATE_WAIT_TIME.toMilliseconds());
          }
        }
        catch (InterruptedException e) {
        }
        if ((monitor.getEndTime() - monitor.getStartTime()) > getWarnAfterMessageHangMillis())
          log.warn("Message (" + msg.getUniqueId() + ") took longer than expected; "
              + ((monitor.getEndTime() - monitor.getStartTime())) + " ms");
      }
    }

    private boolean isMethodAcceptable(String method) {
      return acceptedMethods.contains(method.toUpperCase());
    }

    private void write405(HttpServletResponse response) throws IOException, ServletException {
      response.addHeader("Allow", StringUtils.join(acceptedMethods, COMMA));
      response.sendError(HttpURLConnection.HTTP_BAD_METHOD, "Method Not Allowed");
    }
  }

  public Boolean getAdditionalDebug() {
    return additionalDebug;
  }

  public void setAdditionalDebug(Boolean additionalDebug) {
    this.additionalDebug = additionalDebug;
  }

  boolean additionalDebug() {
    return getAdditionalDebug() != null ? getAdditionalDebug().booleanValue() : false;
  }

  public long getWarnAfterMessageHangMillis() {
    return warnAfterMessageHangMillis;
  }

  public void setWarnAfterMessageHangMillis(long warnAfterMessageHangMillis) {
    this.warnAfterMessageHangMillis = warnAfterMessageHangMillis;
  }

}
