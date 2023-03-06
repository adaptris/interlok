/*
Copyright Adaptris

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.adaptris.core.services.conditional.operator;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.services.conditional.Condition;
import com.adaptris.core.services.conditional.Operator;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
* <p>
* This {@link Operator} simply tests a single value is not null.
* </p>
* <p>
* The value used in the not-null test is the {@link Condition} that this {@link Operator} is
* configured for; which could be the message payload or a metadata item for example. <br/>
* </p>
*
* @config not-null
* @author amcgrath
*
*/
@JacksonXmlRootElement(localName = "not-null")
@XStreamAlias("not-null")
@AdapterComponent
@ComponentProfile(summary = "Tests that a value exists (is not null).",
tag = "conditional,operator")
public class NotNull implements Operator {

@Override
public boolean apply(AdaptrisMessage message, String object) {
return object != null;
}

public String toString() {
return "is not null";
}
}
