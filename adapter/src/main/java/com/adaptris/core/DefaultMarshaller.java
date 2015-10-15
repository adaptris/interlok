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

package com.adaptris.core;


/**
 * 
 * Convenience for getting the default marshalling system currently available in the adapter.
 * 
 * Added a web-service marshaller because the auto-generated web service services currently
 * fail when using the PrettyStaxDriver
 * 
 * @author gcsiki
 */
public class DefaultMarshaller {

	private static AdaptrisMarshaller marshaller;

	public static AdaptrisMarshaller getDefaultMarshaller() throws CoreException {
		if (marshaller == null) {
			// The default marshaller is xstream marshaller for now
			marshaller = new XStreamMarshaller();
		}
		return marshaller;
	}

	public static void setDefaultMarshaller(AdaptrisMarshaller _marshaller) {
		marshaller = _marshaller;
	}

}
