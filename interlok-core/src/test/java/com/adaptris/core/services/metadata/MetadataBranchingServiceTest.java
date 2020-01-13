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

package com.adaptris.core.services.metadata;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.BranchingServiceCollection;
import com.adaptris.core.ServiceCollectionCase;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.BranchingServiceExample;
import com.adaptris.core.services.LogMessageService;
import com.adaptris.core.services.exception.ConfiguredException;
import com.adaptris.core.services.exception.ThrowExceptionService;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairList;

public class MetadataBranchingServiceTest extends BranchingServiceExample {

  private static final String DEFAULT_SERVICE_ID = "default";
  private static final String DUMMY_PAYLOAD = "zzzz";
  private static final String VAL1VAL2 = "val1val2";
  private static final String NEXT_SERVICE_ID = "001";
  private static final String VAL2 = "val2";
  private static final String VAL1 = "val1";
  private static final String KEY2 = "key2";
  private static final String KEY1 = "key1";

  private static enum MetadataValueMatcherCreator {

    IgnoresCaseCreator {
      @Override
      MetadataValueMatcher create() {
        return new IgnoresCaseValueMatcher();
      }

      @Override
      KeyValuePairList createMappings() {
        KeyValuePairList mappings = new KeyValuePairList();
        mappings.addKeyValuePair(new KeyValuePair("MatchValue1", "FirstServiceId"));
        mappings.addKeyValuePair(new KeyValuePair("MatchValue2", "SecondServiceId"));
        return mappings;
      }
    },
    EqualsCreator {
      @Override
      MetadataValueMatcher create() {
        return new EqualsValueMatcher();
      }

      @Override
      KeyValuePairList createMappings() {
        KeyValuePairList mappings = new KeyValuePairList();
        mappings.addKeyValuePair(new KeyValuePair("ExactCaseValue1", "FirstServiceId"));
        mappings.addKeyValuePair(new KeyValuePair("ExactCaseValue2", "SecondServiceId"));
        return mappings;
      }
    },
    UseKeyAsServiceIdCreator {
      @Override
      MetadataValueMatcher create() {
        return new UseKeyAsServiceIdValueMatcher();
      }

      @Override
      KeyValuePairList createMappings() {
        return new KeyValuePairList();
      }
    },
    IntegerValueCreator {
      @Override
      MetadataValueMatcher create() {
        return new IntegerValueMatcher();
      }

      @Override
      KeyValuePairList createMappings() {
        KeyValuePairList result = new KeyValuePairList();
        result.addKeyValuePair(new KeyValuePair("=10", "DefaultServiceId"));
        result.addKeyValuePair(new KeyValuePair("<5", "FirstServiceId"));
        result.addKeyValuePair(new KeyValuePair("<=9", "FirstServiceId"));
        result.addKeyValuePair(new KeyValuePair(">100", "SecondServiceId"));
        result.addKeyValuePair(new KeyValuePair(">=15", "SecondServiceId"));
        return result;
      }
    },
    RegexpValueCreator {
      @Override
      MetadataValueMatcher create() {
        return new RegexpValueMatcher();
      }

      @Override
      KeyValuePairList createMappings() {
        KeyValuePairList result = new KeyValuePairList();
        result.addKeyValuePair(new KeyValuePair("^.*first$", "FirstServiceId"));
        result.addKeyValuePair(new KeyValuePair("^.*1st$", "FirstServiceId"));
        result.addKeyValuePair(new KeyValuePair("^.*second$", "SecondServiceId"));
        result.addKeyValuePair(new KeyValuePair("^.*2nd$", "SecondServiceId"));
        result.addKeyValuePair(new KeyValuePair(".*default$", "DefaultServiceId"));
        return result;
      }
    };
    abstract MetadataValueMatcher create();

    abstract KeyValuePairList createMappings();
  }


  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  private static MetadataValueBranchingService createService() {
    MetadataValueBranchingService service = new MetadataValueBranchingService();
    service.addMetadataKey(KEY1);
    service.addMetadataKey(KEY2);
    KeyValuePairList mappings = new KeyValuePairList();
    mappings.addKeyValuePair(new KeyValuePair(VAL1VAL2, NEXT_SERVICE_ID));
    service.setMetadataToServiceIdMappings(mappings);
    service.setValueMatcher(new EqualsValueMatcher());
    return service;
  }

  @Test
  public void testSetters() throws Exception {
    MetadataValueBranchingService service = createService();
    try {
      service.addMetadataKey("");
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    try {
      service.addMetadataKey(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    try {
      service.setMetadataToServiceIdMappings(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
  }

  private static AdaptrisMessage createMessage(boolean toUpper) {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(DUMMY_PAYLOAD);
    msg.addMetadata(KEY1, toUpper == true ? VAL1.toUpperCase() : VAL1);
    msg.addMetadata(KEY2, toUpper == true ? VAL2.toUpperCase() : VAL2);
    return msg;
  }

  @Test
  public void testInitialise() throws Exception {
    MetadataValueBranchingService service = new MetadataValueBranchingService();
    try {
      LifecycleHelper.initAndStart(service);
      fail();
    } catch (Exception expected) {

    } finally {
      LifecycleHelper.stopAndClose(service);
    }
    try {
      service.setValueMatcher(new UseKeyAsServiceIdValueMatcher());
      LifecycleHelper.initAndStart(service);
    } finally {
      LifecycleHelper.stopAndClose(service);
    }

    service = createService();
    service.setValueMatcher(new UseKeyAsServiceIdValueMatcher());
    try {
      LifecycleHelper.initAndStart(service);
    } finally {
      LifecycleHelper.stopAndClose(service);
    }
  }

  @Test
  public void testDoStandardService() throws Exception {
    MetadataValueBranchingService service = createService();

    AdaptrisMessage msg = createMessage(false);
    execute(service, msg);

    assertTrue(NEXT_SERVICE_ID.equals(msg.getNextServiceId()));
  }

  @Test
  public void testServiceWithEqualsMatcher() throws Exception {
    MetadataValueBranchingService service = createService();
    service.setValueMatcher(new EqualsValueMatcher());
    AdaptrisMessage msg = createMessage(false);
    execute(service, msg);

    assertTrue(NEXT_SERVICE_ID.equals(msg.getNextServiceId()));
  }

  @Test
  public void testDoMissingMetadataService() throws Exception {
    MetadataValueBranchingService service = createService();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(DUMMY_PAYLOAD);
    msg.addMetadata(KEY1, VAL1);
    try {
      execute(service, msg);
      fail("no Exc. adding null Service ID");
    }
    catch (ServiceException e) {
      // expected behaviour
    }
  }

  @Test
  public void testNoMetadataService() throws Exception {
    MetadataValueBranchingService service = createService();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(DUMMY_PAYLOAD);
    try {
      execute(service, msg);
      fail("no Exc. for null Service ID");
    }
    catch (ServiceException e) {
      // expected behaviour
    }
  }

  @Test
  public void testDefaultServiceId() throws Exception {
    MetadataValueBranchingService service = createService();
    service.setDefaultServiceId(DEFAULT_SERVICE_ID);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(DUMMY_PAYLOAD);
    execute(service, msg);
    assertTrue(DEFAULT_SERVICE_ID.equals(msg.getNextServiceId()));
  }

  @Test
  public void testDefaultServiceIdWithValidMetadata() throws Exception {
    MetadataValueBranchingService service = createService();
    service.setDefaultServiceId(DEFAULT_SERVICE_ID);
    AdaptrisMessage msg = createMessage(false);
    execute(service, msg);
    assertTrue(NEXT_SERVICE_ID.equals(msg.getNextServiceId()));
  }

  @Override
  public void testMessageEventGenerator() throws Exception {
    List l = retrieveObjectsForSampleConfig();
    for (Object o : retrieveObjectsForSampleConfig()) {
      ServiceCollectionCase.assertMessageEventGenerator((BranchingServiceCollection) o);
    }
  }

  @Test
  public void testServiceWithIgnoresCaseValueMatcher() throws Exception {
    MetadataValueBranchingService service = createService();
    service.setValueMatcher(new IgnoresCaseValueMatcher());
    AdaptrisMessage msg = createMessage(true);
    execute(service, msg);
    assertTrue(NEXT_SERVICE_ID.equals(msg.getNextServiceId()));
  }

  @Override
  protected List<BranchingServiceCollection> retrieveObjectsForSampleConfig() {
    List result = new ArrayList();
    for (MetadataValueMatcherCreator m : MetadataValueMatcherCreator.values()) {
      result.add(create(m));
    }
    return result;
  }

  protected BranchingServiceCollection create(MetadataValueMatcherCreator m) {
    BranchingServiceCollection sl = new BranchingServiceCollection();
    MetadataValueBranchingService service = new MetadataValueBranchingService();
    service.setValueMatcher(m.create());
    service.setDefaultServiceId("DefaultServiceId");
    service.addMetadataKey("FirstMetadataKeyWhoseValueWeWantToCheck");
    // service.addMetadataKey("Second_Metadata_Key_Whose_Value_Is_Appended_To_FirstMetadataKeyWhoseValueWeWantToCheck");
    service.setMetadataToServiceIdMappings(m.createMappings());
    service.setUniqueId("CheckMetadataValue");
    sl.addService(service);
    sl.setFirstServiceId(service.getUniqueId());
    sl.addService(new LogMessageService("FirstServiceId"));
    sl.addService(new ThrowExceptionService("SecondServiceId", new ConfiguredException("Fail")));
    sl.addService(new LogMessageService("DefaultServiceId"));
    return sl;
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return null;
  }

  @Override
  protected String createBaseFileName(Object object) {
    BranchingServiceCollection bs = (BranchingServiceCollection) object;
    MetadataValueBranchingService s = (MetadataValueBranchingService) bs.getServices().get(0);
    return s.getClass().getCanonicalName() + "-" + s.getValueMatcher().getClass().getSimpleName();
  }
}
