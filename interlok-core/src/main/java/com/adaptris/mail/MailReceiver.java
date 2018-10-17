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

import java.io.Closeable;
import java.util.Iterator;
import java.util.List;

import javax.mail.internet.MimeMessage;

/**
 * Interface for Adaptris mail receiver clients
 * 
 * 
 *
 */
public interface MailReceiver extends Iterable<MimeMessage>, Closeable {

	/**
   * Connect to the mailbox
   * 
   * @throws MailException if the connection failed.
   */
  public void connect() throws MailException;
	
	/**
   * Disconnect from the mail server.
   * 
   * @deprecated since 3.6.5 use {@link Closeable#close()} instead.
   */
  @Deprecated
	public void disconnect();
	

	/**
   * Set the subject filter.
   * 
   * @param filter the filter.
   */
	public void setSubjectFilter(String filter);

	/**
   * Set the sender filter.
   * 
   * @param filter the filter.
   */
	public void setFromFilter(String filter);
	
	/**
	 * Set the sender filter.
	 * 
	 * @param filter the filter.
	 */
	public void setRecipientFilter(String filter);

	/**
	 * Add a custom filter
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
   * Return a filtered list of messages in the mailbox
   * 
   * @return list of MimeMessages
   * @deprecated since 3.6.5 use {@link #iterator()} instead.
   */
  @Deprecated
  public List<MimeMessage> getMessages();

  /**
   * Iterate over the list of messages in the mailbox.
   * 
   * @return an iterator of filtered messages.
   * @throws RuntimeException if we couldn't interact with the mailbox.
   */
  public Iterator<MimeMessage> iterator();

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
