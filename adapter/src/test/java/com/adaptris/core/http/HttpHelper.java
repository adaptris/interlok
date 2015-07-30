/*
 * $RCSfile: HttpHelper.java,v $
 * $Revision: 1.2 $
 * $Date: 2009/04/17 10:56:09 $
 * $Author: lchan $
 */
package com.adaptris.core.http;

import java.net.HttpURLConnection;

import com.adaptris.core.Adapter;
import com.adaptris.core.AdaptrisMessageProducer;
import com.adaptris.core.Channel;
import com.adaptris.core.ChannelList;
import com.adaptris.core.ServiceList;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.core.Workflow;
import com.adaptris.core.WorkflowList;
import com.adaptris.core.stubs.LicenseStub;
import com.adaptris.http.legacy.ConsumeConnection;
import com.adaptris.http.legacy.GenericConsumer;
import com.adaptris.http.legacy.HttpResponseProducer;
import com.adaptris.util.GuidGenerator;

/**
 * @author lchan
 * 
 */
public class HttpHelper {

  static Adapter createAdapter(ConsumeConnection connection,
                                      GenericConsumer consumer,
                                      AdaptrisMessageProducer producer)
      throws Exception {
    return createAdapter(connection, createWorkflow(consumer, producer));
  }

  static Adapter createAdapter(ConsumeConnection connection, Workflow w)
      throws Exception {
    Adapter a = new Adapter();
    a.registerLicense(new LicenseStub());
    a.setUniqueId(new GuidGenerator().getUUID());
    ChannelList cl = new ChannelList();
    Channel channel = new Channel();
    channel.setConsumeConnection(connection);
    WorkflowList wfl = new WorkflowList();
    wfl.getWorkflows().add(w);
    channel.setWorkflowList(wfl);
    cl.addChannel(channel);
    a.setChannelList(cl);
    return a;
  }

  static Workflow createWorkflow(GenericConsumer consumer,
                                 AdaptrisMessageProducer producer)
      throws Exception {
    StandardWorkflow wf = new StandardWorkflow();
    wf.setConsumer(consumer);
    wf.setProducer(producer);
    ServiceList sl = new ServiceList();
    StandaloneProducer p = new StandaloneProducer();
    HttpResponseProducer rp = new HttpResponseProducer();
    rp.setHttpResponseText("OK");
    rp.setHttpResponseCode(HttpURLConnection.HTTP_OK);
    p.setProducer(rp);
    sl.addService(p);
    wf.setServiceCollection(sl);
    return wf;
  }

}
