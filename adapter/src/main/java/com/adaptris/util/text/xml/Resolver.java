package com.adaptris.util.text.xml;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.adaptris.util.text.Conversion;

/**
 * @author Stuart Ellidge
 *
 * Class which implements EntityResolver and URIResolver. It caches in memory
 * previously retrieved documents.
 */
public class Resolver implements EntityResolver, URIResolver {

  private HashMap hm = new HashMap();

  private void cache(URL url) throws Exception {
    String key = url.toExternalForm();

    if (!hm.containsKey(key)) {
      String useProxy = System.getProperty("proxySet", "false");
      String proxyUser = System.getProperty("proxyUser");
      String proxyPass = System.getProperty("proxyPass");

      URLConnection urlConn = url.openConnection();

      if ("true".equals(useProxy)) {
        if (proxyUser != null) {
          String authString = proxyUser + ":" + proxyPass;
          String auth = "Basic "
              + Conversion.byteArrayToBase64String(authString.getBytes());
          urlConn.setRequestProperty("Proxy-Authorization", auth);
        }
      }

      InputStream inputStream = urlConn.getInputStream();

      StringBuffer sb = new StringBuffer();
      String buffer = null;
      BufferedReader inBuf = new BufferedReader(new InputStreamReader(
          inputStream));

      while ((buffer = inBuf.readLine()) != null) {
        sb.append(buffer);
      }

      hm.put(key, sb);
    }
  }

  /**
   * @see EntityResolver#resolveEntity(String, String)
   */
  @Override
  public InputSource resolveEntity(String publicId, String systemId)
      throws SAXException {
    //System.out.println("PUBLIC=" + publicId + ", SYSTEM=" + systemId);
    try {

      URL myUrl = new URL(systemId); // throws MalformedURLException
      cache(myUrl);

      StringBuffer tmp = (StringBuffer) hm.get(myUrl.toExternalForm());
      InputSource ret = new InputSource(new StringReader(tmp.toString()));
      ret.setPublicId(publicId);
      ret.setSystemId(systemId);

      return ret;
    }
    catch (Exception e) {
      throw new SAXException("Failed to resolveEntity publicId=" + publicId
          + ", systemId=" + systemId, e);
    }
  }

  /**
   * @see URIResolver#resolve(java.lang.String, java.lang.String)
   */
  @Override
  public Source resolve(String href, String base) throws TransformerException {
    //System.out.println("HREF=" + href + ", BASE=" + base);
    try {

      URL myUrl = null;

      try {
        myUrl = new URL(href);
      }
      catch (Exception ex) {
        // Indicates that the URL was probably relative and therefore Malformed
        int end = base.lastIndexOf('/');
        String url = base.substring(0, end + 1);
        myUrl = new URL(url + href);
      }

      cache(myUrl);

      StringBuffer tmp = (StringBuffer) hm.get(myUrl.toExternalForm());
      StreamSource ret = new StreamSource(new StringReader(tmp.toString()),
          myUrl.toExternalForm());

      return ret;
    }
    catch (Exception e) {
      throw new TransformerException("Failed to resolve href=" + href
          + ", base=" + base, e);
    }
  }

}
