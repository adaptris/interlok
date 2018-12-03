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

package com.adaptris.core.services.aggregator;

import java.util.List;

import com.adaptris.core.Service;

public abstract class AggregatorCase extends AggregatingServiceExample {

  private static final String XPATH_ENVELOPE = "/envelope";

  public AggregatorCase(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
  }

  @Override
  protected void tearDown() throws Exception {
  }

  public void testSetOverwriteMetadata() throws Exception {
    MessageAggregatorImpl impl = createAggregatorForTests();
    assertNull(impl.getOverwriteMetadata());
    assertEquals(false, impl.overwriteMetadata());
    impl.setOverwriteMetadata(Boolean.TRUE);
    assertEquals(Boolean.TRUE, impl.getOverwriteMetadata());
    assertEquals(true, impl.overwriteMetadata());
    impl.setOverwriteMetadata(null);
    assertNull(impl.getOverwriteMetadata());
    assertEquals(false, impl.overwriteMetadata());
  }

  protected abstract MessageAggregatorImpl createAggregatorForTests();

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return null;
  }

  protected abstract List<Service> retrieveObjectsForSampleConfig();
}
