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

package com.adaptris.core.stubs;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ProduceException;
import com.adaptris.core.ProduceOnlyProducerImp;

/**
 * Producer purely used for marshalling example-xml.
 */
public class DummyMessageProducer extends ProduceOnlyProducerImp {

  public DummyMessageProducer() {
  }

  @Override
  public void prepare() throws CoreException {}

  @Override
  public String getUniqueId() {
    return null;
  }

  @Override
  protected void doProduce(AdaptrisMessage msg, String endpoint) throws ProduceException {}

  @Override
  public String endpoint(AdaptrisMessage msg) throws ProduceException {
    return null;
  }
}
