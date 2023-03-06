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
package com.adaptris.core.security.access;

import java.util.Collections;
import java.util.Map;

import com.adaptris.core.AdaptrisMessage;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
* A no-op identity builder.
*
* @config empty-identity-builder
*/
@JacksonXmlRootElement(localName = "empty-identity-builder")
@XStreamAlias("empty-identity-builder")
public class EmptyIdentityBuilder extends IdentityBuilderImpl {

@Override
public Map<String, Object> build(AdaptrisMessage msg) {
return Collections.emptyMap();
}

}
