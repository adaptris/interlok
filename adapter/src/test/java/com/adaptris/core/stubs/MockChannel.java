package com.adaptris.core.stubs;

import com.adaptris.core.Channel;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultEventHandler;
import com.adaptris.core.EventHandler;
import com.adaptris.core.NullProcessingExceptionHandler;
import com.adaptris.core.Workflow;
import com.adaptris.util.PlainIdGenerator;
import com.adaptris.util.license.License;

public class MockChannel extends Channel {

  private EventHandler eh;
  private int startCount = 0, initCount = 0, stopCount = 0, closeCount = 0;
  private License myLicense = new LicenseStub();

  public MockChannel() throws Exception {
    super();
    setUniqueId("channel_" + new PlainIdGenerator().create(this));
    setMessageErrorHandler(new NullProcessingExceptionHandler());
  }

  @Override
  public void prepare() throws CoreException {
    if (eh == null) {
      eh = new DefaultEventHandler();
    }
    eh.requestStart();
    registerEventHandler(eh);
    super.prepare();
    registerActiveMsgErrorHandler(getMessageErrorHandler());
    for (Workflow workflow : getWorkflowList()) {
      if (workflow.getMessageErrorHandler() != null) {
        workflow.registerActiveMsgErrorHandler(workflow.getMessageErrorHandler());
      }
      else {
        workflow.registerActiveMsgErrorHandler(getMessageErrorHandler());
      }
    }
    super.isEnabled(myLicense);
  }

  public EventHandler obtainEventHandler() {
    return eh;
  }

  public void setEventHandler(EventHandler evtHandler) {
    this.eh = evtHandler;
  }

  @Override
  public void init() throws CoreException {
    prepare();
    super.init();
    initCount++;
  }

  @Override
  public void start() throws CoreException {
    super.start();
    startCount++;
  }

  @Override
  public void stop() {
    super.stop();
    stopCount++;
  }

  @Override
  public void close() {
    super.close();
    closeCount++;
  }

  public int getStartCount() {
    return startCount;
  }

  public int getInitCount() {
    return initCount;
  }

  public int getStopCount() {
    return stopCount;
  }

  public int getCloseCount() {
    return closeCount;
  }
}
