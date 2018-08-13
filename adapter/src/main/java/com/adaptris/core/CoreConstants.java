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
 * <p>
 * Constants for the <code>core</code> package.
 * </p>
 */
public abstract class CoreConstants {

  /**
   * <p>
   * Metadata key which specifies the next sequence number for a <code>MleMarker</code>.
   * </p>
   */
  public static final String MLE_SEQUENCE_KEY = "adpnextmlemarkersequence";

  /**
   * <p>
   * Metadata key which <code>Service</code> implementations should set to indicate that any subsequent configured
   * <code>Service</code>s should not be applied to a message and that it should not be produced.
   * </p>
   */
  public static final String STOP_PROCESSING_KEY = "adpstopprocessing";

  /**
   * <p>
   * Metadata key which if set to 'true' will cause a workflow implementation to skip the produce portion of its processing chain.
   * This effectively means that workflow sequence is stopped after the service collection (configured interceptors will still be
   * triggered) as the {@link Workflow#doProduce(AdaptrisMessage)} method invocation is skipped; this behaviour is not guaranteed
   * and is determined by the underlying concrete {@link Workflow} implementation.
   * </p>
   * <p>
   * The effect of setting this metadata key to 'true' will change the behaviour for standard workflow implementations as follows:
   * </p>
   * <ul>
   * <li>{@link StandardWorkflow} - will cause the producer to be skipped</li>
   * <li>{@link com.adaptris.core.jms.JmsTransactedWorkflow} - will cause the producer to be skipped, but the message will be
   * 'committed' if there have been no failures in the service collection</li>
   * <li>{@link MultiProducerWorkflow} - will cause the standard producer to be skipped; other configured producers that are
   * available will still be invoked.</li>
   * <li>{@link com.adaptris.core.PoolingWorkflow} - will cause the producer to be skipped</li>
   * <li>{@link com.adaptris.core.lms.LargeMessageWorkflow} - will cause the producer to be skipped</li>
   * <li>{@link com.adaptris.core.jms.JmsReplyToWorkflow} - unaffected by this metadata key to ensure that the reply is still sent</li>
   * <li>{@link RequestReplyWorkflow} - unaffected by this metadata key to ensure that a reply to the triggering application can
   * still be sent</li>
   * </ul>
   */
  public static final String KEY_WORKFLOW_SKIP_PRODUCER = "adpworkflowskipproducer";

  /**
   * <p>
   * Metadata value which determines whether or not to stop processing additional services and/or producers
   * </p>
   *
   * @see #STOP_PROCESSING_KEY
   * @see #KEY_WORKFLOW_SKIP_PRODUCER
   */
  public static final String STOP_PROCESSING_VALUE = "true";

  /**
   * <p>
   * Metadata key which <code>MessageSplitterService</code> uses to store the unique ID of the original parent message on the split,
   * child message.
   * </p>
   */
  public static final String PARENT_UNIQUE_ID_KEY = "adpparentguid";

  /**
   * <p>
   * Constant used by branching services to indicate to <code>BranchingServiceCollection</code> that an end point has been reached.
   * </p>
   */
  public static final String ENDPOINT_SERVICE_UNIQUE_ID = "end";

  /**
   * <p>
   * Metadata key for storing the original name (generally file name) of a message.
   * </p>
   */
  public static final String ORIGINAL_NAME_KEY = "originalname";

  /**
   * <p>
   * Metadata key for storing the last modified date of the consumed file.
   * </p>
   */
  public static final String FILE_LAST_MODIFIED_KEY = "lastmodified";

  /**
   * <p>
   * Metadata key for storing the size of the message
   * </p>
   */
  public static final String FS_FILE_SIZE = "fsFileSize";

  /**
   * <p>
   * Metadata key for storing the name (generally file name) of a message that has been sent by a Producer.
   * </p>
   */
  public static final String PRODUCED_NAME_KEY = "producedname";

  /**
   * <p>
   * Metadata key for storing the name space of an event when it is converted to an <code>AdaptrisMessage</code>. NB this is not
   * used when converting back this allows the name space to be used as its destination.
   * </p>
   */
  public static final String EVENT_NAME_SPACE_KEY = "eventnamespace";

  /**
   * The metadata key for storing the classname of the event.
   * <p>
   * It is generally unused, however, it is helpful to have this when unmarshalling the events.
   */
  public static final String EVENT_CLASS = "eventclass";

  /**
   * Metadata key for <code>HttpSession</code> object metadata.
   */
  public static final String HTTP_SESSION_KEY = "httpsession";

  /**
   * Metadata key that contains the http method (GET, POST, PUT etc) when receiving a message via
   * {@link com.adaptris.core.http.jetty.JettyMessageConsumer} or similar.
   *
   */
  public static final String HTTP_METHOD = "httpmethod";

  /**
   * <p>
   * Metadta key for number of previous retries.
   * </p>
   */
  public static final String RETRY_COUNT_KEY = "previousretrycount";

  /**
   * Metadata key specifying the email subject.
   */
  public static final String EMAIL_SUBJECT = "emailsubject";

  /**
   * Metadata key that specifies the name of the attachment.
   */
  public static final String EMAIL_ATTACH_FILENAME = "emailattachmentfilename";

  /**
   * Metadata key that specifies the content-type of the attachment.
   */
  public static final String EMAIL_ATTACH_CONTENT_TYPE = "emailattachment" + "contenttype";

  /**
   * Metadata key that specifies the total number of attachments.
   */
  public static final String EMAIL_TOTAL_ATTACHMENTS = "emailtotalattachments";

  /**
   * Metadata key that specifies the cc list for sending.
   */
  public static final String EMAIL_CC_LIST = "emailcc";

  /**
   * Metadata key specifying the email message container from whence a payload may have spawned.
   */
  public static final String EMAIL_MESSAGE_ID = "emailmessageid";

  /**
   * Metadata key specifying the email body. This is only used if the <code>SmtpProducer</code> is configured to send the document
   * as an attachment.
   */
  public static final String EMAIL_TEMPLATE_BODY = "emailtemplatebody";

  /**
   * Metadata key specifying that security has been encrypted using v1 encryption compability mode.
   */
  public static final String SECURITY_V1_COMPATIBILITY = "v1encryption" + "compatibility";

  /**
   * Metadata key containing the OFTP destination.
   */
  public static final String OFTP_DESTINATION = "oftpdestination";

  /**
   * Metadata key containing the OFTP source.
   */
  public static final String OFTP_SOURCE = "oftpsource";

  /**
   * Metadata key containing the OFTP message id.
   */
  public static final String OFTP_MESSAGE_ID = "oftpmessageid";

  /**
   * Metadata key containing the date from the OFTP sfid.
   * <p>
   * This is the date in the form "yyyy-MM-dd HH:mm:ss zzz".
   */
  public static final String OFTP_DATE = "oftpdate";

  /**
   * Metadata key specifying the message should be treated as an EERP.
   */
  public static final String OFTP_IS_ACKNOWLEDGEMENT = "oftpack";

  /**
   * Metadata key that is used to key the object metadata that contains the raw socket.
   */
  public static final String SOCKET_OBJECT_KEY = "socketobject";

  /**
   * Metadata key for the FTP reply to name override.
   */
  public static final String FTP_REPLYTO_NAME = "ftpreplytoname";

  /**
   * Metadata key for the transaction id sent to and from the business connector.
   */
  public static final String SAPBC_TID = "sapbctid";

  /**
   * Metadata key that allows override of the transform services.
   *
   */
  public static final String TRANSFORM_OVERRIDE = "transformurl";

  /**
   * Metadata key that allows override of the remote partner in security services.
   */
  public static final String SECURITY_REMOTE_PARTNER = "securityremotepartner";

  /**
   * Metadata key that indicates that the AdaptrisMessage payload is a Mime Multipart message
   */
  public static final String MSG_MIME_ENCODED = "adpmimemultipart";

  /**
   * The object metadata key that contains the last captured exception.
   *
   */
  public static final String OBJ_METADATA_EXCEPTION = Exception.class.getName();

  /**
   * The object metadata key that contains the last component that caused the exception (generally a Service).
   *
   */
  public static final String OBJ_METADATA_EXCEPTION_CAUSE = Exception.class.getName() + "_Cause";

  /**
   * Metadata key that contains the last response code from an HTTP Server when using
   * {@link com.adaptris.core.http.client.net.HttpProducer} or similar.
   *
   */
  public static final String HTTP_PRODUCER_RESPONSE_CODE = "adphttpresponse";

  /**
   * Metadata key that contains the IDoc type.
   * <p>
   * This is the value of the field <code>IDOCTYP</code>.
   * </p>
   *
   */
  public static final String JCO_IDOCTYP = "jcoidoctyp";


  /**
   * @deprecated since 3.8.0 use {@link com.adaptris.core.http.jetty.JettyConstants#JETTY_URL} instead
   */
  @Deprecated
  public static final String JETTY_URL = com.adaptris.core.http.jetty.JettyConstants.JETTY_URL;

  /**
   * @deprecated since 3.8.0 use {@link com.adaptris.core.http.jetty.JettyConstants#JETTY_URI} instead
   */
  @Deprecated
  public static final String JETTY_URI = com.adaptris.core.http.jetty.JettyConstants.JETTY_URI;

  /**
   * @deprecated since 3.8.0 use {@link com.adaptris.core.http.jetty.JettyConstants#JETTY_QUERY_STRING} instead
   */
  @Deprecated
  public static final String JETTY_QUERY_STRING = com.adaptris.core.http.jetty.JettyConstants.JETTY_QUERY_STRING;

  /**
   * <p>
   * Metadata key for storing the directory where a file was consumed from.
   * </p>
   */
  public static final String FS_CONSUME_DIRECTORY = "fsConsumeDir";

  /**
   * <p>
   * Metadata key for storing the name of the immediate parent directory that a file was consumed from.
   * </p>
   */
  public static final String FS_CONSUME_PARENT_DIR = "fsParentDir";

  /**
   * <p>
   * Metadata key for storing the directory where a file was produced to by instances of {@link com.adaptris.core.fs.FsProducer}
   * </p>
   */
  public static final String FS_PRODUCE_DIRECTORY = "fsProduceDir";

  /**
   * <p>
   * Regex pattern for the unique id of {@link com.adaptris.core.AdaptrisComponent} which are
   * managed by JMX. This prevent to use reserved characters in the unique id. '@' has been added to
   * prevent issues when a workflow and channel uid are added together like workflowUid@ChannelUid.
   * </p>
   */
  public static final String UNIQUE_ID_JMX_PATTERN = "[^,\\*\\?=:\"\\\\@]+";

}
