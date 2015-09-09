package com.adaptris.core;

import java.util.ArrayList;
import java.util.Collection;

import com.adaptris.util.license.License;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This is a dummy for marshalling purposes.
 * 
 * @config no-retries
 * 
 * @author lchan
 * 
 */
@XStreamAlias("no-retries")
public class NoRetries implements FailedMessageRetrier {

  public void addWorkflow(Workflow workflow) {
    ;
  }

  public void close() {
  }

  public void init() throws CoreException {
  }

  public void start() throws CoreException {
  }

  public void stop() {
  }

  public boolean isEnabled(License license) {
    return true;
  }

  public void onAdaptrisMessage(AdaptrisMessage msg) {
  }

  public void clearWorkflows() {
  }

  public Collection<String> registeredWorkflowIds() {
    return new ArrayList<String>();
  }
}
