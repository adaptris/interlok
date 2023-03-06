/*
* Copyright 2019 Adaptris Ltd.
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

import com.adaptris.annotation.ComponentProfile;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
*
* @config jdbc-no-parameter-logging
*
*/
@JacksonXmlRootElement(localName = "jdbc-no-parameter-logging")
@XStreamAlias("jdbc-no-parameter-logging")
@ComponentProfile(summary="Never log parameters for JDBC Statement", since="3.8.4")
public class NoParameterLogging implements ParameterLogger {

@Override
public void log(int paramterIndex, Object o) {
// Intentionally empty
}

}
