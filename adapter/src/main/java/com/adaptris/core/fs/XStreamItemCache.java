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

package com.adaptris.core.fs;

import com.adaptris.core.AdaptrisMarshaller;
import com.adaptris.core.XStreamMarshaller;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @config fs-xstream-processed-item-cache
 * @license BASIC
 * 
 * @author dsefton
 * @author $Author: dsefton $
 */
@XStreamAlias("fs-xstream-processed-item-cache")
public class XStreamItemCache extends MarshallingItemCache {
	
	/**
	 * Default Constructor
	 *
	 * @throws Exception
	 */
	public XStreamItemCache() throws Exception {
		super();
	}
	
	public XStreamItemCache(String store) throws Exception {
		super(store);
	}
	
	@Override
	protected AdaptrisMarshaller initMarshaller() throws Exception{
		return new XStreamMarshaller();
	}

}
