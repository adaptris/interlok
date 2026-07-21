package com.adaptris.core.http.jetty.retry;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.Adapter;
import com.adaptris.core.ChannelList;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.StandaloneRequestor;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.core.Workflow;
import com.adaptris.core.http.client.ConfiguredRequestMethodProvider;
import com.adaptris.core.http.client.RequestMethodProvider.RequestMethod;
import com.adaptris.core.http.client.net.StandardHttpProducer;
import com.adaptris.core.http.jetty.EmbeddedConnection;
import com.adaptris.core.http.jetty.EmbeddedJettyHelper;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.core.stubs.StubEventHandler;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.cloud.RemoteBlob;
import com.adaptris.interlok.junit.scaffolding.FailedMessageRetrierCase;
import com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase;

class RetryFromJettyDualStoreSmokeTest extends FailedMessageRetrierCase {

  private static final String USA = "usa";
  private static final String EU = "eu";

  private static final EmbeddedJettyHelper jettyHelper = new EmbeddedJettyHelper();
  private static final IsolatedInMemoryRetryStore primaryStore = new IsolatedInMemoryRetryStore();
  private static final IsolatedInMemoryRetryStore secondaryStore = new IsolatedInMemoryRetryStore();

  @BeforeAll
  static void beforeAll() throws Exception {
    LifecycleHelper.initAndStart(primaryStore);
    LifecycleHelper.initAndStart(secondaryStore);
    jettyHelper.startServer();
  }

  @AfterAll
  static void afterAll() throws Exception {
    LifecycleHelper.stopAndClose(primaryStore);
    LifecycleHelper.stopAndClose(secondaryStore);
    jettyHelper.stopServer();
  }

  @BeforeEach
  void clearStores() {
    primaryStore.clear();
    secondaryStore.clear();
  }

  @Test
  void smokeReportPath() throws Exception {
    RetryFromJettyDualStore retrier = createDualStore();
    try {
      primaryStore.write(messageForWorkflow("usa-report-seed"));
      start(retrier);

      AdaptrisMessage triggerMsg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      StandardHttpProducer http = buildProducer(jettyHelper.buildUrl("/api/failed/" + USA + "/list"));
      http.setMethodProvider(new ConfiguredRequestMethodProvider(RequestMethod.GET));
      ExampleServiceCase.execute(new StandaloneRequestor(http), triggerMsg);

      assertEquals(RetryFromJettyBase.HTTP_OK,
          triggerMsg.getMetadataValue(CoreConstants.HTTP_PRODUCER_RESPONSE_CODE));
      assertFalse(triggerMsg.getContent().isEmpty());
    } finally {
      stop(retrier);
    }
  }

  @Test
  void smokeRetryPath() throws Exception {
    RetryFromJettyDualStore retrier = createDualStore();
    StandardWorkflow workflow = createWorkflow();
    try {
      MockMessageProducer workflowProducer = (MockMessageProducer) workflow.getProducer();
      retrier.addWorkflow(workflow);
      retrier.addWorkflow(createWorkflow());

      AdaptrisMessage baseMsg = messageForWorkflow("retry-payload");
      baseMsg.addMetadata(Workflow.WORKFLOW_ID_KEY, workflow.obtainWorkflowId());
      primaryStore.write(baseMsg);

      start(workflow, retrier);

      AdaptrisMessage triggerMsg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      StandardHttpProducer http = buildProducer(jettyHelper.buildUrl("/api/" + USA + "/retry/" + baseMsg.getUniqueId()));
      ExampleServiceCase.execute(new StandaloneRequestor(http), triggerMsg);

      assertEquals(RetryFromJettyBase.HTTP_ACCEPTED,
          triggerMsg.getMetadataValue(CoreConstants.HTTP_PRODUCER_RESPONSE_CODE));
      await().atMost(Duration.ofSeconds(1)).with().pollInterval(Duration.ofMillis(100))
          .until(workflowProducer::messageCount, greaterThanOrEqualTo(1));
    } finally {
      stop(retrier, workflow);
    }
  }

  @Test
  void smokeDeletePath() throws Exception {
    RetryFromJettyDualStore retrier = createDualStore();
    try {
      AdaptrisMessage baseMsg = messageForWorkflow("eu-delete-seed");
      secondaryStore.write(baseMsg);
      start(retrier);

      AdaptrisMessage triggerMsg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      StandardHttpProducer http = buildProducer(
          jettyHelper.buildUrl("/api/failed/" + EU + "/delete/" + baseMsg.getUniqueId()));
      http.setMethodProvider(new ConfiguredRequestMethodProvider(RequestMethod.DELETE));
      http.setIgnoreServerResponseCode(true);
      ExampleServiceCase.execute(new StandaloneRequestor(http), triggerMsg);

      assertEquals(RetryFromJettyBase.HTTP_OK,
          triggerMsg.getMetadataValue(CoreConstants.HTTP_PRODUCER_RESPONSE_CODE));
    } finally {
      stop(retrier);
    }
  }

  @Test
  void smokeStackTracePath() throws Exception {
    RetryFromJettyDualStore retrier = createDualStore();
    try {
      AdaptrisMessage baseMsg = messageForWorkflow("stacktrace-line-1\nstacktrace-line-2");
      primaryStore.write(baseMsg);
      start(retrier);

      AdaptrisMessage triggerMsg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      StandardHttpProducer http = buildProducer(
          jettyHelper.buildUrl("/api/failed/" + USA + "/stacktrace/" + baseMsg.getUniqueId()));
      http.setMethodProvider(new ConfiguredRequestMethodProvider(RequestMethod.GET));
      http.setIgnoreServerResponseCode(true);
      ExampleServiceCase.execute(new StandaloneRequestor(http), triggerMsg);

      assertEquals(RetryFromJettyBase.HTTP_OK,
          triggerMsg.getMetadataValue(CoreConstants.HTTP_PRODUCER_RESPONSE_CODE));
      assertTrue(triggerMsg.getContent().contains("stacktrace-line-1"));
    } finally {
      stop(retrier);
    }
  }

  @Test
  void smokeUnknownRegionReturnsBadRequest() throws Exception {
    RetryFromJettyDualStore retrier = createDualStore();
    try {
      primaryStore.write(messageForWorkflow("seed-for-fallback-test"));
      start(retrier);

      AdaptrisMessage triggerMsg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      StandardHttpProducer http = buildProducer(
          jettyHelper.buildUrl("/api/failed/unknown-region/list"));
      http.setIgnoreServerResponseCode(true);
      http.setMethodProvider(new ConfiguredRequestMethodProvider(RequestMethod.GET));
      ExampleServiceCase.execute(new StandaloneRequestor(http), triggerMsg);

      assertEquals(RetryFromJettyBase.HTTP_BAD,
          triggerMsg.getMetadataValue(CoreConstants.HTTP_PRODUCER_RESPONSE_CODE));
    } finally {
      stop(retrier);
    }
  }

  private RetryFromJettyDualStore createDualStore() {
    return new RetryFromJettyDualStore().withFirstRetryStore(primaryStore)
        .withSecondRetryStore(secondaryStore)
        .withFirstRetryStoreIdentifier(USA)
        .withSecondRetryStoreIdentifier(EU)
        .withRetryStoreRoutingExpression("%message{route}")
        .withReportBuilder(new ReportBuilder());
  }

  @Override
  protected RetryFromJettyDualStore create() {
    return createDualStore();
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    Adapter result = new Adapter();
    RetryFromJettyDualStore retrier = createDualStore();
    retrier.setConnection(new EmbeddedConnection());
    result.setFailedMessageRetrier(retrier);
    result.setChannelList(new ChannelList());
    result.setEventHandler(new StubEventHandler());
    result.setUniqueId(UUID.randomUUID().toString());
    return result;
  }

  @Override
  protected String createBaseFileName(Object object) {
    return RetryFromJettyDualStore.class.getCanonicalName();
  }

  @Override
  protected RetryFromJettyDualStore createForExamples() {
    return createDualStore();
  }

  private StandardHttpProducer buildProducer(String url) {
    return new StandardHttpProducer().withURL(url);
  }

  private AdaptrisMessage messageForWorkflow(String content) {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(content);
    msg.addMetadata(Workflow.WORKFLOW_ID_KEY, "workflow-default");
    return msg;
  }

  private static class IsolatedInMemoryRetryStore implements RetryStore {

    private final Map<String, AdaptrisMessage> store = new ConcurrentHashMap<>();

    @Override
    public void write(AdaptrisMessage msg) {
      store.put(msg.getUniqueId(), msg);
    }

    @Override
    public AdaptrisMessage buildForRetry(String msgId, Map<String, String> metadata,
        AdaptrisMessageFactory factory) throws InterlokException {
      AdaptrisMessage msg = store.get(msgId);
      if (msg == null) {
        throw new InterlokException(msgId + " not found");
      }
      return msg;
    }

    @Override
    public Map<String, String> getMetadata(String msgId) throws InterlokException {
      AdaptrisMessage msg = store.get(msgId);
      if (msg == null) {
        throw new InterlokException(msgId + " not found");
      }
      return new HashMap<>(msg.getMessageHeaders());
    }

    @Override
    public boolean delete(String msgId) {
      return store.remove(msgId) != null;
    }

    @Override
    public Iterable<RemoteBlob> report(boolean includeErrorMessage) {
      List<RemoteBlob> blobs = new ArrayList<>();
      for (Map.Entry<String, AdaptrisMessage> entry : store.entrySet()) {
        blobs.add(new RemoteBlob.Builder()
            .setBucket("smoke")
            .setLastModified(System.currentTimeMillis())
            .setName(entry.getKey())
            .setSize(entry.getValue().getSize())
            .build());
      }
      return blobs;
    }

    @Override
    public void acknowledge(String acknowledgeId) {
      // no-op for smoke testing
    }

    @Override
    public void deleteAcknowledged() {
      // no-op for smoke testing
    }

    @Override
    public void updateRetryCount(String messageId) {
      // no-op for smoke testing
    }

    @Override
    public void makeConnection(AdaptrisConnection connection) {
      // no-op for smoke testing
    }

    @Override
    public String getStackTrace(String msgId) throws InterlokException {
      AdaptrisMessage msg = store.get(msgId);
      if (msg == null) {
        throw new InterlokException("Stack trace not found for message ID: " + msgId);
      }
      return msg.getContent();
    }

    public void clear() {
      store.clear();
    }
  }
}


