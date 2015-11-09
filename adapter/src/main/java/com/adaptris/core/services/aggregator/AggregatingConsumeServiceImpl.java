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

import com.adaptris.core.AdaptrisComponent;
import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.LifecycleHelper;


/**
 * Base class for {@link AggregatingConsumeService} implementations providing common functionality.
 * 
 * @author lchan
 * 
 */
public abstract class AggregatingConsumeServiceImpl<E extends AdaptrisConnection> extends ServiceImp implements
    AggregatingConsumeService<E> {

  public AggregatingConsumeServiceImpl() {
  }

  protected void start(AdaptrisComponent ac) throws ServiceException {
    try {
      LifecycleHelper.init(ac);
      LifecycleHelper.start(ac);
    }
    catch (CoreException e) {
      throw new ServiceException(e);
    }
  }

  protected void stop(AdaptrisComponent ac) {
    LifecycleHelper.stop(ac);
    LifecycleHelper.close(ac);
  }

  @Override
  protected void initService() throws CoreException {
  }

  @Override
  public void start() throws CoreException {
    super.start();
  }

  @Override
  public void stop() {
    super.stop();
  }

  @Override
  protected void closeService() {
  }
}
