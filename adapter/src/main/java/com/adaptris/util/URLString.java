package com.adaptris.util;

import java.net.URL;

import javax.mail.URLName;

/**
 * A Simple URL parser, that can parse any given URL into it's constituent
 * parts.
 * <p>
 * A URL should be in the form
 * <code> &lt;protocol>://&lt;user:password>@&lt;host:port>/&lt;path>#ref
 *  </code>
 * Example:
 * <ul>
 * <li>smtp://user%40btinternet.com:password@mail.btinternet.com/</li>
 * <li>http://myname:password@localhost:8080/thisURL</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Originally this was a self-built parser, but as URLName does exactly the same
 * thing and is provided as part of the javax.mail package, it makes more sense
 * to use that. As the methods are intrinsically the same, we can simply extend
 * URLName directly.
 * </p>
 * 
 * 
 * @author $Author: lchan $
 */
public class URLString extends URLName {

  /**
   * 
   * @see URLName#URLName(String, String, int, String, String, String)
   */
  public URLString(String protocol, String host, int port, String file,
                   String username, String password) {
    super(protocol, host, port, file, username, password);
  }

  /**
   * 
   * @see URLName#URLName(URL)
   */
  public URLString(URL url) {
    super(url);
  }

  /**
   * 
   * @see URLName#URLName(String)
   */
  public URLString(String url) {
    super(url);
  }
}
