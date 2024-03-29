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

package com.adaptris.security;

import java.io.File;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers;

import com.adaptris.core.util.PropertyHelper;
import com.adaptris.security.certificate.CertificateBuilder;
import com.adaptris.security.certificate.CertificateBuilderFactory;
import com.adaptris.security.certificate.CertificateParameter;
import com.adaptris.security.exc.AdaptrisSecurityException;
import com.adaptris.security.keystore.KeystoreFactory;
import com.adaptris.security.keystore.KeystoreLocation;
import com.adaptris.security.keystore.KeystoreProxy;
import com.adaptris.security.util.Constants;
import com.adaptris.security.util.SecurityUtil;
import com.adaptris.util.SafeGuidGenerator;

/**
 */
public class Config {

  static final String CFG_ROOT = "test.cfg.root";

  static final String SECURITY_PROPERTIES = "security-test.properties";

  static final String REMOTE_TESTS_ENABLED = "test.remote.location.enabled";

  static final String KEYSTORE_REMOTE_ROOT = "keystore.remote.root";
  static final String KEYSTORE_REMOTE_REALPATH = "keystore.remote.realpath";

  static final String KEYSTORE_TEST_URL = "keystore.keystoreUrl";
  static final String KEYSTORE_CA_URL = "keystore.ca.keystoreUrl";
  static final String KEYSTORE_COMMON_KEYSTORE_PW = "keystores.common.keystorePassword";
  static final String KEYSTORE_COMMON_PRIVKEY_PW = "keystores.common.privateKeyPassword";
  static final String KEYSTORE_COMMON_PRIVKEY_ALIAS = "keystores.common.privatekey.alias";
  static final String KEYSTORE_COMMON_CA_ALIAS = "keystores.common.ca.alias";
  static final String KEYSTORE_TEST_NEW_URL = "keystore.new.keystoreUrl";
  static final String KEYSTORE_TEST_NEW_PW = "keystore.new.keystorePassword";
  static final String KEYSTORE_SINGLE_X509_URL = "keystore.single.x509url";
  static final String KEYSTORE_SINGLE_XML_KEY_INFO_URL = "keystore.single.xmlkeyinfourl";
  static final String KEYSTORE_SINGLE_XML_KEY_INFO_ALIAS = "keystore.single.xmlkeyinfourl.alias";
  static final String KEYSTORE_SINGLE_X509_ALIAS = "keystore.single.x509url.alias";
  static final String KEYSTORE_SINGLE_PKCS12_ALIAS = "keystore.single.pkcs12url.alias";

  static final String KEYSTORE_SINGLE_X509_URL_UPPERCASE = "keystore.single.x509url.uppercase";
  static final String KEYSTORE_SINGLE_PKCS12_URL_UPPERCASE = "keystore.single.pkcs12url.uppercase";

  static final String KEYSTORE_SINGLE_X509_ALIAS_UPPERCASE = "keystore.single.x509url.uppercaseAlias";

  static final String KEYSTORE_SINGLE_XML_KEY_INFO_URL_UPPERCASE = "keystore.single.xmlkeyinfourl.uppercase";
  static final String KEYSTORE_SINGLE_XML_KEY_INFO_ALIAS_UPPERCASE = "keystore.single.xmlkeyinfourl.uppercaseAlias";

  static final String KEYSTORE_IMPORT_PKCS12_FILE = "keystore.import.pkcs12.file";
  static final String KEYSTORE_IMPORT_CERTCHAIN_FILE = "keystore.import.certchain.file";
  static final String KEYSTORE_IMPORT_X509_FILE = "keystore.import.x509.file";

  static final String KEYSTORE_COMPOSITE_URLROOT = "keystore.composite";

  static final String CERTIFICATE_C = "certificate.country";
  static final String CERTIFICATE_ST = "certificate.stateOrProvince";
  static final String CERTIFICATE_L = "certificate.locality";
  static final String CERTIFICATE_O = "certificate.organisation";
  static final String CERTIFICATE_OU = "certificate.organisationalUnit";
  static final String CERTIFICATE_CN = "certificate.commonName";
  static final String CERTIFICATE_EMAIL = "certificate.emailAddress";
  static final String CERTIFICATE_SIGALG = "certificate.signaturealgorithm";
  static final String CERTIFICATE_KEYALG = "certificate.keyalgorithm";
  static final String CERTIFICATE_KEYSIZE = "certificate.keyalgorithm.size";

  static final String CERTIFICATE_IGNORE_REVOKED = "certificate.ignore.revoked";

  static final String CERTHANDLER_EXPIRED = "certificate.handler.expired";
  static final String CERTHANDLER_GOOD = "certificate.handler.good";

  static final String SECURITY_ALG = "security.algorithm";
  static final String SECURITY_ALGSIZE = "security.algorithm.keysize";

  private static Config instance = null;

  private Properties config = null;

  private Config() {
    try {
      config = PropertyHelper.loadQuietly(() -> {
        return this.getClass().getClassLoader().getResourceAsStream(SECURITY_PROPERTIES);
      });
      if (config.size() == 0) {
        throw new Exception("No Configuration(security-test.properties) available");
      }
      SecurityUtil.addProvider();
      System.setProperty(Constants.IGNORE_REV, config.getProperty(Config.CERTIFICATE_IGNORE_REVOKED, "false"));
      File cfgRoot = new File(config.getProperty(CFG_ROOT));
      cfgRoot.mkdirs();
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public synchronized static final Config getInstance() {
    if (instance == null) {
      instance = new Config();
    }
    return instance;
  }

  /** Get the properties that is our configuration */
  public Properties getProperties() {
    return (Properties) config.clone();
  }

  public Properties getPropertySubset(String prefix) {
    return PropertyHelper.getPropertySubset(config, prefix);
  }

  public String getProperty(String key) {
    return config.getProperty(key);
  }

  public String getProperty(String key, String defaultValue) {
    return config.getProperty(key, defaultValue);
  }


  public CertificateBuilder getBuilder(String commonName) throws Exception {

    CertificateBuilder builder = CertificateBuilderFactory.getInstance().createBuilder();
    CertificateParameter cp = new CertificateParameter();
    X500NameBuilder subject = new X500NameBuilder();

    subject.addRDN(X509ObjectIdentifiers.countryName, config.getProperty(CERTIFICATE_C));
    subject.addRDN(X509ObjectIdentifiers.stateOrProvinceName, config.getProperty(CERTIFICATE_ST));
    subject.addRDN(X509ObjectIdentifiers.localityName, config.getProperty(CERTIFICATE_L));
    subject.addRDN(X509ObjectIdentifiers.organization, config.getProperty(CERTIFICATE_O));
    subject.addRDN(X509ObjectIdentifiers.organizationalUnitName, config.getProperty(CERTIFICATE_OU));
    subject.addRDN(X509ObjectIdentifiers.commonName, commonName);
    subject.addRDN(PKCSObjectIdentifiers.pkcs_9_at_emailAddress, config.getProperty(CERTIFICATE_EMAIL));

    cp.setSignatureAlgorithm(config.getProperty(CERTIFICATE_SIGALG));

    cp.setKeyAlgorithm(config.getProperty(CERTIFICATE_KEYALG), Integer.parseInt(config.getProperty(CERTIFICATE_KEYSIZE)));
    cp.setSubjectInfo(subject.build());

    builder.setCertificateParameters(cp);
    return builder;
  }

  public KeystoreLocation buildKeystore(String ksUrl, String cn, boolean overwrite)
      throws Exception {

    String commonName =
        StringUtils.defaultIfBlank(cn, config.getProperty(KEYSTORE_COMMON_PRIVKEY_ALIAS));
    KeystoreLocation ksc = KeystoreFactory.getDefault().create(ksUrl,
        config.getProperty(Config.KEYSTORE_COMMON_KEYSTORE_PW).toCharArray());
    KeystoreProxy ksp = KeystoreFactory.getDefault().create(ksc);
    if (ksc.exists() && overwrite == false) {
      ksp.load();
    }
    CertificateBuilder builder = getBuilder(commonName);
    Certificate selfCert = builder.createSelfSignedCertificate();
    PrivateKey privkey = builder.getPrivateKey();
    char[] password = config.getProperty(KEYSTORE_COMMON_PRIVKEY_PW).toCharArray();
    Certificate[] certChain = new Certificate[1];
    certChain[0] = selfCert;
    ksp.setPrivateKey(commonName, privkey, password, certChain);
    ksp.commit();
    return ksc;
  }

  public KeystoreLocation newKeystore(String cn) throws Exception {
    String uniqueName = new SafeGuidGenerator().safeUUID();
    String keystoreUrl =
        String.format("file:///%s/%s?keystoreType=jks", config.getProperty(CFG_ROOT), uniqueName);
    return buildKeystore(keystoreUrl, cn, false);
  }

  public void importPrivateKey(String ksUrl, String filename, boolean overwrite) throws AdaptrisSecurityException, IOException {
    String commonName = config.getProperty(KEYSTORE_COMMON_PRIVKEY_ALIAS);
    KeystoreLocation ksc = KeystoreFactory.getDefault().create(ksUrl,
        config.getProperty(Config.KEYSTORE_COMMON_KEYSTORE_PW).toCharArray());
    KeystoreProxy ksp = KeystoreFactory.getDefault().create(ksc);
    if (ksc.exists() && overwrite == false) {
      ksp.load();
    }
    ksp.importPrivateKey(commonName, config.getProperty(KEYSTORE_COMMON_PRIVKEY_PW).toCharArray(), filename,
        config.getProperty(KEYSTORE_COMMON_PRIVKEY_PW).toCharArray());
    ksp.commit();
  }
}
