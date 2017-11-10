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

package com.adaptris.core.security;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.Args;
import com.adaptris.security.EncryptionAlgorithm;
import com.adaptris.security.SecurityService;
import com.adaptris.security.SecurityServiceFactory;
import com.adaptris.security.exc.AdaptrisSecurityException;
import com.adaptris.security.exc.PasswordException;
import com.adaptris.security.keystore.Alias;
import com.adaptris.security.keystore.ConfiguredKeystore;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * The base class for all security services.
 *
 * @see SecurityService
 */
public abstract class CoreSecurityService extends ServiceImp {

  private String remotePartner = "";
  private String localPartner = "";
  private SecurityServiceFactory securityFactory;
  @NotNull
  @Valid
  @AutoPopulated
  private EncryptionAlgorithm encryptionAlgorithm = null;
  @AdvancedConfig
  private String successId = null;
  @AdvancedConfig
  private String failId = null;
  @AdvancedConfig
  private String remotePartnerMetadataKey;
  @AutoPopulated
  @NotNull
  @Valid
  private PrivateKeyPasswordProvider privateKeyPasswordProvider;
  @XStreamImplicit
  private List<ConfiguredKeystore> keystoreUrls;

  protected transient boolean branchingEnabled = false;
  private transient char[] pkPassword = null;
  private transient Alias localPartnerAlias = null, remotePartnerAlias = null;
  private transient SecurityService service = null;

  /**
   * Creates a new Object with the following defaults.
   * <ul>
   * <li>Default encryption algorithm is DESede/CBC/PKCS5Padding, KeySize=168</li>
   * <li>V1 compatibility is false</li>
   * <li>security factory is null (suitable for communication with V1 implementations)</li>
   * </ul>
   */
  public CoreSecurityService() {
    encryptionAlgorithm = new EncryptionAlgorithm("DESede/CBC/PKCS5Padding", 168);
    keystoreUrls = new ArrayList<ConfiguredKeystore>();
    setRemotePartnerMetadataKey(CoreConstants.SECURITY_REMOTE_PARTNER);
    setPrivateKeyPasswordProvider(new LegacyPrivateKeyPasswordProvider());
  }

  /**
   *
   * @see com.adaptris.core.ServiceImp#isBranching()
   */
  @Override
  public boolean isBranching() {
    return branchingEnabled;
  }

  /**
   * Get the list of keystore urls.
   *
   * @return the list of keystore urls.
   */
  public List<ConfiguredKeystore> getKeystoreUrls() {
    return keystoreUrls;
  }

  /**
   * Set the keystore urls.
   *
   * @param list the list of keystore urls.
   */
  public void setKeystoreUrls(List<ConfiguredKeystore> list) {
    keystoreUrls = Args.notNull(list, "keystoreUrls");
  }

  /**
   * Add an url to the list.
   * <p>
   * Valid URLS are in the form <br />
   * <code>[protocol]://[server]:[port]/[path]?keystoreType=[type]&
   * keystorePassword=[password]</code>
   * <ul>
   * <li>protocol - the protocol to use, e.g. http</li>
   * <li>server - the server hosting the keystore</li>
   * <li>port - the port through which the protocol will operate</li>
   * <li>path - Path to the keystore</li>
   * <li>type - The type of keystore to use (e.g. JKS)</li>
   * <li>password - password to the keystore</li>
   * </ul>
   * </p>
   * <p>
   * An example would be <code>http://www.adaptris.com/my.ks?keystoreType=JKS&
   * keystorePassword=ABCDE</code> or
   * <code>file://localhost/c:/my.ks?keystoreType=JKS&
   * keystorePassword=ABCDE</code>
   * </p>
   * <p>
   * For the purposes of security each of the keystores will be searched in turn for the matching partner information. Only the
   * first matching partner will be used.
   * </p>
   *
   * @param url an individual url
   * @see #getKeystoreUrls()
   * @see ConfiguredKeystore
   */
  public void addKeystoreUrl(ConfiguredKeystore url) {
    keystoreUrls.add(url);
  }

  /**
   * Set the local partner keystore alias.
   *
   * @param s the local partner
   */
  public void setLocalPartner(String s) {
    localPartner = s;
  }

  /**
   * Get the local partner keystore alias.
   *
   * @return the local partner
   */
  public String getLocalPartner() {
    return localPartner;
  }

  /**
   * Set the remote partner keystore alias.
   * <p>
   * If this is not set, then it will be derived from metadata.
   * </p>
   *
   * @see CoreConstants#SECURITY_REMOTE_PARTNER
   * @param s the remote partner
   */
  public void setRemotePartner(String s) {
    remotePartner = s;
  }

  /**
   * Get the remote partner alias.
   *
   * @return the remote partner.
   */
  public String getRemotePartner() {
    return remotePartner;
  }

  /**
   * Set the encryption algorithm to be used.
   *
   * @param enc the encryption algorithm
   */
  public void setEncryptionAlgorithm(EncryptionAlgorithm enc) {
    encryptionAlgorithm = Args.notNull(enc, "encryptionAlgorithm");
  }

  /**
   * Get the encryption algorithm to be used.
   *
   * @return the encryption algorithm
   */
  public EncryptionAlgorithm getEncryptionAlgorithm() {
    return encryptionAlgorithm;
  }

  /**
   * Set the type of encryption to be used.
   *
   * @param s the encryption type.
   */
  public void setSecurityFactory(SecurityServiceFactory s) {
    securityFactory = s;
  }

  /**
   * Get the encryption type to be used.
   *
   * @return the encryption type.
   */
  public SecurityServiceFactory getSecurityFactory() {
    return securityFactory;
  }

  @Override
  protected final void initService() throws CoreException {
    try {
      pkPassword = getPrivateKeyPasswordProvider().retrievePrivateKeyPassword();
    }
    catch (PasswordException e) {
      throw new CoreException("Could not get password using " + getPrivateKeyPasswordProvider().getClass().getCanonicalName(), e);
    }

    try {
      if (isEmpty(localPartner)) {
        throw new CoreException("No Local Partner configured");
      }
      localPartnerAlias = new Alias(localPartner, pkPassword);
      if (isEmpty(remotePartner)) {
        log.warn("Remote partner not configured,  " + "must be set individually as message metadata");
      }
      else {
        remotePartnerAlias = new Alias(remotePartner);
      }
      SecurityServiceFactory factory = securityFactory;
      if (factory == null) {
        factory = SecurityServiceFactory.defaultInstance();
      }
      factory.prepare();
      service = factory.createService();
      for (Iterator i = keystoreUrls.iterator(); i.hasNext();) {
        ConfiguredKeystore url = (ConfiguredKeystore) i.next();
        service.registerKeystore(url);
      }
      service.setEncryptionAlgorithm(encryptionAlgorithm);
      if (successId != null && failId != null) {
        branchingEnabled = true;
      }
      else {
        log.debug("No Success Id or Fail Id, branching disabled");
      }
    }
    catch (AdaptrisSecurityException e) {
      throw new CoreException(e);
    }
  }

  @Override
  protected void closeService() {
    return;
  }

  @Override
  public void prepare() throws CoreException {

  }


  final Alias retrieveLocalPartner() {
    return localPartnerAlias;
  }

  final Alias retrieveRemotePartner(AdaptrisMessage m) throws AdaptrisSecurityException {
    Alias rpa = remotePartnerAlias;
    if (m.containsKey(getRemotePartnerMetadataKey())) {
      String aliasName = m.getMetadataValue(getRemotePartnerMetadataKey());
      log.debug("Message metadata overrides configured remote partner with [" + aliasName + "]");
      rpa = new Alias(aliasName);
    }
    if (rpa == null) {
      throw new AdaptrisSecurityException("No Remote Partner alias");
    }
    return rpa;
  }

  final SecurityService retrieveSecurityImplementation() {
    return service;
  }

  /**
   * @return the failId
   */
  public String getFailId() {
    return failId;
  }

  /**
   * @param s the failId to set
   */
  public void setFailId(String s) {
    failId = s;
  }

  /**
   * @return the successId
   */
  public String getSuccessId() {
    return successId;
  }

  /**
   * @param s the successId to set
   */
  public void setSuccessId(String s) {
    successId = s;
  }

  public String getRemotePartnerMetadataKey() {
    return remotePartnerMetadataKey;
  }

  public void setRemotePartnerMetadataKey(String s) {
    remotePartnerMetadataKey = s;
  }

  public PrivateKeyPasswordProvider getPrivateKeyPasswordProvider() {
    return privateKeyPasswordProvider;
  }

  /**
   * Set the private key password provider.
   *
   * @param pkpp the provider; default is {@link LegacyPrivateKeyPasswordProvider} which retrieves the private key password from
   *          'security.properties' on the classpath to support backward compatibility.
   */
  public void setPrivateKeyPasswordProvider(PrivateKeyPasswordProvider pkpp) {
    privateKeyPasswordProvider = pkpp;
  }

}
