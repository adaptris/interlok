package com.adaptris.core.http.jetty.retry;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import com.adaptris.core.Adapter;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ChannelList;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.NullService;
import com.adaptris.core.StandaloneRequestor;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.core.Workflow;
import com.adaptris.core.http.client.ConfiguredRequestMethodProvider;
import com.adaptris.core.http.client.RequestMethodProvider;
import com.adaptris.core.http.client.net.HttpRequestService;
import com.adaptris.core.http.client.net.StandardHttpProducer;
import com.adaptris.core.http.jetty.EmbeddedConnection;
import com.adaptris.core.http.jetty.EmbeddedJettyHelper;
import com.adaptris.core.services.exception.ConfiguredException;
import com.adaptris.core.services.exception.ThrowExceptionService;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.core.stubs.StubEventHandler;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.cloud.RemoteBlob;
import com.adaptris.interlok.junit.scaffolding.FailedMessageRetrierCase;
import com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase;

public class RetryFromJettyTest extends FailedMessageRetrierCase {

  private static EmbeddedJettyHelper jettyHelper = new EmbeddedJettyHelper();
  private static InMemoryRetryStore retryStore = new InMemoryRetryStore();

  @BeforeClass
  public static void beforeAll() throws Exception {
    LifecycleHelper.initAndStart(retryStore);
    // Seed some messages into the retry store which has a
    // static map.
    for (int i = 0; i < 2; i++) {
      retryStore.write(AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello World"));
    }
    jettyHelper.startServer();
  }

  @AfterClass
  public static void afterAll() throws Exception {
    LifecycleHelper.stopAndClose(retryStore);
    InMemoryRetryStore.removeAll();
    jettyHelper.stopServer();
  }


  @Test
  public void testReport() throws Exception {
    RetryFromJetty retrier = create();
    try {
      start(retrier);
      String url = jettyHelper.buildUrl(RetryFromJetty.DEFAULT_REPORTING_ENDPOINT);
      HttpRequestService http = new HttpRequestService(url);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      ExampleServiceCase.execute(http, msg);
      assertEquals(RetryFromJetty.HTTP_OK,
          msg.getMetadataValue(CoreConstants.HTTP_PRODUCER_RESPONSE_CODE));
      // We know there should be at least 2 messages seeded...
      try (InputStream in = msg.getInputStream()) {
        List lines = IOUtils.readLines(in, StandardCharsets.UTF_8);
        assertTrue(lines.size() >= 2);
      }
    } finally {
      stop(retrier);
    }
  }

  @Test
  public void testReport_Broken() throws Exception {
    RetryFromJetty retrier = create();
    try {
      retrier.setRetryStore(new BrokenRetryStore());
      start(retrier);
      String url = jettyHelper.buildUrl(RetryFromJetty.DEFAULT_REPORTING_ENDPOINT);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();

      StandardHttpProducer http = buildProducer(url);
      http.setIgnoreServerResponseCode(true);
      http.setMethodProvider(
          new ConfiguredRequestMethodProvider(RequestMethodProvider.RequestMethod.GET));

      ExampleServiceCase.execute(new StandaloneRequestor(http), msg);
      assertEquals(RetryFromJetty.HTTP_ERROR,
          msg.getMetadataValue(CoreConstants.HTTP_PRODUCER_RESPONSE_CODE));
    } finally {
      stop(retrier);
    }
  }

  @Test
  public void testRetry() throws Exception {
    RetryFromJetty retrier = create();
    StandardWorkflow workflow = createWorkflow();
    try {
      MockMessageProducer workflowProducer = (MockMessageProducer) workflow.getProducer();
      retrier.addWorkflow(workflow);
      retrier.addWorkflow(createWorkflow());
      start(workflow, retrier);
      AdaptrisMessage baseMsg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      baseMsg.addMetadata(Workflow.WORKFLOW_ID_KEY, workflow.obtainWorkflowId());
      retryStore.write(baseMsg);
      assertNotNull(retryStore.getMetadata(baseMsg.getUniqueId()));

      String url =
          jettyHelper.buildUrl(RetryFromJetty.DEFAULT_ENDPOINT_PREFIX + baseMsg.getUniqueId());
      StandardHttpProducer http = buildProducer(url);
      AdaptrisMessage triggerMsg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      ExampleServiceCase.execute(new StandaloneRequestor(http), triggerMsg);
      assertEquals(RetryFromJetty.HTTP_ACCEPTED,
          triggerMsg.getMetadataValue(CoreConstants.HTTP_PRODUCER_RESPONSE_CODE));

      // This should trigger the workflow, so we should wait for the message
      await().atMost(Duration.ofSeconds(1)).with().pollInterval(Duration.ofMillis(100))
          .until(workflowProducer::messageCount, greaterThanOrEqualTo(1));
      assertEquals(1, workflowProducer.messageCount());

    } finally {
      stop(retrier, workflow);
    }
  }

  @Test
  public void testRetry_WrongMethod() throws Exception {
    RetryFromJetty retrier = create();
    StandardWorkflow workflow = createWorkflow();
    try {
      MockMessageProducer workflowProducer = (MockMessageProducer) workflow.getProducer();
      retrier.addWorkflow(workflow);
      retrier.addWorkflow(createWorkflow());
      start(workflow, retrier);
      AdaptrisMessage baseMsg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      baseMsg.addMetadata(Workflow.WORKFLOW_ID_KEY, workflow.obtainWorkflowId());
      retryStore.write(baseMsg);
      assertNotNull(retryStore.getMetadata(baseMsg.getUniqueId()));

      // This should result in a route condition that doesn't match POST + the msgId so 400 is
      // expected
      String url =
          jettyHelper.buildUrl(RetryFromJetty.DEFAULT_ENDPOINT_PREFIX + baseMsg.getUniqueId());
      StandardHttpProducer http = buildProducer(url);
      http.setIgnoreServerResponseCode(true);
      http.setMethodProvider(
          new ConfiguredRequestMethodProvider(RequestMethodProvider.RequestMethod.GET));


      AdaptrisMessage triggerMsg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      ExampleServiceCase.execute(new StandaloneRequestor(http), triggerMsg);
      assertEquals(RetryFromJetty.HTTP_BAD,
          triggerMsg.getMetadataValue(CoreConstants.HTTP_PRODUCER_RESPONSE_CODE));

    } finally {
      stop(retrier, workflow);
    }
  }

  @Test
  public void testRetry_NotFound() throws Exception {
    RetryFromJetty retrier = create();
    StandardWorkflow workflow = createWorkflow();
    try {
      MockMessageProducer workflowProducer = (MockMessageProducer) workflow.getProducer();
      retrier.addWorkflow(workflow);
      retrier.addWorkflow(createWorkflow());
      start(workflow, retrier);
      AdaptrisMessage baseMsg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      baseMsg.addMetadata(Workflow.WORKFLOW_ID_KEY, workflow.obtainWorkflowId());
      retryStore.write(baseMsg);
      assertNotNull(retryStore.getMetadata(baseMsg.getUniqueId()));

      // This should result in a msgId that isn't found; so we get a 500...
      AdaptrisMessage triggerMsg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      String url =
          jettyHelper.buildUrl(RetryFromJetty.DEFAULT_ENDPOINT_PREFIX + triggerMsg.getUniqueId());
      StandardHttpProducer http = buildProducer(url);
      http.setIgnoreServerResponseCode(true);
      ExampleServiceCase.execute(new StandaloneRequestor(http), triggerMsg);
      assertEquals(RetryFromJetty.HTTP_ERROR,
          triggerMsg.getMetadataValue(CoreConstants.HTTP_PRODUCER_RESPONSE_CODE));

    } finally {
      stop(retrier, workflow);
    }
  }

  @Test
  public void testExecuteQuietly() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    NullService s1 = new NullService();
    ThrowExceptionService s2 = new ThrowExceptionService(new ConfiguredException("failure"));
    try {
      start(s1, s2);
      RetryFromJetty.executeQuietly(s1, msg);
      RetryFromJetty.executeQuietly(s2, msg);
    } finally {
      stop(s1, s2);
    }
  }

  private StandardHttpProducer buildProducer(String url) {
    StandardHttpProducer producer = new StandardHttpProducer().withURL(url);
    return producer;
  }


  @Override
  protected RetryFromJetty create() {
    return new RetryFromJetty().withRetryStore(new InMemoryRetryStore())
        .withReportBuilder(new ReportBuilder());
  }


  @Override
  protected Object retrieveObjectForSampleConfig() {
    Adapter result = null;
    try {
      RetryFromJetty fmr = new RetryFromJetty();
      fmr.setConnection(new EmbeddedConnection());
      fmr.setRetryStore(new InMemoryRetryStore());
      result = new Adapter();
      result.setFailedMessageRetrier(fmr);
      result.setChannelList(new ChannelList());
      result.setEventHandler(new StubEventHandler());
      result.setUniqueId(UUID.randomUUID().toString());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return result;
  }

  @Override
  protected String createBaseFileName(Object object) {
    return RetryFromJetty.class.getCanonicalName();
  }

  @Override
  protected RetryFromJetty createForExamples() {
    RetryFromJetty fmr = new RetryFromJetty();
    fmr.setRetryStore(new FilesystemRetryStore().withBaseUrl("file:///path/to/messages"));
    return fmr;
  }

  private class BrokenRetryStore implements RetryStore {

    @Override
    public void write(AdaptrisMessage msg) throws InterlokException {
      throw new UnsupportedOperationException();
    }

    @Override
    public AdaptrisMessage buildForRetry(String msgId, Map<String, String> metadata,
        AdaptrisMessageFactory factory) throws InterlokException {
      throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String> getMetadata(String msgId) throws InterlokException {
      throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<RemoteBlob> report() throws InterlokException {
      throw new UnsupportedOperationException();
    }
  }

}
