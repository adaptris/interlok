package com.adaptris.core.http.jetty.retry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import com.adaptris.core.http.jetty.JettyMessageConsumer;

class RetryFromJettyBaseExpressionTest {

  private static final String RETRY_ENDPOINT = "/resolved/retry/";
  private static final String REPORTING_ENDPOINT = "/resolved/report";
  private static final String DELETE_ENDPOINT = "/resolved/delete/";
  private static final String STACKTRACE_ENDPOINT = "/resolved/stacktrace/";

  private static final String RETRY_PROPERTY = "interlok.retry.base.retryEndpointPrefix";
  private static final String REPORTING_PROPERTY = "interlok.retry.base.reportingEndpoint";
  private static final String DELETE_PROPERTY = "interlok.retry.base.deleteEndpointPrefix";
  private static final String STACKTRACE_PROPERTY = "interlok.retry.base.stackTraceEndpointPrefix";

  @Test
  void endpointExpressionsResolveForRetryFromJetty() throws Exception {
    RetryFromJetty retrier = new RetryFromJetty()
        .withRetryStore(new InMemoryRetryStore())
        .withReportBuilder(new ReportBuilder());

    assertEndpointExpressionResolution(retrier);
    assertSingleStorePrepareMappings(retrier);
  }

  @Test
  void endpointExpressionsResolveForRetryFromJettyDualStore() throws Exception {
    RetryFromJettyDualStore retrier = new RetryFromJettyDualStore()
        .withFirstRetryStore(new InMemoryRetryStore())
        .withSecondRetryStore(new InMemoryRetryStore())
        .withFirstRetryStoreIdentifier("usa")
        .withSecondRetryStoreIdentifier("eu")
        .withRetryStoreRoutingExpression("%message{route}")
        .withReportBuilder(new ReportBuilder());

    assertEndpointExpressionResolution(retrier);
    assertDualStorePrepareMappings(retrier);
  }

  private void assertEndpointExpressionResolution(RetryFromJettyBase retrier) {
    System.setProperty(RETRY_PROPERTY, RETRY_ENDPOINT);
    System.setProperty(REPORTING_PROPERTY, REPORTING_ENDPOINT);
    System.setProperty(DELETE_PROPERTY, DELETE_ENDPOINT);
    System.setProperty(STACKTRACE_PROPERTY, STACKTRACE_ENDPOINT);

    retrier.setRetryEndpointPrefix("%sysprop{" + RETRY_PROPERTY + "}");
    retrier.setReportingEndpoint("%sysprop{" + REPORTING_PROPERTY + "}");
    retrier.setDeleteEndpointPrefix("%sysprop{" + DELETE_PROPERTY + "}");
    retrier.setStackTraceEndpointPrefix("%sysprop{" + STACKTRACE_PROPERTY + "}");

    assertEquals(RETRY_ENDPOINT, retrier.retryEndpointPrefix());
    assertEquals(REPORTING_ENDPOINT, retrier.reportingEndpoint());
    assertEquals(DELETE_ENDPOINT, retrier.deleteEndpointPrefix());
    assertEquals(STACKTRACE_ENDPOINT, retrier.stackTraceEndpointPrefix());
  }

  private void assertSingleStorePrepareMappings(RetryFromJetty retrier) throws Exception {
    try {
      retrier.prepare();

      assertEquals(RETRY_ENDPOINT + "*", ((JettyMessageConsumer) retrier.retrying.getConsumer()).getPath());
      assertEquals(REPORTING_ENDPOINT, ((JettyMessageConsumer) retrier.reporting.getConsumer()).getPath());
      assertEquals(DELETE_ENDPOINT + "*", ((JettyMessageConsumer) retrier.deleting.getConsumer()).getPath());
      assertEquals(STACKTRACE_ENDPOINT + "*", ((JettyMessageConsumer) retrier.gettingStacktrace.getConsumer()).getPath());

      assertEquals("^" + RETRY_ENDPOINT + "(.*)", retrier.retryRouting.getUrlPattern());
      assertEquals("^" + DELETE_ENDPOINT + "(.*)", retrier.deleteRouting.getUrlPattern());
      assertEquals("^" + STACKTRACE_ENDPOINT + "(.*)", retrier.stackTraceRouting.getUrlPattern());
    } finally {
      clearProperty(RETRY_PROPERTY);
      clearProperty(REPORTING_PROPERTY);
      clearProperty(DELETE_PROPERTY);
      clearProperty(STACKTRACE_PROPERTY);
      retrier.close();
    }
  }

  private void assertDualStorePrepareMappings(RetryFromJettyDualStore retrier) throws Exception {
    try {
      retrier.prepare();

      assertEquals("/api/*", ((JettyMessageConsumer) retrier.reporting.getConsumer()).getPath());
      assertNull(retrier.retrying);
      assertNull(retrier.deleting);
      assertNull(retrier.gettingStacktrace);

      assertEquals("^/api/([^/]+)/retry/(.*)", retrier.retryRouting.getUrlPattern());
      assertEquals("^/api/failed/([^/]+)/delete/(.*)", retrier.deleteRouting.getUrlPattern());
      assertEquals("^/api/failed/([^/]+)/stacktrace/(.*)", retrier.stackTraceRouting.getUrlPattern());
    } finally {
      clearProperty(RETRY_PROPERTY);
      clearProperty(REPORTING_PROPERTY);
      clearProperty(DELETE_PROPERTY);
      clearProperty(STACKTRACE_PROPERTY);
      retrier.close();
    }
  }

  private void clearProperty(String key) {
    System.clearProperty(key);
  }
}



