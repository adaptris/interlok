/*
 * Copyright 2017 Adaptris Ltd.
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
package com.adaptris.core.runtime;

import java.io.IOException;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.adaptris.core.CoreException;
import com.adaptris.util.URLString;

public interface AdapterBuilderMBean extends BaseComponentMBean {

  ObjectName createAdapter() throws IOException, MalformedObjectNameException, CoreException;

  ObjectName createAdapter(URLString url) throws IOException, MalformedObjectNameException, CoreException;

  ObjectName createAdapterFromUrl(String url) throws IOException, MalformedObjectNameException, CoreException;

  ObjectName createAdapter(String xml) throws IOException, MalformedObjectNameException, CoreException;

  void updateVCS() throws CoreException;
}
