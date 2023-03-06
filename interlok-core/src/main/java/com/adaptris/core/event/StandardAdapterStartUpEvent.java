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

import com.adaptris.core.Adapter;
import com.adaptris.core.AdapterStartUpEvent;
import com.adaptris.core.CoreException;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
* <p>
* Event containing <code>Adapter</code> start-up information..
* </p>
*
* @config standard-adapter-start-up-event
*
* @see AdapterStartUpEvent
*/
@JacksonXmlRootElement(localName = "standard-adapter-start-up-event")
@XStreamAlias("standard-adapter-start-up-event")
public class StandardAdapterStartUpEvent extends AdapterStartUpEvent {
private static final long serialVersionUID = 2014012301L;
// Dummy for backwards compatibility with v2 events.
// Do not remove until all V2 clients are removed.
private String compressedAdapterXml;
// Dummy for backwards compatibility with v2 events.
// Do not remove until all V2 clients are removed.
private String adapter;

public StandardAdapterStartUpEvent() throws Exception {
super();
}


/**
*
* @see AdapterStartUpEvent#setAdapter(Adapter)
*/
@Override
public void setAdapter(Adapter param) throws CoreException {
}
}
