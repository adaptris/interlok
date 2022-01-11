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

package com.adaptris.core.services.metadata;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AffectsMetadata;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.services.SequenceNumberOverflowBehaviour;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.PropertyHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.BooleanUtils;

import javax.validation.constraints.NotBlank;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Properties;

/**
 * Simple sequence number generator that uses a file to store the next sequence number.
 * 
 * <p>
 * Rather than using a database to store sequence numbers (c.f.
 * {@link com.adaptris.core.services.jdbc.MetadataIdentitySequenceNumberService} or
 * {@link com.adaptris.core.services.jdbc.StaticIdentitySequenceNumberService}) , this service uses a a plain text file to store the
 * sequence number. Multiple instances of this service should use different files; behaviour is undefined if multiple instances use
 * the same file.
 * </p>
 * <p>
 * The sequence number file consists of a simple key value pair
 * <code>SimpleSequenceNumberService.next=[the next sequence number]</code> which is accessed using a {@link Properties} object. If
 * the property does not exist, then it is defaulted to 1. If you wish to manually modify the file, then the value should always be
 * the next sequence number. If the file does not exist, then a new file is created (using {@link File#createNewFile()} , and the
 * sequence number defaulted to 1.
 * </p>
 * 
 * @config simple-sequence-number-service
 * 
 * 
 * @author lchan
 * 
 */
@XStreamAlias("simple-sequence-number-service")
@AdapterComponent
@ComponentProfile(summary = "Generate a sequence number from a simple text file", tag = "service,sequence")
@DisplayOrder(order = {"metadataKey", "numberFormat", "overflowBehaviour", "maximumSequenceNumber", "sequenceNumberFile", "alwaysReplaceMetadata"})
public class SimpleSequenceNumberService extends ServiceImp
{
  @Getter
  @NotBlank
  @AutoPopulated
  @InputFieldDefault("0")
  private String numberFormat;

  @Getter
  @Setter
  @AdvancedConfig
  @InputFieldDefault(value = "true")
  private Boolean alwaysReplaceMetadata;

  @Getter
  @NotBlank
  @AffectsMetadata
  private String metadataKey;

  @Getter
  @Setter
  private SequenceNumberOverflowBehaviour overflowBehaviour;

  @Getter
  @Setter
  @AdvancedConfig
  private Long maximumSequenceNumber;

  @Getter
  @NotBlank
  private String sequenceNumberFile;

  private static final String PROPERTY_KEY = "SimpleSequenceNumberService.next";
  private static final String ONE = "1";

  public SimpleSequenceNumberService() {
    super();
    setNumberFormat("0");
  }

  /**
   * {@inheritDoc}.
   */
  @Override
  protected void initService() throws CoreException {
    try {
      Args.notBlank(getMetadataKey(), "metadataKey");
      Args.notBlank(getNumberFormat(), "numberFormat");
      Args.notBlank(getSequenceNumberFile(), "sequenceNumberFile");
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  /**
   * {@inheritDoc}.
   */
  @Override
  protected void closeService()
  {
    /* do nothing */
  }

  /**
   * {@inheritDoc}.
   */
  @Override
  public void start() throws CoreException
  {
    super.start();
  }

  /**
   * {@inheritDoc}.
   */
  @Override
  public void stop()
  {
    super.stop();
  }

  /**
   * {@inheritDoc}.
   */
  @Override
  public void prepare() throws CoreException
  {
    /* do nothing */
  }

  /**
   * Set the file that will contain the sequence number.
   *
   * @param s the file.
   */
  public void setSequenceNumberFile(String s) {
    sequenceNumberFile = Args.notBlank(s, "SequenceNumberFile");
  }

  /**
   * Metadata will be formatted using the pattern specified.
   *
   * <p>
   * This allows you to format the number precisely to the value that is required; e.g if you use "000000000" then the metadata
   * value is always 9 characters long, the number being prefixed by leading zeros
   * </p>
   *
   * @see java.text.DecimalFormat
   * @param format the numberFormat to set. The default is '0'; which coupled with the default overflow behaviour of 'Continue'
   *          means it will just use the raw number.
   */
  public void setNumberFormat(String format) {
    numberFormat = Args.notBlank(format, "numberFormat");
  }

  /**
   * Set the metadata key where the resulting sequence number will be stored.
   *
   * @param key the metadataKey to set
   */
  public void setMetadataKey(String key) {
    metadataKey = Args.notBlank(key, "metadataKey");
  }

  public boolean alwaysReplaceMetadata()
  {
    return BooleanUtils.toBooleanDefaultIfNull(getAlwaysReplaceMetadata(), true);
  }

  /**
   * @see com.adaptris.core.Service#doService(com.adaptris.core.AdaptrisMessage)
   */
  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    NumberFormat formatter = new DecimalFormat(getNumberFormat());
    if (!alwaysReplaceMetadata() && msg.headersContainsKey(getMetadataKey())) {
      log.debug("{} already exists, not updating", getMetadataKey());
      return;
    }
    try {
      File myFile = new File(getSequenceNumberFile());
      Properties p = load(myFile);
      long count = Long.parseLong(nextSequenceNumber(p, getMaximumSequenceNumber()));
      String countString = formatter.format(count);
      if (countString.length() > getNumberFormat().length()) {
        count = SequenceNumberOverflowBehaviour.getBehaviour(getOverflowBehaviour()).wrap(count);
        countString = formatter.format(count);
      }
      incrementSequenceNumberProperty(p, count, getMaximumSequenceNumber());
      store(p, myFile);
      msg.addMetadata(getMetadataKey(), countString);
    }
    catch (IOException e) {
      throw new ServiceException("Failed whilst generating sequence number", e);
    }
  }


  private static Properties load(File myFile) throws IOException {
    if (!myFile.exists()) {
      myFile.createNewFile();
    }
    return PropertyHelper.loadQuietly(myFile);
  }

  private static void store(Properties p, File myFile) throws IOException {
    try (OutputStream out = new FileOutputStream(myFile)) {
      p.store(out, "");
    }
  }

  private static String nextSequenceNumber(Properties p, Long maximum) {
    if (!p.containsKey(PROPERTY_KEY)) {
      p.setProperty(PROPERTY_KEY, ONE);
    }
    if(greaterThanMaximum(p.getProperty(PROPERTY_KEY), maximum)){
      p.setProperty(PROPERTY_KEY, ONE);
    }
    return p.getProperty(PROPERTY_KEY);
  }

  private static void incrementSequenceNumberProperty(Properties p, Long count, Long maximum){
    if(greaterThanMaximum(String.valueOf(count + 1), maximum)){
      p.setProperty(PROPERTY_KEY, ONE);
    } else {
      p.setProperty(PROPERTY_KEY, String.valueOf(count + 1));
    }
  }

  private static boolean greaterThanMaximum(String value, Long maximum){
    if(maximum == null){
      return false;
    }
    long count = Long.parseLong(value);
    return count > maximum;
  }

}
