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

package com.adaptris.mail;

import java.util.List;

import javax.mail.internet.MimeMessage;

/**
 * Interface for Adaptris mail receiver clients
 * 
 * 
 *
 */
public interface MailReceiver {

	/**
	 * Connect to the mailbox
	 * <p>
	 * This will also:-
	 * 		Retrieve the messages from the server
	 * 		Filter out messages that do not fit the supplied patterns
	 * </p>
	 * 
	 * @see #setFromFilter(String)
	 * @see #addCustomFilter(String, String)
	 * @see #setSubjectFilter(String)
	 * @throws MailException if the connection failed.
	 */
	public void connect() throws MailException;
	
	/**
	 * Disconnect from the mail server.
	 */
	public void disconnect();
	

	/**
	 * Set the subject filter.
	 * <p>
	 * This filters the subject that is present in the message
	 * 
	 * @param filter the filter.
	 */
	public void setSubjectFilter(String filter);

	/**
	 * Set the sender filter.
	 * <p>
	 * This filters the sender that is present in the message
	 * 
	 * @param filter the filter.
	 */
	public void setFromFilter(String filter);
	
	/**
	 * Set the sender filter.
	 * <p>
	 * This filters the sender that is present in the message
	 * 
	 * @param filter the filter.
	 */
	public void setRecipientFilter(String filter);

	/**
	 * Add a custom filter
	 * <p>
	 * This filters any specific user header that is present in the message
	 * 
	 * @param filter the filter.
	 * @param headerValue the header value
	 */
	public void addCustomFilter(String headerValue, String filter);

	/**
	 * Set the handler for regular expressions.
	 * 
	 * @param type one of "GLOB", "AWK", "PERL5" or their respective compiler
	 *          classnames
	 */
	public void setRegularExpressionCompiler(String type);
	
	/**
	 * Specify whether to delete messages on disconnect / logout.
	 * <p>
	 * By default all messages that are retrived have the
	 * <b>Flags.Flag.DELETED</b> flag set, which means that if this is set to true
	 * then the messages will be deleted when disconnect is called
	 * </p>
	 * 
	 * @param delFlag true or false
	 */
	public void purge(boolean delFlag);

	/**
	 * Return the list of messages currently in this object
	 * 
	 * @return list of MimeMessages
	 */
	public List<MimeMessage> getMessages();

	/**
	 * Mark the message as read and deleted, if purge 'on'.
	 * 
	 * @param msg	message to be set
	 * @throws MailException on error
	 */
  public void setMessageRead(MimeMessage msg) throws MailException;
	
	/**
	 * Reset the state of the message so that it is no longer marked as seen or
	 * deleted.
	 * 
	 * @param msg the Message to reset.
	 * @throws Exception on error.
	 */
  public void resetMessage(MimeMessage msg) throws Exception;
	
}
