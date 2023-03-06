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

package com.adaptris.core.event;

import com.adaptris.core.AdapterLifecycleEvent;
import com.adaptris.core.EventNameSpaceConstants;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;


/**
* <p>
* <code>AdapterLifecycleEvent</code> indicating that <code>init</code> has been invoked.
* </p>
*
* @config adapter-init-event
*/
@JacksonXmlRootElement(localName = "adapter-init-event")
@XStreamAlias("adapter-init-event")
public class AdapterInitEvent extends AdapterLifecycleEvent {
private static final long serialVersionUID = 2014012301L;

/**
* <p>
* Creates a new instance.
* </p>
*/
public AdapterInitEvent() {
super(EventNameSpaceConstants.ADAPTER_INIT);
}
}
