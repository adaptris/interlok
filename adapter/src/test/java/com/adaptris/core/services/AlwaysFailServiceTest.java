package com.adaptris.core.services;

import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.exception.ExceptionServiceExample;

@SuppressWarnings("deprecation")
public class AlwaysFailServiceTest extends ExceptionServiceExample {

  public AlwaysFailServiceTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  protected void setUp() throws Exception {
  }

  public void testService() throws Exception {
    try {
      execute(new AlwaysFailService(), AdaptrisMessageFactory.getDefaultInstance().newMessage());
      fail("AlwaysFail didn't fail!");
    } catch (ServiceException e) {
      // expected
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new AlwaysFailService();
  }

}
