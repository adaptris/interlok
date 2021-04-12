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

package com.adaptris.core.jms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.validation.Valid;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AffectsMetadata;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.metadata.ExcludeJmsHeaders;
import com.adaptris.core.metadata.MetadataFilter;
import com.adaptris.core.metadata.NoOpMetadataFilter;
import com.adaptris.core.util.LifecycleHelper;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

// abstract factory pattern

/**
 * <p>
 * Super class of classes that translate <code>AdaptrisMessage</code>s to the
 * various type of <code>javax.jms.Message</code>s, and vice versa. Set a
 * <code>metadataFilter</code> to move metadata when the message is translated.
 * If the moveJmsHeaders flag is true, JMS  headers will be moved as well.
 * </p>
 */
public abstract class MessageTypeTranslatorImp implements MessageTypeTranslator, MetadataHandlerContext {

  protected transient Logger log = LoggerFactory.getLogger(this.getClass());
  protected transient Session session; // not persisted
  private transient AdaptrisMessageFactory messageFactoryToUse;
  protected transient MetadataHandler helper;

  private static final MetadataFilter DEFAULT_FILTER = new NoOpMetadataFilter();
  /**
   * Set the filter that will be used return a subset of the messages metdata to be copied over during the translate.
   */
  @AdvancedConfig
  @Valid
  @AutoPopulated
  @AffectsMetadata
  @InputFieldDefault(value = "no-op-metadata-filter")
  private MetadataFilter metadataFilter;
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  @AffectsMetadata
  private Boolean moveJmsHeaders;
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean reportAllErrors;

  @AdvancedConfig(rare = true)
  @XStreamImplicit
  private List<MetadataConverter> metadataConverters;

  /**
   * <p>
   * Creates a new instance. By default
   * <ul>
   * <li>move-jms-headers = false</li>
   * <li>report-all-errors = false</li>
   * </ul>
   */
  public MessageTypeTranslatorImp() {
    registerMessageFactory(new DefaultMessageFactory());
    helper = new MetadataHandler(this);
  }

  /**
   * Return the current metadata filter.
   *
   * @return {@link com.adaptris.core.metadata.MetadataFilter}
   * @see MetadataHandlerContext#metadataFilter()
   */
  public MetadataFilter getMetadataFilter() {
    return metadataFilter;
  }

  /**
   * Set the {@link MetadataFilter} to be used when converting between JMS messages and AdaptrisMessage objects
   * 
   * @param mf the metadata filter implementation, default is {@link com.adaptris.core.metadata.NoOpMetadataFilter} which means
   *          <strong>ALL</strong> metadata is copied into the JMS Message, and vice-versa.
   * @see MetadataHandlerContext#metadataFilter()
   * @since 3.0.2
   * @see ExcludeJmsHeaders
   * @see MetadataFilter
   */
  public void setMetadataFilter(MetadataFilter mf) {
    metadataFilter = mf;
  }


  @Override
  public void registerSession(Session s) {
    session = s;
  }

  @Override
  public Session currentSession() {
    return session;
  }

  /**
   * <p>
   * Returns true if JMS Headers (as well as JMS Properties) should be copied.
   * </p>
   *
   * @return true if JMS Headers (as well as JMS Properties) should be copied, otherwise false
   * @see MetadataHandlerContext#moveJmsHeaders()
   */
  public Boolean getMoveJmsHeaders() {
    return moveJmsHeaders;
  }

  /**
   * <p>
   * Sets whether JMS Headers (as well as JMS Properties) should be copied.
   * </p>
   *
   * @param b true if JMS Headers (as well as JMS Properties) should be copied, otherwise false
   * @see MetadataHandlerContext#moveJmsHeaders()
   */
  public void setMoveJmsHeaders(Boolean b) {
    moveJmsHeaders = b;
  }

  /**
   * @return the reportAllErrors
   * @see MetadataHandlerContext#reportAllErrors()
   */
  public Boolean getReportAllErrors() {
    return reportAllErrors;
  }

  /**
   * Set the list of metadata converters to uses when converting from AdaptrisMessage to JMS Message.
   * @param mc list of message converters
   */
  public void setMetadataConverters(List<MetadataConverter> mc) {
    this.metadataConverters = mc;
  }

  /**
   * Get the list of metadata converters to uses when converting from AdaptrisMessage to JMS Message.
   * @return list of message converters
   */
  public List<MetadataConverter> getMetadataConverters() {
    return metadataConverters;
  }

  /**
   * Report all non-critical errors with a stacktrace.
   * <p>
   * When moving JMS Headers, it is possible depending on the vendor that some exceptions are thrown when attempting to get standard
   * JMS headers. By default, these errors are simply logged without a stack-trace. If the full strack trace is required, then set
   * this to be true.
   * </p>
   *
   * @param b the reportAllErrors to set
   * @see MetadataHandlerContext#reportAllErrors()
   */
  public void setReportAllErrors(Boolean b) {
    reportAllErrors = b;
  }


  @Override
  public void registerMessageFactory(AdaptrisMessageFactory f) {
    messageFactoryToUse = f;
  }

  @Override
  public AdaptrisMessageFactory currentMessageFactory() {
    return messageFactoryToUse;
  }

  @Override
  public boolean moveJmsHeaders() {
    return BooleanUtils.toBooleanDefaultIfNull(getMoveJmsHeaders(), false);
  }

  @Override
  public boolean reportAllErrors() {
    return BooleanUtils.toBooleanDefaultIfNull(getReportAllErrors(), false);
  }


  @Override
  public MetadataFilter metadataFilter() {
    return ObjectUtils.defaultIfNull(getMetadataFilter(), DEFAULT_FILTER);
  }

  @Override
  public List<MetadataConverter> metadataConverters(){
    return ObjectUtils.defaultIfNull(getMetadataConverters(), Collections.emptyList());
  }

  public <T extends MessageTypeTranslatorImp> T withMetadataConverters(
      List<MetadataConverter> list) {
    setMetadataConverters(list);
    return (T) this;
  }

  public <T extends MessageTypeTranslatorImp> T withMetadataConverters(
      MetadataConverter... list) {
    return withMetadataConverters(new ArrayList<>(Arrays.asList(list)));
  }

  public <T extends MessageTypeTranslatorImp> T withReportAllErrors(Boolean b) {
    setReportAllErrors(b);
    return (T) this;
  }

  public <T extends MessageTypeTranslatorImp> T withMoveJmsHeaders(Boolean b) {
    setMoveJmsHeaders(b);
    return (T) this;
  }

  public <T extends MessageTypeTranslatorImp> T withMetadataFilter(MetadataFilter f) {
    setMetadataFilter(f);
    return (T) this;
  }


  /**
   * Convenience method to translate a {@link Message} into a {@link com.adaptris.core.AdaptrisMessage}.
   *
   * <p>
   * If translation fails, then an attempt is made to use an {@link AutoConvertMessageTranslator} instance to attempt to translate
   * the message so that configurations are handled as well as possible.
   * </p>
   *
   * @param translator the translator to use.
   * @param msg the javax.jms.Message
   * @return an AdaptrisMessage instance
   * @throws JMSException if the message could not be translated.
   */
  public static AdaptrisMessage translate(MessageTypeTranslator translator, Message msg) throws JMSException {
    if(msg == null)
      return null;
    Logger logR = LoggerFactory.getLogger(translator.getClass());
    AdaptrisMessage result = null;
    try {
      result = translator.translate(msg);
    }
    catch (Exception e) {
      logR.warn("Can't handle {} using {}", msg.getClass().getName(), translator.getClass().getName());
      MessageTypeTranslator mt = replicate(translator);
      logR.warn("Assuming mis-configuration and attempting to use {}", mt.getClass().getName());
      try {
        start(mt);
        result = mt.translate(msg);
      }
      finally {
        stop(mt);
      }
    }
    return result;
  }

  private static AutoConvertMessageTranslator replicate(MessageTypeTranslator mt) throws JMSException {
    AutoConvertMessageTranslator result = new AutoConvertMessageTranslator();
    copyConfiguration(mt, result);
    return result;
  }

  protected static MessageTypeTranslatorImp copyConfiguration(MessageTypeTranslator source, MessageTypeTranslatorImp dest)
      throws JMSException {
    dest.registerSession(source.currentSession());
    if (source instanceof MessageTypeTranslatorImp) {
      MessageTypeTranslatorImp impl = (MessageTypeTranslatorImp) source;
      dest.setMoveJmsHeaders(impl.getMoveJmsHeaders());
      dest.setReportAllErrors(impl.getReportAllErrors());
      dest.setMetadataFilter(impl.getMetadataFilter());
      dest.setMetadataConverters(impl.getMetadataConverters());
      dest.registerMessageFactory(impl.currentMessageFactory());
    }
    return dest;
  }

  protected static void start(MessageTypeTranslator mt) throws JMSException {
    try {
      LifecycleHelper.initAndStart(mt, false);
    }
    catch (CoreException e) {
      JmsUtils.rethrowJMSException(e);
    }
  }

  protected static void stop(MessageTypeTranslator mt) {
    LifecycleHelper.stopAndClose(mt, false);
  }
}
