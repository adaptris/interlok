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

package com.adaptris.core.security.access;

import static com.adaptris.core.http.jetty.HttpConsumerTest.JETTY_USER_REALM;
import static com.adaptris.core.security.access.IdentityBuilderTest.PASSWORD;
import static com.adaptris.core.security.access.IdentityBuilderTest.ROLE;
import static com.adaptris.core.security.access.IdentityBuilderTest.USER;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.http.jetty.JettyHashUserRealmVerifier;
import com.adaptris.core.security.SecurityServiceExample;
import com.adaptris.core.security.access.MetadataIdentityBuilderImpl.MetadataSource;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

public class VerifyIdentityServiceTest extends SecurityServiceExample {

  public VerifyIdentityServiceTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {

  }

  @Override
  protected void tearDown() throws Exception {

  }

  public void testDefaultService() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(USER, getName() + ThreadLocalRandom.current().nextInt());
    msg.addMetadata(PASSWORD, getName() + ThreadLocalRandom.current().nextInt());
    msg.addMetadata(ROLE, getName() + ThreadLocalRandom.current().nextInt());
    VerifyIdentityService service = new VerifyIdentityService();
    try {
      execute(service, msg);
      fail();
    }
    catch (ServiceException expected) {

    }
  }

  public void testWithJettyHashRealm() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(USER, "user");
    msg.addMetadata(PASSWORD, "password");
    msg.addMetadata(ROLE, "user");
    JettyHashUserRealmVerifier verifier = new JettyHashUserRealmVerifier(PROPERTIES.getProperty(JETTY_USER_REALM));
    MetadataIdentityBuilder builder = new MetadataIdentityBuilder(MetadataSource.Standard,
        new ArrayList<String>(Arrays.asList(USER, PASSWORD, ROLE)));

    VerifyIdentityService service = new VerifyIdentityService(builder, verifier);
    execute(service, msg);
  }

  public void testMetadataVerification() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(USER, "user");
    msg.addMetadata(PASSWORD, "password");
    msg.addMetadata("dbUser", "user");
    msg.addMetadata("dbPassword", "password");
    MetadataIdentityVerifier verifier = new MetadataIdentityVerifier(new KeyValuePairSet(Arrays.asList(new KeyValuePair[]
    {
        new KeyValuePair(USER, "dbUser"), new KeyValuePair(PASSWORD, "dbPassword")
    })));
    MetadataIdentityBuilder builder = new MetadataIdentityBuilder(MetadataSource.Standard,
        new ArrayList<String>(Arrays.asList(USER, PASSWORD)));

    VerifyIdentityService service = new VerifyIdentityService(builder, verifier);
    execute(service, msg);
  }

  public void testMetadataVerification_Fails() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(USER, "user");
    msg.addMetadata(PASSWORD, "password");
    msg.addMetadata("dbUser", "user");
    msg.addMetadata("dbPassword", "NotThePassword");
    MetadataIdentityVerifier verifier = new MetadataIdentityVerifier(new KeyValuePairSet(Arrays.asList(new KeyValuePair[] {
        new KeyValuePair(USER, "dbUser"), new KeyValuePair(PASSWORD, "dbPassword")
    })));
    MetadataIdentityBuilder builder = new MetadataIdentityBuilder(MetadataSource.Standard,
        new ArrayList<String>(Arrays.asList(USER, PASSWORD)));

    VerifyIdentityService service = new VerifyIdentityService(builder, verifier);
    try {
      execute(service, msg);
      fail();
    }
    catch (ServiceException expected) {

    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    JettyHashUserRealmVerifier verifier = new JettyHashUserRealmVerifier("/path/to/jetty/realm.properties");
    MetadataIdentityBuilder builder = new MetadataIdentityBuilder(MetadataSource.Standard,
        new ArrayList<String>(Arrays.asList(USER, PASSWORD, ROLE)));
    return new VerifyIdentityService(builder, verifier);
  }

}
