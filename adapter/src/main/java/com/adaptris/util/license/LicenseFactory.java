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

package com.adaptris.util.license;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.util.URLString;

/**
 * The License Factory for obtaining a license object.
 */
public final class LicenseFactory {

  protected static transient final Logger log = LoggerFactory.getLogger(LicenseFactory.class.getName());

  private static DefaultLicenseFactory factory = new DefaultLicenseFactory();

  private static final String LICENSE_PROPERTY_KEY = "adp.license.key";

  private LicenseFactory() {
  }

  /**
   * Retrieve a license Object from the specified location.
   * <p>
   *
   * @param location the location
   * @return a License object
   * @throws LicenseException if there was a failure to parse/retrieve the object.
   */
  public static License getLicense(String location) throws LicenseException {
    return factory.readLicense(location);
  }

  /**
   * Create a License object directly from a string.
   *
   * @param licenseString the string.
   * @return a license object.
   * @throws LicenseException if the license is not valid.
   */
  public static License create(String licenseString) throws LicenseException {
    return factory.create(licenseString);
  }

  private static final class DefaultLicenseFactory {
    DefaultLicenseFactory() {
    }

    License readLicense(String location) throws LicenseException {

      String licenseKey = null;
      URI urlString = null;
      try {
        try {
          urlString = new URI(location);
        }
        catch (URISyntaxException e) {
          log.trace("Attempting Work-around for invalid RFC2396 URI [" + location + "]");
          // So there's basically a fully qualified Windows path (i.e. file:///c:/fred).
          if (location.startsWith("file://") && location.split(":").length >= 3) {
            URLString fakeUrlString = new URLString(location);
            String filePath = "/" + fakeUrlString.getFile();
            urlString = new URI("file", null, null, -1, filePath, null, null);
          }
          else {
            throw e;
          }
        }
        // log.trace("Ok created a URI : " + urlString.toString());
        if ("file".equalsIgnoreCase(urlString.getScheme()) || urlString.getScheme() == null) {
          licenseKey = readFromFile(urlString.getPath());
        }
        else {
          URL actualURL = new URL(location);
          URLConnection urlC = actualURL.openConnection();
          urlC.setDoOutput(true);
          urlC.setDoInput(true);
          licenseKey = readLicense(urlC.getInputStream());
        }
      }
      catch (Exception e) {
        throw new LicenseException(e);
      }
      return create(licenseKey);
    }

    private String readFromFile(String fname) throws LicenseException {
      log.trace("Reading license from [" + fname + "]");
      String fileToRead = fname;
      if (fname.startsWith("/") || fname.startsWith("\\")) {
        fileToRead = fname.substring(1);
      }
      File file = new File(fileToRead);
      String licenseKey = null;
      InputStream in = null;
      try {
        if (file.exists()) {
          in = new FileInputStream(file);
          licenseKey = readLicense(in);
        }
        else {
          in = this.getClass().getClassLoader().getResourceAsStream(fileToRead);
          if (in != null) {
            licenseKey = readLicense(in);
          }
        }
      }
      catch (IOException e) {
        throw new LicenseException(e);
      }
      finally {
        IOUtils.closeQuietly(in);
      }
      return licenseKey;
    }

    private String readLicense(InputStream input) throws IOException {
      Properties p = new Properties();
      p.load(input);
      return p.getProperty(LICENSE_PROPERTY_KEY);
    }

    License create(String licenseString) throws LicenseException {
      if (isEmpty(licenseString)) {
        // log.trace("No License String, default to RestrictedLicense");
        return new RestrictedLicense();
      }
      return new SimpleLicense(licenseString);
    }
  }
}
