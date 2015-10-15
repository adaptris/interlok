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

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;
import com.thoughtworks.xstream.annotations.XStreamAlias;

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
 * @license BASIC
 * @author lchan
 * 
 */
@XStreamAlias("simple-sequence-number-service")
public class SimpleSequenceNumberService extends ServiceImp {

  @NotBlank
  private String numberFormat;
  private Boolean alwaysReplaceMetadata;
  @NotBlank
  private String metadataKey;
  private OverflowBehaviour overflowBehaviour;
  @NotBlank
  private String sequenceNumberFile;

  private static final String PROPERTY_KEY = "SimpleSequenceNumberService.next";
  private static final String ONE = "1";

  /**
   * The behaviour of the sequence number generator when the number exceeds that specified by the number format.
   *
   *
   */
  public enum OverflowBehaviour {
    ResetToOne() {
      @Override
      long wrap(long i) {
        return Long.valueOf(ONE);
      }

    },
    Continue() {
      @Override
      long wrap(long i) {
        return i;
      }
    };
    abstract long wrap(long i);
  }

  public SimpleSequenceNumberService() {
    super();
    setNumberFormat("0");
  }

  /** @see com.adaptris.core.AdaptrisComponent#init() */
  @Override
  public void init() throws CoreException {
    if (isEmpty(getMetadataKey())) {
      throw new CoreException("MetadataKey to set the sequence number against is null/empty");
    }
    if (isEmpty(getSequenceNumberFile())) {
      throw new CoreException("File containing the sequence number is null/empty");
    }
  }

  @Override
  public void start() throws CoreException {
    super.start();
  }

  @Override
  public void stop() {
    super.stop();
  }

  @Override
  public void close() {
  }

  /**
   * @see com.adaptris.core.Service#doService(com.adaptris.core.AdaptrisMessage)
   */
  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    NumberFormat formatter = new DecimalFormat(getNumberFormat());
    if (!alwaysReplaceMetadata() && msg.containsKey(getMetadataKey())) {
      log.debug(getMetadataKey() + " already exists, not updating");
      return;
    }
    try {
      File myFile = new File(getSequenceNumberFile());
      Properties p = load(myFile);
      Long count = Long.parseLong(nextSequenceNumber(p));
      String countString = formatter.format(count);
      if (countString.length() > getNumberFormat().length()) {
        count = getBehaviour(getOverflowBehaviour()).wrap(count);
        countString = formatter.format(count);
      }
      p.setProperty(PROPERTY_KEY, String.valueOf(count + 1));
      store(p, myFile);
      msg.addMetadata(getMetadataKey(), countString);
    }
    catch (IOException e) {
      throw new ServiceException("Failed whilst generating sequence number", e);
    }
  }

  /**
   * @return the metadataKey
   */
  public String getMetadataKey() {
    return metadataKey;
  }

  /**
   * Set the metadata key where the resulting sequence number will be stored.
   *
   * @param key the metadataKey to set
   */
  public void setMetadataKey(String key) {
    if (isEmpty(key)) {
      throw new IllegalArgumentException("MetadataKey may not be null/empty");
    }
    metadataKey = key;
  }

  /**
   * @return the numberFormat
   */
  public String getNumberFormat() {
    return numberFormat;
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
    if (isEmpty(format)) {
      throw new IllegalArgumentException("Numberformat may not be null/empty");
    }
    numberFormat = format;
  }


  public Boolean getAlwaysReplaceMetadata() {
    return alwaysReplaceMetadata;
  }

  /**
   * Whether or not to always replace the metadata key.
   *
   * @param t the alwaysReplaceMetadata to set, default is true.
   */
  public void setAlwaysReplaceMetadata(Boolean t) {
    alwaysReplaceMetadata = t;
  }

  private boolean alwaysReplaceMetadata() {
    return getAlwaysReplaceMetadata() != null ? getAlwaysReplaceMetadata().booleanValue() : true;
  }

  public OverflowBehaviour getOverflowBehaviour() {
    return overflowBehaviour;
  }

  /**
   * Set the behaviour when the sequence number exceeds that specified by the number format.
   * 
   * @param s the behaviour to set (default is {@link OverflowBehaviour#Continue})
   * @see OverflowBehaviour
   */
  public void setOverflowBehaviour(OverflowBehaviour s) {
    overflowBehaviour = s;
  }

  private static OverflowBehaviour getBehaviour(OverflowBehaviour s) {
    return s != null ? s : OverflowBehaviour.Continue;
  }

  public String getSequenceNumberFile() {
    return sequenceNumberFile;
  }

  /**
   * Set the file that will contain the sequence number.
   *
   * @param s the file.
   */
  public void setSequenceNumberFile(String s) {
    if (isEmpty(s)) {
      throw new IllegalArgumentException("SequenceNumberFile may not be null/empty");
    }
    sequenceNumberFile = s;
  }

  private static Properties load(File myFile) throws IOException {
    Properties result = new Properties();
    InputStream in = null;
    try {
      if (!myFile.exists()) {
        myFile.createNewFile();
      }
      in = new FileInputStream(myFile);
      result.load(in);
    }
    finally {
      IOUtils.closeQuietly(in);
    }
    return result;
  }

  private static void store(Properties p, File myFile) throws IOException {
    OutputStream out = null;
    try {
      out = new FileOutputStream(myFile);
      p.store(out, "");
    }
    finally {
      IOUtils.closeQuietly(out);
    }
  }

  private static String nextSequenceNumber(Properties p) {
    if (!p.containsKey(PROPERTY_KEY)) {
      p.setProperty(PROPERTY_KEY, ONE);
    }
    return p.getProperty(PROPERTY_KEY);
  }

  @Override
  public boolean isEnabled(License license) throws CoreException {
    return license.isEnabled(LicenseType.Basic);
  }

}
