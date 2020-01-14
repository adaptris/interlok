/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.adaptris.core.services.path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import java.util.List;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.ServiceCase;
import com.adaptris.core.ServiceException;
import com.adaptris.core.common.ConstantDataInputParameter;
import com.adaptris.core.common.Execution;
import com.adaptris.core.common.MetadataDataInputParameter;
import com.adaptris.core.common.MetadataDataOutputParameter;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.config.DataInputParameter;
import com.adaptris.interlok.types.InterlokMessage;

@SuppressWarnings("deprecation")
public class RegexpServiceTest extends ServiceCase {
  private static final String TARGET_METADATA_KEY = "targetMetadataKey";
  private static final String SOURCE_METADATA_KEY = "sourceKey";

  public static final String PAYLOAD = "Address: Adaptris Limited, 120 Bath Road, " + "Heathrow, Middlesex, UB3 5AN";
  public static final String POSTCODE_REGEXP = ".*[ ]([A-Z]{1,2}[0-9R][0-9A-Z]? [0-9][A-Z-[CIKMOV]]{2}).*";
  public static final String ALTERNATE_PAYLOAD = "The quick brown fox jumps over the lazy dog";
  public static final String ALTERNATE_REGEX = "^The (.*) brown fox.*";

  /**
   * Key in unit-test.properties that defines where example goes unless overriden {@link #setBaseDir(String)}.
   * 
   */
  public static final String BASE_DIR_KEY = "RegexpServiceExamples.baseDir";

  public RegexpServiceTest() {
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }
  private RegexpService createService() {
    RegexpService s = new RegexpService();
    s.getExecutions()
        .add(new Execution(new ConstantDataInputParameter(POSTCODE_REGEXP), new MetadataDataOutputParameter(TARGET_METADATA_KEY)));
    return s;
  }

  @Test
  public void testService() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD);
    execute(createService(), msg);
    assertEquals("UB3 5AN", msg.getMetadataValue(TARGET_METADATA_KEY));
  }

  @Test
  public void testService_Exception() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(ALTERNATE_PAYLOAD);
    RegexpService s = new RegexpService();
    s.getExecutions().add(new Execution(new DodgyInputParameter(), new MetadataDataOutputParameter(TARGET_METADATA_KEY)));
    try {
      execute(s, msg);
      fail();
    } catch (ServiceException expected) {

    }
  }

  @Test
  public void testService_NoMatch() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(ALTERNATE_PAYLOAD);
    execute(createService(), msg);
    assertFalse(msg.containsKey(TARGET_METADATA_KEY));
  }

  @Test
  public void testService_CachedPattern() throws Exception {
    RegexpService s = createService();
    start(s);
    AdaptrisMessage msg1 = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD);
    s.doService(msg1);

    AdaptrisMessage msg2 = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD);
    s.doService(msg2);

    stop(s);
    assertEquals("UB3 5AN", msg1.getMetadataValue(TARGET_METADATA_KEY));
    assertEquals("UB3 5AN", msg2.getMetadataValue(TARGET_METADATA_KEY));
  }

  @Test
  public void testService_CachedPattern_Mismatch() throws Exception {
    RegexpService s = new RegexpService();
    List<Execution> executions = s.getExecutions();
    executions.add(
        new Execution(new MetadataDataInputParameter(SOURCE_METADATA_KEY), new MetadataDataOutputParameter(TARGET_METADATA_KEY)));

    start(s);
    AdaptrisMessage msg1 = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD);
    msg1.addMetadata(new MetadataElement(SOURCE_METADATA_KEY, POSTCODE_REGEXP));
    s.doService(msg1);

    AdaptrisMessage msg2 = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD);
    msg2.addMetadata(new MetadataElement(SOURCE_METADATA_KEY, ALTERNATE_REGEX));
    s.doService(msg2);

    stop(s);
    assertEquals("UB3 5AN", msg1.getMetadataValue(TARGET_METADATA_KEY));
    assertFalse(msg2.containsKey(TARGET_METADATA_KEY));
  }

  @Override
  protected RegexpService retrieveObjectForSampleConfig() {
    return createService();
  }


  private class DodgyInputParameter implements DataInputParameter<String> {

    @Override
    public String extract(InterlokMessage arg0) throws InterlokException {
      throw new InterlokException(this.getClass().getSimpleName());
    }

  }

}
