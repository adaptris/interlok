package com.adaptris.core.runtime;

import java.util.ArrayList;
import java.util.List;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.BaseCase;
import com.adaptris.core.Workflow;
import com.adaptris.core.util.LifecycleHelper;

public class NullMessageErrorDigesterTest extends BaseCase {

  public NullMessageErrorDigesterTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
  }

  @Override
  protected void tearDown() throws Exception {
  }

  public void testLifecycle() throws Exception {
    MessageErrorDigester digester = createDigester();
    LifecycleHelper.init(digester);
    LifecycleHelper.start(digester);
    LifecycleHelper.stop(digester);
    LifecycleHelper.close(digester);
  }


  public void testDigest() throws Exception {
    MessageErrorDigester digester = createDigester();
    try {
      start(digester);
      List<AdaptrisMessage> msgs = createMessages(5, 1);
      for (AdaptrisMessage msg : msgs) {
        digester.digest(msg);
      }
      assertEquals(0, digester.getTotalErrorCount());
    }
    finally {
      stop(digester);
    }
  }

  private MessageErrorDigester createDigester() {
    return new NullMessageErrorDigester();
  }

  private List<AdaptrisMessage> createMessages(int size, int start) {
    List<AdaptrisMessage> errors = new ArrayList<AdaptrisMessage>();
    for (int i = 0; i < size; i++) {
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      msg.addMetadata(Workflow.WORKFLOW_ID_KEY, "workflow" + (i + start));
      errors.add(msg);
    }
    return errors;
  }

}