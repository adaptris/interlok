package com.adaptris.core.jms;

import java.util.UUID;

import com.adaptris.core.Channel;
import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.ExampleWorkflowCase;

/**
 * <p>
 * Tests for JmsReplyToWorkflow.
 * </p>
 */
@SuppressWarnings("deprecation")
public class JmsReplyToWorkflowTest extends ExampleWorkflowCase {

  /**
   * Constructor for JmsReplyToWorkflowTest.
   *
   * @param arg0
   */
  public JmsReplyToWorkflowTest(String arg0) {
    super(arg0);
  }

  @Override
  protected void setUp() throws Exception {
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    Channel c = new Channel();
    c.setUniqueId(UUID.randomUUID().toString());
    c.setConsumeConnection(JmsTransactedWorkflowTest.configure(new JmsConnection()));
    c.setProduceConnection(JmsTransactedWorkflowTest.configure(new JmsConnection()));
    JmsReplyToWorkflow workflow = new JmsReplyToWorkflow();
    workflow.setUniqueId(UUID.randomUUID().toString());
    workflow.setProducer(new PtpProducer());
    workflow.setConsumer(new PtpConsumer(new ConfiguredConsumeDestination("Sample_Queue1")));
    c.getWorkflowList().add(workflow);
    return c;
  }

  @Override
  protected String createBaseFileName(Object object) {
    return JmsReplyToWorkflow.class.getName();
  }

  @Override
  protected JmsReplyToWorkflow createWorkflowForGenericTests() {
    return new JmsReplyToWorkflow();
  }



}
