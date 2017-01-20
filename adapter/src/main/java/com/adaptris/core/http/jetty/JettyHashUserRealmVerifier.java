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
package com.adaptris.core.http.jetty;

import static org.apache.commons.lang.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.jetty.util.security.Credential;
import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.security.access.IdentityBuilder;
import com.adaptris.core.security.access.IdentityVerifier;
import com.adaptris.core.security.access.IdentityVerifierImpl;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link IdentityVerifier} implementation that uses the same file as {@link HashLoginServiceFactory} to perform identity
 * verification.
 * 
 * @config jetty-hash-user-realm-identity-verifier
 */
@XStreamAlias("jetty-hash-user-realm-identity-verifier")
public class JettyHashUserRealmVerifier extends IdentityVerifierImpl {

  /**
   * The key in the identity map that contains the username {@value #KEY_USERNAME}.
   * 
   */
  public static final String KEY_USERNAME = "user";
  /**
   * The key in the identity map that contains the username {@value #KEY_PASSWORD}.
   * 
   */
  public static final String KEY_PASSWORD = "password";
  /**
   * The key in the identity map that contains the role {@value #KEY_ROLE}.
   * 
   */
  public static final String KEY_ROLE = "role";

  @NotBlank
  private String filename;

  private transient long fileLastModified = -1;
  private transient Map<String, AccessCredentials> users;

  public JettyHashUserRealmVerifier() {

  }

  public JettyHashUserRealmVerifier(String filename) {
    this();
    setFilename(filename);
  }

  @Override
  public void init() throws CoreException {
    if (isEmpty(filename)) {
      throw new CoreException("Empty Filename");
    }
  }

  @Override
  public boolean validate(IdentityBuilder builder, AdaptrisMessage msg) {
    boolean result = false;
    try {
      Map<String, Object> identity = builder.build(msg);
      
      Map<String, AccessCredentials> myUsers = loadUsers();
      String username = (String) identity.get(KEY_USERNAME);
      if (myUsers.containsKey(username)) {
        result = myUsers.get(username).validate((String) identity.get(KEY_PASSWORD), (String) identity.get(KEY_ROLE));
      }
    }
    catch (Exception e) {
      result = false;
    }
    return result;
  }

  /**
   * @return the filename
   */
  public String getFilename() {
    return filename;
  }

  /**
   * @param filename the filename to set
   */
  public void setFilename(String filename) {
    this.filename = Args.notBlank(filename, "filename");
  }

  private Map<String, AccessCredentials> loadUsers() throws IOException {
    File file = new File(filename);
    if (fileLastModified < file.lastModified()) {
      Properties p = new Properties();
      try (InputStream in = new FileInputStream(file)) {
        p.load(in);
      }
      users = loadUsers(p);
      fileLastModified = file.lastModified();
    }
    return users;
  }

  private Map<String, AccessCredentials> loadUsers(Properties properties) throws IOException {
    Map<String, AccessCredentials> result = new HashMap<>();
    for (Map.Entry<Object, Object> entry : properties.entrySet()) {
      String username = ((String) entry.getKey()).trim();
      String credentials = ((String) entry.getValue()).trim();
      String roles = null;
      int c = credentials.indexOf(',');
      if (c >= 0 && credentials.length() > 1) {
        roles = credentials.substring(c + 1).trim();
        credentials = credentials.substring(0, c).trim();
      }
      result.put(username, new AccessCredentials(credentials, roles));
    }
    return result;
  }

  private class AccessCredentials {
    private Credential credentials;
    private List<String> roles;

    AccessCredentials(String creds, String roles) {
      credentials = Credential.getCredential(creds);
      this.roles = roles != null ? Arrays.asList(roles.split(",")) : new ArrayList<String>();
    }

    boolean validate(String pw, String role) {
      return validatePassword(pw) && hasRole(role);
    }

    private boolean validatePassword(String pw) {
      return credentials.check(pw);
    }

    private boolean hasRole(String role) {
      if (roles.size() > 0) {
        return roles.contains(defaultIfEmpty(role, ""));
      }
      // no roles assigned to this user, so all roles valid.
      return true;
    }
  }
}
