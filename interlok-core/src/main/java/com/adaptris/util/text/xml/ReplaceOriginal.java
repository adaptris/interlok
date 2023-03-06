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

package com.adaptris.util.text.xml;

import org.w3c.dom.Document;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
* Merge implementation that simply replaces the original.
*
* @config xml-replace-original
*
* @author lchan
*
*/
@JacksonXmlRootElement(localName = "xml-replace-original")
@XStreamAlias("xml-replace-original")
public class ReplaceOriginal extends MergeImpl {
public ReplaceOriginal() {

}

@Override
public Document merge(Document original, Document newDoc) throws Exception {
return newDoc;
}
}
