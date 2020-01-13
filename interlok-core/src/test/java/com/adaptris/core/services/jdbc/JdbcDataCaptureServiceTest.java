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

package com.adaptris.core.services.jdbc;
import org.junit.Test;

public class JdbcDataCaptureServiceTest extends JdbcDataCaptureServiceCase {

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }


  @Override
  protected JdbcDataCaptureService newService() {
    return new JdbcDataCaptureService();
  }

  @Test
  public void testBackReferences() throws Exception {
    this.testBackReferences(new JdbcDataCaptureService("INSERT INTO MYTABLE ('ABC');"));
  }
}
