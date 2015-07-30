package com.adaptris.core.services;

import java.io.Reader;
import java.io.StringReader;

import com.adaptris.annotation.MarshallingCDATA;
import com.adaptris.core.AdaptrisMessage;
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
 * to remember to invoke {@link AdaptrisMessage#setNextServiceId(String)} as part of the script and {@link #setBranching(Boolean)}
 * should be true.
 * <p>
 * 
 * @config embedded-scripting-service
 * 
 * @author lchan
 * 
 */
@XStreamAlias("embedded-scripting-service")
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
