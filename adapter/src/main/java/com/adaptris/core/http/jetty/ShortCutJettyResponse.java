package com.adaptris.core.http.jetty;

import static com.adaptris.core.http.jetty.JettyPoolingWorkflowInterceptor.MESSAGE_MONITOR;

import java.util.Date;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.PoolingWorkflow;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Allows you to short cut {@link JettyPoolingWorkflowInterceptor} behaviour in a {@link PoolingWorkflow}.
 * <p>
 * Normally, the response is not committed until the workflow is complete. If you have a workflow bound to HTTP that takes a
 * significant amount of time then this can cause arbitrary timeouts within firewalls and proxies. If the client sends a
 * {@code Expect: 102-Processing} header, then the adapter will automatically send a 102 response every 20 seconds or so until the
 * workflow has finished. This service allows you to shortcut that behaviour and commit a response before the underlying workflow is
 * complete. You will still need to use {@link StandardResponseProducer} prior to using this service.
 * </p>
 * <p>
 * While not strictly enforced, you should consider returning a {@code 202 ACCEPTED} rather than a {@code 200 OK} if you are going
 * to send the response before the end of the workflow. This would be the more "correct" behaviour as per the HTTP specifications.
 * </p>
 * 
 * @config jetty-commit-response
 * @author lchan
 *
 */
@XStreamAlias("jetty-commit-response")
@AdapterComponent
@ComponentProfile(summary = "Allows you to commit the HTTP response immediately rather than at the end of a PoolingWorkflow", tag = "service,http,https,jetty")
public class ShortCutJettyResponse extends ServiceImp {

  public ShortCutJettyResponse() {

  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    if (msg.getObjectHeaders().containsKey(MESSAGE_MONITOR)) {
      JettyConsumerMonitor o = (JettyConsumerMonitor) msg.getObjectHeaders().get(MESSAGE_MONITOR);
      o.setMessageComplete(true);
      o.setEndTime(new Date().getTime());
      synchronized (o) {
        o.notifyAll();
      }
    }
  }

  @Override
  public void prepare() throws CoreException {
  }

  @Override
  protected void initService() throws CoreException {
  }

  @Override
  protected void closeService() {
  }

}
