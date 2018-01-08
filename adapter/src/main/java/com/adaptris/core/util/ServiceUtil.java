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

import com.adaptris.core.Service;

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
}
