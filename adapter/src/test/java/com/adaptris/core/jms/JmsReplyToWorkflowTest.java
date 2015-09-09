package com.adaptris.core.jms;

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
    c.setConsumeConnection(new JmsConnection());
    c.setProduceConnection(new JmsConnection());
    JmsReplyToWorkflow workflow = new JmsReplyToWorkflow();
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

  // @Override
  // public void testLicenseCombinations() throws Exception {
  // super.testLicenseCombinations();
  // assertEquals(false, createWorkflowLicenseCombo(true, true,
  // true).isEnabled(new LicenseStub() {
  // @Override
  // public boolean isEnabled(int arg0) {
  // if (arg0 == License.JMS) {
  // return false;
  // }
  // return super.isEnabled(arg0);
  // }
  // }));
  // }

}
