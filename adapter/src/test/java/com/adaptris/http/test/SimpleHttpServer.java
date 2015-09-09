package com.adaptris.http.test;

import static com.adaptris.core.PortManager.nextUnusedPort;
import static com.adaptris.core.PortManager.release;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.adaptris.http.HttpListener;
import com.adaptris.http.HttpsListener;
import com.adaptris.http.Listener;
import com.adaptris.http.RequestProcessor;
import com.adaptris.security.keystore.KeystoreFactory;
import com.adaptris.security.keystore.KeystoreLocation;
import com.adaptris.security.keystore.KeystoreProxy;
import com.adaptris.security.password.Password;

/**
 * A simple Socket server
 * <p>
 * It just reads in some bytes, and echos them to the log and writes back a
 * string to the remote client
 * <p>
 * The configuration file is a file called "http-test.properties" somewhere in
 * the classpath.
 * <p>
 * The server will stop if a file called "http-test.stop" is found in the
 * current working directory.
 */
public class SimpleHttpServer {

  private boolean serverStarted;
  private static Log logR = LogFactory.getLog(SimpleHttpServer.class);
  private Properties config;
  private HttpListener http = null;
  private HttpsListener https = null;
  private List processorList;
  private int httpPort;
  private int httpsPort;

  public SimpleHttpServer() throws Exception {
    InputStream in = SimpleHttpServer.class.getClassLoader().getResourceAsStream(Config.CONFIG_FILE);
    config = new Properties();
    config.load(in);
    in.close();
    initProcessors();
  }

  public SimpleHttpServer(Properties p) throws Exception {
    config = p;
    initProcessors();
  }

  public int getHttpPort() {
    return httpPort;
  }

  public int getHttpsPort() {
    return httpsPort;
  }

  synchronized void startServer() throws Exception {
    if (serverStarted) {
      return;
    }
    initialise();
    if (http != null) {
      http.start();
    }
    if (https != null) {
      https.start();
    }
    serverStarted = true;
  }

  synchronized void stopServer() throws Exception {
    if (serverStarted) {
      if (https != null) {
        https.stop();
        release(httpsPort);
      }
      if (http != null) {
        http.stop();
        release(httpPort);
      }
      serverStarted = false;
    }
  }

  private void initProcessors() throws Exception {
    processorList = new ArrayList();
    Properties urlList = Config.getPropertySubset(Config.HTTP_SERVER_LISTENER, config);
    Iterator i = urlList.keySet().iterator();

    while (i.hasNext()) {
      String key = i.next().toString();
      ProcessorConfig rc = new ProcessorConfig(key);
      if (processorList.contains(rc)) {
        rc = (ProcessorConfig) processorList.get(processorList.indexOf(rc));
      }
      else {
        processorList.add(rc);
      }
      rc.set(key, config.getProperty(key));
    }
  }

  private void initialise() throws Exception {
    httpPort = nextUnusedPort(8080);
    httpsPort = nextUnusedPort(8080);
    http = new HttpListener(httpPort);
    http.setServerSocketTimeout(200);
    https = new HttpsListener(httpsPort);
    https.setServerSocketTimeout(200);
    KeystoreFactory fac = KeystoreFactory.getDefault();
    KeystoreLocation ksl = fac.create(config.getProperty(Config.HTTPS_KEYSTOREURL), Password.decode(
        config.getProperty(Config.HTTPS_KEYSTOREPASSWORD)).toCharArray());
    KeystoreProxy ksp = fac.create(ksl);
    https.registerKeystore(ksp);
    https.setAlwaysTrust(true);
    int threads = Integer.parseInt(config.getProperty(Config.HTTP_SERVER_REQUESTPROCESSORS, "10"));
    addRequestProcessors(https, threads);
    addRequestProcessors(http, threads);
  }

  private void addRequestProcessors(Listener l, int threads) throws Exception {
    for (int i = 0; i < threads; i++) {
      Iterator j = processorList.iterator();
      while (j.hasNext()) {
        ProcessorConfig c = (ProcessorConfig) j.next();
        Class clazz = Class.forName(c.getClassName());
        Constructor constructor = clazz.getDeclaredConstructor(new Class[]
        {
            String.class, Integer.class, Properties.class
        });
        RequestProcessor rp = (RequestProcessor) constructor.newInstance(new Object[]
        {
            c.getUrl(), new Integer(threads), c.getConfig()
        });
        l.addRequestProcessor(rp);
        logR.info("Added " + c.getClassName() + " for " + c.getUrl() + " on " + l);
      }
    }
  }

}