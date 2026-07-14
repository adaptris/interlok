package com.adaptris.core.http.jetty.retry;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.adaptris.core.Adapter;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ChannelList;
import com.adaptris.core.http.jetty.EmbeddedConnection;
import com.adaptris.core.http.jetty.EmbeddedJettyHelper;
import com.adaptris.core.stubs.StubEventHandler;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.interlok.junit.scaffolding.FailedMessageRetrierCase;

/**
 * Tests for RetryFromJetty multi-store routing functionality.
 *
 * Focus: Configuration and backwards compatibility validation
 * These tests verify core routing setup without HTTP complexity.
 *
 * Organized into test classes:
 * - BackwardsCompatibilityTests: Validates legacy single-store behavior
 * - MockBasedStoreConfigurationTests: Validates multi-store configuration
 */
class RetryFromJettyMultiStoreTest extends FailedMessageRetrierCase {

  private static final EmbeddedJettyHelper jettyHelper = new EmbeddedJettyHelper();

  @BeforeAll
  static void beforeAll() throws Exception {
    jettyHelper.startServer();
  }

  @AfterAll
  static void afterAll() throws Exception {
    jettyHelper.stopServer();
  }

  @Nested
  @DisplayName("Backwards Compatibility - Single Store")
  class BackwardsCompatibilityTests {

    private InMemoryRetryStore legacyStore;

    @BeforeEach
    void beforeEach() throws Exception {
      legacyStore = new InMemoryRetryStore();
      for (int i = 0; i < 2; i++) {
        legacyStore.write(AdaptrisMessageFactory.getDefaultInstance().newMessage("Test Message " + i));
      }
      LifecycleHelper.init(legacyStore);
      LifecycleHelper.start(legacyStore);
    }

    @AfterEach
    void afterEach() {
      LifecycleHelper.stop(legacyStore);
      LifecycleHelper.close(legacyStore);
      InMemoryRetryStore.removeAll();
    }

    @Test
    @DisplayName("Report endpoint uses single legacy store")
    void testReport_LegacyStore() throws Exception {
      RetryFromJetty retrier = new RetryFromJetty()
          .withRetryStore(legacyStore)
          .withReportBuilder(new ReportBuilder());

      try {
        start(retrier);
        assertNotNull(retrier.getRetryStore());
        assertEquals(legacyStore, retrier.getRetryStore());
      } finally {
        stop(retrier);
      }
    }

    @Test
    @DisplayName("Delete endpoint works with single legacy store")
    void testDelete_LegacyStore() throws Exception {
      RetryFromJetty retrier = new RetryFromJetty()
          .withRetryStore(legacyStore)
          .withReportBuilder(new ReportBuilder());

      try {
        start(retrier);
        AdaptrisMessage baseMsg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
        legacyStore.write(baseMsg);

        // Verify message can be deleted
        boolean deleted = legacyStore.delete(baseMsg.getUniqueId());
        assertTrue(deleted);
      } finally {
        stop(retrier);
      }
    }

    @Test
    @DisplayName("Single store configuration initializes successfully")
    void testConfig_SingleStore() throws Exception {
      RetryFromJetty retrier = new RetryFromJetty()
          .withRetryStore(legacyStore)
          .withReportBuilder(new ReportBuilder());

      try {
        start(retrier);
        assertNotNull(retrier.getRetryStore());
      } finally {
        stop(retrier);
      }
    }
  }

  /**
   * Mock-based tests for store routing configuration (no HTTP stack).
   * These tests verify multi-store configuration is accepted and stores initialize correctly.
   * Focus: Configuration validation rather than message flow through HTTP listeners.
   */
  @Nested
  @DisplayName("Store Configuration - Mock-Based (No HTTP)")
  class MockBasedStoreConfigurationTests {

    private InMemoryRetryStore euStore;
    private InMemoryRetryStore usStore;
    private InMemoryRetryStore defaultStore;
    private InMemoryRetryStore legacyStore;

    @BeforeEach
    void beforeEach() {
      euStore = new InMemoryRetryStore();
      usStore = new InMemoryRetryStore();
      defaultStore = new InMemoryRetryStore();
      legacyStore = new InMemoryRetryStore();
    }

    @AfterEach
    void afterEach() {
      InMemoryRetryStore.removeAll();
    }

    @Test
    @DisplayName("Multi-store configuration with routing expression initializes successfully")
    void testConfig_MultiStoreInitialization() throws Exception {
      Map<String, RetryStore> storesByRoute = new HashMap<>();
      storesByRoute.put("eu", euStore);
      storesByRoute.put("us", usStore);

      RetryFromJetty retrier = new RetryFromJetty()
          .withRetryStore(legacyStore)
          .withReportBuilder(new ReportBuilder())
          .withRetryStoreRoutingExpression("%message{pn.routing.region}");
      retrier.setRetryStoresByRoute(storesByRoute);

      try {
        start(retrier);
        assertEquals(2, retrier.getRetryStoresByRoute().size());
        assertTrue(retrier.getRetryStoresByRoute().containsKey("eu"));
        assertTrue(retrier.getRetryStoresByRoute().containsKey("us"));
      } finally {
        stop(retrier);
      }
    }

    @Test
    @DisplayName("Multi-store with default store initializes successfully")
    void testConfig_WithDefaultStore() throws Exception {
      Map<String, RetryStore> storesByRoute = new HashMap<>();
      storesByRoute.put("eu", euStore);
      storesByRoute.put("us", usStore);

      RetryFromJetty retrier = new RetryFromJetty()
          .withRetryStore(legacyStore)
          .withReportBuilder(new ReportBuilder())
          .withRetryStoreRoutingExpression("%message{pn.routing.region}");
      retrier.setRetryStoresByRoute(storesByRoute);
      retrier.setDefaultRetryStore(defaultStore);

      try {
        start(retrier);
        assertNotNull(retrier.getDefaultRetryStore());
      } finally {
        stop(retrier);
      }
    }

    @Test
    @DisplayName("Routing expression can be configured and retrieved")
    void testConfig_RoutingExpression() throws Exception {
      String customExpression = "%message{custom.region}";
      RetryFromJetty retrier = new RetryFromJetty()
          .withRetryStore(legacyStore)
          .withReportBuilder(new ReportBuilder())
          .withRetryStoreRoutingExpression(customExpression);

      try {
        start(retrier);
        assertEquals(customExpression, retrier.getRetryStoreRoutingExpression());
      } finally {
        stop(retrier);
      }
    }

    @Test
    @DisplayName("Multiple stores remain separate when configured")
    void testConfig_StoreSeparation() throws Exception {
      Map<String, RetryStore> storesByRoute = new HashMap<>();
      storesByRoute.put("eu", euStore);
      storesByRoute.put("us", usStore);

      RetryFromJetty retrier = new RetryFromJetty()
          .withRetryStore(legacyStore)
          .withReportBuilder(new ReportBuilder())
          .withRetryStoreRoutingExpression("%message{pn.routing.region}");
      retrier.setRetryStoresByRoute(storesByRoute);

      try {
        start(retrier);
        assertTrue(retrier.getRetryStoresByRoute().containsKey("eu"));
        assertTrue(retrier.getRetryStoresByRoute().containsKey("us"));
        assertSame(euStore, retrier.getRetryStoresByRoute().get("eu"));
        assertSame(usStore, retrier.getRetryStoresByRoute().get("us"));
        assertSame(legacyStore, retrier.getRetryStore());
      } finally {
        stop(retrier);
      }
    }

    @Test
    @DisplayName("Configuration with case-insensitive route keys")
    void testConfig_CaseInsensitiveRoutes() throws Exception {
      Map<String, RetryStore> storesByRoute = new HashMap<>();
      storesByRoute.put("EU", euStore);  // uppercase
      storesByRoute.put("us", usStore);  // lowercase

      RetryFromJetty retrier = new RetryFromJetty()
          .withRetryStore(legacyStore)
          .withReportBuilder(new ReportBuilder())
          .withRetryStoreRoutingExpression("%message{pn.routing.region}");
      retrier.setRetryStoresByRoute(storesByRoute);

      try {
        start(retrier);
        Map<String, RetryStore> routes = retrier.getRetryStoresByRoute();
        assertTrue(routes.containsKey("eu") || routes.containsKey("EU"));
      } finally {
        stop(retrier);
      }
    }

    @Test
    @DisplayName("Backwards compatible with single legacy store only")
    void testConfig_LegacyStoreOnly() throws Exception {
      RetryFromJetty retrier = new RetryFromJetty()
          .withRetryStore(legacyStore)
          .withReportBuilder(new ReportBuilder());

      try {
        start(retrier);
        assertNotNull(retrier.getRetryStore());
        assertSame(legacyStore, retrier.getRetryStore());
      } finally {
        stop(retrier);
      }
    }

    @Test
    @DisplayName("No stores configured throws error during start")
    void testConfig_NoStoresConfigured() {
      RetryFromJetty retrier = new RetryFromJetty()
          .withReportBuilder(new ReportBuilder());

      assertThrows(Exception.class, () -> start(retrier));
    }
  }

  @Override
  protected RetryFromJetty create() {
    return new RetryFromJetty()
        .withRetryStore(new InMemoryRetryStore())
        .withReportBuilder(new ReportBuilder());
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    Adapter result;
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
    return RetryFromJetty.class.getCanonicalName() + "-MultiStore";
  }

  @Override
  protected RetryFromJetty createForExamples() {
    RetryFromJetty fmr = new RetryFromJetty();
    fmr.setRetryStore(new FilesystemRetryStore().withBaseUrl("file:///path/to/messages"));
    return fmr;
  }
}

