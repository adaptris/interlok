package com.adaptris.core.http.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.net.Authenticator;
import java.net.Authenticator.RequestorType;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.bouncycastle.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ThreadLocalCredentialsTest {

  private static final String TARGET = "http://localhost";
  
  @Before
  public void setup() {
    ThreadLocalCredentials tlc = ThreadLocalCredentials.getInstance(TARGET);
    AdapterResourceAuthenticator.getInstance().addAuthenticator(ThreadLocalCredentials.getInstance("http://www.adaptris.com"));
    AdapterResourceAuthenticator.getInstance().addAuthenticator(tlc);
    Authenticator.setDefault(AdapterResourceAuthenticator.getInstance());
  }
  
  @After
  public void teardown() {
    ThreadLocalCredentials tlc = ThreadLocalCredentials.getInstance(TARGET);
    tlc.removeThreadCredentials();
    AdapterResourceAuthenticator.getInstance().removeAuthenticator(null);
    AdapterResourceAuthenticator.getInstance().removeAuthenticator(tlc);
    AdapterResourceAuthenticator.getInstance().removeAuthenticator(ThreadLocalCredentials.getInstance("http://www.adaptris.com"));
    Authenticator.setDefault(null);
  }
  
  @Test
  public void mainThreadInitialNull() throws InterruptedException, UnknownHostException, MalformedURLException {
    assertNull("PasswordAuthentication must be null before it is set", requestAuthentication());
  }
  
  @Test
  public void mainThreadWorks() throws InterruptedException, UnknownHostException, MalformedURLException {
    ThreadLocalCredentials.getInstance(TARGET).setThreadCredentials(new PasswordAuthentication("username", "password".toCharArray()));
    
    PasswordAuthentication auth = requestAuthentication();
    assertEquals("username", auth.getUserName());
    assertTrue(Arrays.areEqual("password".toCharArray(), auth.getPassword()));
  }
  
  @Test
  public void secondThreadInitialNull() throws InterruptedException, UnknownHostException, MalformedURLException {
    ThreadLocalCredentials.getInstance(TARGET).setThreadCredentials(new PasswordAuthentication("username", "password".toCharArray()));
    
    // Fire up a new thread and make sure it's null in there
    final AtomicReference<PasswordAuthentication> fromOtherThread = new AtomicReference<>(new PasswordAuthentication("dummy", "dummy".toCharArray()));
    Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          PasswordAuthentication auth = requestAuthentication();
          fromOtherThread.set(auth);
        } catch (Exception e) {}
      }
    });
    t.start();
    t.join();

    assertNull("PasswordAuthentication must be null from other thread before being set", fromOtherThread.get());
    
    PasswordAuthentication auth = requestAuthentication();
    assertEquals("Main thread credentials must still be set", "username", auth.getUserName());
    assertTrue("Main thread credentials must still be set", Arrays.areEqual("password".toCharArray(), auth.getPassword()));
  }
  
  @Test
  public void secondThreadWorks() throws InterruptedException, UnknownHostException, MalformedURLException {
    ThreadLocalCredentials.getInstance(TARGET).setThreadCredentials(new PasswordAuthentication("username", "password".toCharArray()));
    
    final AtomicBoolean t2UsernameOK = new AtomicBoolean(false);
    final AtomicBoolean t2PasswordOK = new AtomicBoolean(false);
    Thread t2 = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          ThreadLocalCredentials.getInstance(TARGET).setThreadCredentials(new PasswordAuthentication("username2", "password2".toCharArray()));
          
          PasswordAuthentication auth = requestAuthentication();
          
          t2UsernameOK.set("username2".equals(auth.getUserName()));
          t2PasswordOK.set(Arrays.areEqual("password2".toCharArray(), auth.getPassword()));
        } catch (Exception e) {}
      }
    });
    t2.start();
    t2.join();
    
    assertTrue("Username in other thread is wrong", t2UsernameOK.get());
    assertTrue("Password in other thread is wrong", t2PasswordOK.get());
    
    PasswordAuthentication auth = requestAuthentication();
    assertEquals("Main thread credentials must still be set", "username", auth.getUserName());
    assertTrue("Main thread credentials must still be set", Arrays.areEqual("password".toCharArray(), auth.getPassword()));
  }
  
  private PasswordAuthentication requestAuthentication() throws UnknownHostException, MalformedURLException {
    return Authenticator.requestPasswordAuthentication("localhost", InetAddress.getLocalHost(), 80, "http", "", "http", new URL("http://localhost"), RequestorType.SERVER);
  }
  
}
