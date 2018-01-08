/*
 * Copyright 2018 Adaptris Ltd.
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
package com.adaptris.core.util;

import java.util.ArrayList;
import java.util.List;

import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.AllowsRetriesConnection;
import com.adaptris.core.ConnectedService;
import com.adaptris.core.CoreException;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceCollection;
import com.adaptris.core.ServiceWrapper;

public abstract class ServiceUtil {

  public static Service[] discardNulls(Service... services) {
    List<Service> list = new ArrayList<>();
    for (Service s : services) {
      if (s != null) {
        list.add(s);
      }
    }
    return list.toArray(new Service[0]);
  }

  public static Service rewriteConnectionsForTesting(Service service) throws CoreException {
    Service rewritten = service;
    if (rewritten instanceof ConnectedService) {
      ConnectedService connectedService = (ConnectedService) rewritten;
      connectedService.setConnection(cloneForTesting(connectedService.getConnection()));
    }
    if (rewritten instanceof ServiceCollection) {
      rewriteConnectionsForTesting((ServiceCollection) rewritten);
    }
    if (rewritten instanceof ServiceWrapper) {
      rewriteConnectionsForTesting((ServiceWrapper) rewritten);
    }
    return rewritten;
  }

  private static AdaptrisConnection cloneForTesting(AdaptrisConnection conn) throws CoreException {
    if (conn == null) {
      return null;
    }
    AdaptrisConnection cloned = conn.cloneForTesting();
    if (cloned instanceof AllowsRetriesConnection) {
      AllowsRetriesConnection retry = (AllowsRetriesConnection) cloned;
      retry.setConnectionAttempts(0);
    }
    return cloned;
  }

  private static ServiceCollection rewriteConnectionsForTesting(ServiceCollection service) throws CoreException {
    for (Service s : service.getServices()) {
      rewriteConnectionsForTesting(s);
    }
    return service;
  }

  private static ServiceWrapper rewriteConnectionsForTesting(ServiceWrapper service) throws CoreException {
    for (Service s : service.wrappedServices()) {
      rewriteConnectionsForTesting(s);
    }
    return service;
  }
}
