package com.adaptris.core.http.jetty;

import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.LoginService;

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Allows you to configure a {@code HashLoginService} for use with Jetty.
 * <p>
 * This simply exposes the Jetty HashLoginService as Interlok configuration. The documentation from
 * <a href="https://wiki.eclipse.org/Jetty/Tutorial/Realms">the eclipse jetty site</a> should always
 * be considered canonical. The HashLoginService is a realm whose authentication and authorization
 * information is stored in a properties file. Each line in the file contains a username, a
 * password, and zero or more role assignments. The format is
 * 
 * <pre>
 * {@code 
username: password[,rolename ...]
username: password[,rolename ...]
 * }
 * </pre>
 * 
 * where:
 * <ul>
 * <li><strong>username</strong> is the user's unique identity</li>
 * <li><strong>password</strong> is the user's (possibly obfuscated or MD5 encrypted) password;</li>
 * <li><strong>rolename</strong> is the user's role.</li>
 * </ul>
 * <p>
 * If you need to secure the passwords in your properties file then you can follow <a href=
 * "https://www.eclipse.org/jetty/documentation/current/configuring-security-secure-passwords.html">the
 * jetty documentation</a> to obfuscate/hash the password. Note that the {@code CRYPT} style is
 * based on Unix Crypt which is considered weak and insecure (you probably shouldn't be using it).
 * 
 * <pre>
 * {@code
$ java -cp ./lib/jetty-util.jar  org.eclipse.jetty.util.security.Password myusername MySuperSecretPassword
OBF:1o4o1zly1rhf1zst1y0s1vu91uvk1ldu1w1c1sot1y7z1sox1w261ldo1uum1vv11y0y1zsx1riz1zlk1o5y
MD5:d418c3e96fb98e73fd603a8b6134edda
CRYPT:my8hdCDBVkNU.
 * }
 * </pre>
 *
 * @config jetty-hash-login-service
 */
@JacksonXmlRootElement(localName = "jetty-hash-login-service")
@XStreamAlias("jetty-hash-login-service")
@DisplayOrder(order = {"userRealm", "filename"})
@ComponentProfile(summary = "allows use of org.eclipse.jetty.security.HashLoginService to authenticate users",
    tag = "jetty,authentication")
public class HashLoginServiceFactory extends LoginServiceFactoryImpl {

  public HashLoginServiceFactory() {
    super();
  }

  public HashLoginServiceFactory(String realm, String filename) {
    this();
    setUserRealm(realm);
    setFilename(filename);
  }

  @Override
  public LoginService retrieveLoginService() throws Exception {
    HashLoginService loginService = new HashLoginService(getUserRealm(), validateFilename());
    loginService.setHotReload(true);
    return new LoginServiceProxy().withLoginService(loginService);
  }
}
