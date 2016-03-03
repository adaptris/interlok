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

package com.adaptris.core.services;

import java.io.Reader;
import java.io.StringReader;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.MarshallingCDATA;
import com.adaptris.core.BranchingServiceCollection;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Supports arbitary scripting languges that are supported by JSR223.
 * <p>
 * You should take care when configuring this class; it can present an audit trail issue when used in combination with
 * {@link com.adaptris.core.services.dynamic.DynamicServiceLocator} or
 * {@link com.adaptris.core.services.dynamic.DynamicServiceExecutor} if your script executes arbitrary system commands. In that
 * situation, all commands will be executed with the current users permissions may be subject to the virtual machines security
 * manager.
 * </p>
 * <p>
 * The script is executed and the AdaptrisMessage that is due to be processed is bound against the key "message". This can be used
 * as a standard variable within the script
 * </p>
 * <p>
 * Note that this class can be used as the selector as part of a {@link BranchingServiceCollection}. If used as such, then you need
 * to remember to invoke {@link com.adaptris.core.AdaptrisMessage#setNextServiceId(String)} as part of the script and {@link #setBranching(Boolean)}
 * should be true.
 * <p>
 * 
 * @config embedded-scripting-service
 * 
 * @author lchan
 * 
 */
@XStreamAlias("embedded-scripting-service")
@AdapterComponent
@ComponentProfile(summary = "Execute an embedded JSR223 script", tag = "service,scripting")
@DisplayOrder(order = {"language", "script", "branching"})
public class EmbeddedScriptingService extends ScriptingServiceImp {

	@MarshallingCDATA
	private String script;
	
	public EmbeddedScriptingService() {
		super();
	}

	public EmbeddedScriptingService(String uniqueId) {
		this();
		setUniqueId(uniqueId);
	}


	public String getScript() {
		return script;
	}

	/**
	 * Set the contents of the script.
	 *
	 * @param s the script
	 */
	public void setScript(String s) {
		script = s;
	}

	@Override
	protected Reader createReader() {
		return new StringReader(getScript() == null ? "" : getScript());
	}

}
