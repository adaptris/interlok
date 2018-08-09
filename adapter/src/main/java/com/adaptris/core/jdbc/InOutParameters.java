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

package com.adaptris.core.jdbc;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * This class contains the INOUT parameters that a stored procedure will require to be executed.
 * 
 * @config jdbc-in-out-parameters
 * @author Aaron McGrath
 * 
 */
@XStreamAlias("jdbc-in-out-parameters")
public class InOutParameters extends JdbcParameterList<InOutParameter> {
  @XStreamImplicit
  private List<InOutParameter> parameters;

  public InOutParameters() {
    parameters = new ArrayList<InOutParameter>();
  }
  
  @Override
  public List<InOutParameter> getParameters() {
    return parameters;
  }


}
