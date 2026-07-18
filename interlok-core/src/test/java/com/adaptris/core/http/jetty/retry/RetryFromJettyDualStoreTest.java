package com.adaptris.core.http.jetty.retry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.AdaptrisMessageConsumer;
import com.adaptris.core.CoreException;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.http.jetty.JettyConstants;
import com.adaptris.core.StartedState;
import com.adaptris.core.Workflow;
import com.adaptris.interlok.InterlokException;
import com.adaptris.core.util.LifecycleHelper;

class RetryFromJettyDualStoreTest {

  private static final String ROUTE_KEY = "route";

  @Test
  void reportRoutesToPrimaryStore() throws Exception {
    RetryStore primary = mock(RetryStore.class);
    RetryStore secondary = mock(RetryStore.class);
    ReportBuilder reportBuilder = mock(ReportBuilder.class);
    RetryFromJettyDualStore retrier = newDualStore(primary, secondary, reportBuilder);
    prepareForListenerTests(retrier);
    RetryFromJettyBase.RetryJettyListenerImpl listener = retrier.reporter;
    AdaptrisMessage msg = requestMessage("usa", null, null);

    reset(primary, secondary, reportBuilder);
    when(primary.report(true)).thenReturn(Collections.emptyList());

    listener.onAdaptrisMessage(msg, m -> {}, m -> {});

    assertEquals(RetryFromJettyBase.HTTP_OK, msg.getMetadataValue(RetryFromJettyBase.HTTP_STATUS_KEY));
    verify(primary).report(true);
    verifyNoInteractions(secondary);
    verify(reportBuilder).build(any(), same(msg));
  }

  @Test
  void reportRoutesToSecondaryStore() throws Exception {
    RetryStore primary = mock(RetryStore.class);
    RetryStore secondary = mock(RetryStore.class);
    ReportBuilder reportBuilder = mock(ReportBuilder.class);
    RetryFromJettyDualStore retrier = newDualStore(primary, secondary, reportBuilder);
    prepareForListenerTests(retrier);
    RetryFromJettyBase.RetryJettyListenerImpl listener = retrier.reporter;
    AdaptrisMessage msg = requestMessage("eu", null, null);

    reset(primary, secondary, reportBuilder);
    when(secondary.report(true)).thenReturn(Collections.emptyList());

    listener.onAdaptrisMessage(msg, m -> {}, m -> {});

    assertEquals(RetryFromJettyBase.HTTP_OK, msg.getMetadataValue(RetryFromJettyBase.HTTP_STATUS_KEY));
    verify(secondary).report(true);
    verifyNoInteractions(primary);
    verify(reportBuilder).build(any(), same(msg));
  }

  @Test
  void reportRoutesUsingResolvedIdentifierExpressions() throws Exception {
    RetryStore primary = mock(RetryStore.class);
    RetryStore secondary = mock(RetryStore.class);
    ReportBuilder reportBuilder = mock(ReportBuilder.class);
    RetryFromJettyDualStore retrier = new RetryFromJettyDualStore()
        .withFirstRetryStore(primary)
        .withSecondRetryStore(secondary)
        .withFirstRetryStoreIdentifier("%message{primary-store-id}")
        .withSecondRetryStoreIdentifier("%message{secondary-store-id}")
        .withRetryStoreRoutingExpression("%message{" + ROUTE_KEY + "}")
        .withReportBuilder(reportBuilder);
    prepareForListenerTests(retrier);
    RetryFromJettyBase.RetryJettyListenerImpl listener = retrier.reporter;
    AdaptrisMessage msg = requestMessage("usa", null, null);
    msg.addMetadata("primary-store-id", "usa");
    msg.addMetadata("secondary-store-id", "eu");

    reset(primary, secondary, reportBuilder);
    when(primary.report(true)).thenReturn(Collections.emptyList());

    listener.onAdaptrisMessage(msg, m -> {}, m -> {});

    assertEquals(RetryFromJettyBase.HTTP_OK, msg.getMetadataValue(RetryFromJettyBase.HTTP_STATUS_KEY));
    verify(primary).report(true);
    verifyNoInteractions(secondary);
    verify(reportBuilder).build(any(), same(msg));
  }

  @Test
  void reportWithAmbiguousResolvedIdentifiersReturnsBadRequest() throws Exception {
    RetryStore primary = mock(RetryStore.class);
    RetryStore secondary = mock(RetryStore.class);
    ReportBuilder reportBuilder = mock(ReportBuilder.class);
    RetryFromJettyDualStore retrier = new RetryFromJettyDualStore()
        .withFirstRetryStore(primary)
        .withSecondRetryStore(secondary)
        .withFirstRetryStoreIdentifier("%message{primary-store-id}")
        .withSecondRetryStoreIdentifier("%message{secondary-store-id}")
        .withRetryStoreRoutingExpression("%message{" + ROUTE_KEY + "}")
        .withReportBuilder(reportBuilder);
    prepareForListenerTests(retrier);
    RetryFromJettyBase.RetryJettyListenerImpl listener = retrier.reporter;
    AdaptrisMessage msg = requestMessage("usa", null, null);
    msg.addMetadata("primary-store-id", "usa");
    msg.addMetadata("secondary-store-id", "usa");

    reset(primary, secondary, reportBuilder);
    listener.onAdaptrisMessage(msg, m -> {}, m -> {});

    assertEquals(RetryFromJettyBase.HTTP_BAD, msg.getMetadataValue(RetryFromJettyBase.HTTP_STATUS_KEY));
    verifyNoInteractions(primary, secondary, reportBuilder);
  }

  @Test
  void deleteRoutesToSecondaryStore() throws Exception {
    RetryStore primary = mock(RetryStore.class);
    RetryStore secondary = mock(RetryStore.class);
    ReportBuilder reportBuilder = mock(ReportBuilder.class);
    RetryFromJettyDualStore retrier = newDualStore(primary, secondary, reportBuilder);
    prepareForListenerTests(retrier);
    RetryFromJettyBase.RetryJettyListenerImpl listener = retrier.deleter;
    AdaptrisMessage msg = requestMessage("eu", RetryFromJettyBase.HTTP_DELETE_METHOD,
        RetryFromJettyBase.DEFAULT_DELETE_PREFIX + "msg-2");

    reset(primary, secondary, reportBuilder);
    when(secondary.delete("msg-2")).thenReturn(true);

    assertEquals("msg-2", listener.extractMsgId(retrier.deleteRouting, msg));
    listener.onAdaptrisMessage(msg, m -> {}, m -> {});

    verify(secondary).delete("msg-2");
    verifyNoInteractions(primary);
  }

  @Test
  void deleteRoutesToPrimaryStore() throws Exception {
    RetryStore primary = mock(RetryStore.class);
    RetryStore secondary = mock(RetryStore.class);
    ReportBuilder reportBuilder = mock(ReportBuilder.class);
    RetryFromJettyDualStore retrier = newDualStore(primary, secondary, reportBuilder);
    prepareForListenerTests(retrier);
    RetryFromJettyBase.RetryJettyListenerImpl listener = retrier.deleter;
    AdaptrisMessage msg = requestMessage("usa", RetryFromJettyBase.HTTP_DELETE_METHOD,
        RetryFromJettyBase.DEFAULT_DELETE_PREFIX + "msg-20");

    reset(primary, secondary, reportBuilder);
    when(primary.delete("msg-20")).thenReturn(true);

    assertEquals("msg-20", listener.extractMsgId(retrier.deleteRouting, msg));
    listener.onAdaptrisMessage(msg, m -> {}, m -> {});

    verify(primary).delete("msg-20");
    verifyNoInteractions(secondary);
  }

  @Test
  void retryRoutesToPrimaryStoreAndSubmitsWorkflow() throws Exception {
    RetryStore primary = mock(RetryStore.class);
    RetryStore secondary = mock(RetryStore.class);
    ReportBuilder reportBuilder = mock(ReportBuilder.class);
    RetryFromJettyDualStore retrier = newDualStore(primary, secondary, reportBuilder);
    prepareForListenerTests(retrier);
    RetryFromJettyBase.RetryJettyListenerImpl listener = retrier.retrier;
    AdaptrisMessage msg = requestMessage("usa", RetryFromJettyBase.HTTP_RETRY_METHOD,
        RetryFromJettyBase.DEFAULT_ENDPOINT_PREFIX + "msg-3");

    Workflow workflow = mock(Workflow.class);
    AdaptrisMessageConsumer consumer = mock(AdaptrisMessageConsumer.class);
    AdaptrisMessage retryMessage = AdaptrisMessageFactory.getDefaultInstance().newMessage("retry-payload");
    Map<String, String> metadata = new HashMap<>();
    metadata.put(Workflow.WORKFLOW_ID_KEY, "workflow-1");

    when(workflow.obtainWorkflowId()).thenReturn("workflow-1");
    when(workflow.retrieveComponentState()).thenReturn(StartedState.getInstance());
    when(workflow.getConsumer()).thenReturn(consumer);
    when(consumer.getMessageFactory()).thenReturn(AdaptrisMessageFactory.getDefaultInstance());
    reset(primary, secondary, reportBuilder);
    when(primary.getMetadata("msg-3")).thenReturn(metadata);
    when(primary.buildForRetry(eq("msg-3"), same(metadata), same(AdaptrisMessageFactory.getDefaultInstance())))
        .thenReturn(retryMessage);

    retrier.addWorkflow(workflow);
    ExecutorService executor = Executors.newSingleThreadExecutor();
    retrier.workflowSubmitter = executor;
    try {
      assertEquals("msg-3", listener.extractMsgId(retrier.retryRouting, msg));
      listener.onAdaptrisMessage(msg, m -> {}, m -> {});
      verify(primary).getMetadata("msg-3");
      verifyNoInteractions(secondary);
      verify(workflow, timeout(1000)).onAdaptrisMessage(same(retryMessage), any(), any());
    } finally {
      executor.shutdownNow();
    }
  }

  @Test
  void retryRoutesToSecondaryStoreAndSubmitsWorkflow() throws Exception {
    RetryStore primary = mock(RetryStore.class);
    RetryStore secondary = mock(RetryStore.class);
    ReportBuilder reportBuilder = mock(ReportBuilder.class);
    RetryFromJettyDualStore retrier = newDualStore(primary, secondary, reportBuilder);
    prepareForListenerTests(retrier);
    RetryFromJettyBase.RetryJettyListenerImpl listener = retrier.retrier;
    AdaptrisMessage msg = requestMessage("eu", RetryFromJettyBase.HTTP_RETRY_METHOD,
        RetryFromJettyBase.DEFAULT_ENDPOINT_PREFIX + "msg-30");

    Workflow workflow = mock(Workflow.class);
    AdaptrisMessageConsumer consumer = mock(AdaptrisMessageConsumer.class);
    AdaptrisMessage retryMessage = AdaptrisMessageFactory.getDefaultInstance().newMessage("retry-payload");
    Map<String, String> metadata = new HashMap<>();
    metadata.put(Workflow.WORKFLOW_ID_KEY, "workflow-30");

    when(workflow.obtainWorkflowId()).thenReturn("workflow-30");
    when(workflow.retrieveComponentState()).thenReturn(StartedState.getInstance());
    when(workflow.getConsumer()).thenReturn(consumer);
    when(consumer.getMessageFactory()).thenReturn(AdaptrisMessageFactory.getDefaultInstance());
    reset(primary, secondary, reportBuilder);
    when(secondary.getMetadata("msg-30")).thenReturn(metadata);
    when(secondary.buildForRetry(eq("msg-30"), same(metadata), same(AdaptrisMessageFactory.getDefaultInstance())))
        .thenReturn(retryMessage);

    retrier.addWorkflow(workflow);
    ExecutorService executor = Executors.newSingleThreadExecutor();
    retrier.workflowSubmitter = executor;
    try {
      assertEquals("msg-30", listener.extractMsgId(retrier.retryRouting, msg));
      listener.onAdaptrisMessage(msg, m -> {}, m -> {});
      verify(secondary).getMetadata("msg-30");
      verifyNoInteractions(primary);
      verify(workflow, timeout(1000)).onAdaptrisMessage(same(retryMessage), any(), any());
    } finally {
      executor.shutdownNow();
    }
  }

  @Test
  void stackTraceRoutesToSecondaryStore() throws Exception {
    RetryStore primary = mock(RetryStore.class);
    RetryStore secondary = mock(RetryStore.class);
    ReportBuilder reportBuilder = mock(ReportBuilder.class);
    RetryFromJettyDualStore retrier = newDualStore(primary, secondary, reportBuilder);
    prepareForListenerTests(retrier);
    RetryFromJettyBase.RetryJettyListenerImpl listener = retrier.stacktraceGetter;
    AdaptrisMessage msg = requestMessage("eu", RetryFromJettyBase.HTTP_STACKTRACE_METHOD,
        RetryFromJettyBase.DEFAULT_STACKTRACE_PREFIX + "msg-4");

    reset(primary, secondary, reportBuilder);
    when(secondary.getStackTrace("msg-4")).thenReturn("stacktrace");

    assertEquals("msg-4", listener.extractMsgId(retrier.stackTraceRouting, msg));
    listener.onAdaptrisMessage(msg, m -> {}, m -> {});
    verify(secondary).getStackTrace("msg-4");
    verifyNoInteractions(primary);
    assertNotNull(msg.getContent());
  }

  @Test
  void stackTraceRoutesToPrimaryStore() throws Exception {
    RetryStore primary = mock(RetryStore.class);
    RetryStore secondary = mock(RetryStore.class);
    ReportBuilder reportBuilder = mock(ReportBuilder.class);
    RetryFromJettyDualStore retrier = newDualStore(primary, secondary, reportBuilder);
    prepareForListenerTests(retrier);
    RetryFromJettyBase.RetryJettyListenerImpl listener = retrier.stacktraceGetter;
    AdaptrisMessage msg = requestMessage("usa", RetryFromJettyBase.HTTP_STACKTRACE_METHOD,
        RetryFromJettyBase.DEFAULT_STACKTRACE_PREFIX + "msg-40");

    reset(primary, secondary, reportBuilder);
    when(primary.getStackTrace("msg-40")).thenReturn("stacktrace-primary");

    assertEquals("msg-40", listener.extractMsgId(retrier.stackTraceRouting, msg));
    listener.onAdaptrisMessage(msg, m -> {}, m -> {});
    verify(primary).getStackTrace("msg-40");
    verifyNoInteractions(secondary);
    assertNotNull(msg.getContent());
  }

  @Test
  void unknownRouteDoesNotFallBackToAnyStore() throws Exception {
    RetryStore primary = mock(RetryStore.class);
    RetryStore secondary = mock(RetryStore.class);
    ReportBuilder reportBuilder = mock(ReportBuilder.class);
    RetryFromJettyDualStore retrier = newDualStore(primary, secondary, reportBuilder);
    prepareForListenerTests(retrier);
    reset(primary, secondary, reportBuilder);
    RetryFromJettyBase.RetryJettyListenerImpl listener = retrier.reporter;
    AdaptrisMessage msg = requestMessage("unknown", null, null);

    listener.onAdaptrisMessage(msg, m -> {}, m -> {});

    assertEquals(RetryFromJettyBase.HTTP_BAD, msg.getMetadataValue(RetryFromJettyBase.HTTP_STATUS_KEY));
    verifyNoInteractions(primary, secondary, reportBuilder);
  }

  @Test
  void reportWithIncludeErrorMessageFalse() throws Exception {
    RetryStore primary = mock(RetryStore.class);
    RetryStore secondary = mock(RetryStore.class);
    ReportBuilder reportBuilder = mock(ReportBuilder.class);
    RetryFromJettyDualStore retrier = newDualStore(primary, secondary, reportBuilder);
    prepareForListenerTests(retrier);
    RetryFromJettyBase.RetryJettyListenerImpl listener = retrier.reporter;
    AdaptrisMessage msg = requestMessage("usa", null, null);
    msg.addMetadata(retrier.includeErrorMessageFlagMetadataKey(), "false");

    reset(primary, secondary, reportBuilder);
    when(primary.report(false)).thenReturn(Collections.emptyList());

    listener.onAdaptrisMessage(msg, m -> {}, m -> {});

    assertEquals(RetryFromJettyBase.HTTP_OK, msg.getMetadataValue(RetryFromJettyBase.HTTP_STATUS_KEY));
    verify(primary).report(false);
    verifyNoInteractions(secondary);
  }

  @Test
  void reportMissingRouteMetadataReturnsBadRequest() throws Exception {
    RetryStore primary = mock(RetryStore.class);
    RetryStore secondary = mock(RetryStore.class);
    ReportBuilder reportBuilder = mock(ReportBuilder.class);
    RetryFromJettyDualStore retrier = newDualStore(primary, secondary, reportBuilder);
    prepareForListenerTests(retrier);
    RetryFromJettyBase.RetryJettyListenerImpl listener = retrier.reporter;
    AdaptrisMessage msg = requestMessage(null, null, null);

    reset(primary, secondary, reportBuilder);
    listener.onAdaptrisMessage(msg, m -> {}, m -> {});

    assertEquals(RetryFromJettyBase.HTTP_BAD, msg.getMetadataValue(RetryFromJettyBase.HTTP_STATUS_KEY));
    verifyNoInteractions(primary, secondary, reportBuilder);
  }

  @Test
  void reportWithSecondaryAndNoRoutingExpressionReturnsBadRequest() {
    RetryStore primary = mock(RetryStore.class);
    RetryStore secondary = mock(RetryStore.class);
    ReportBuilder reportBuilder = mock(ReportBuilder.class);
    RetryFromJettyDualStore retrier = new RetryFromJettyDualStore()
        .withFirstRetryStore(primary)
        .withSecondRetryStore(secondary)
        .withFirstRetryStoreIdentifier("usa")
        .withSecondRetryStoreIdentifier("eu")
        .withRetryStoreRoutingExpression(" ")
        .withReportBuilder(reportBuilder);
    RetryFromJettyDualStore.ReportListener listener = retrier.new ReportListener();
    AdaptrisMessage msg = requestMessage("usa", null, null);

    listener.onAdaptrisMessage(msg, m -> {}, m -> {});

    assertEquals(RetryFromJettyBase.HTTP_BAD, msg.getMetadataValue(RetryFromJettyBase.HTTP_STATUS_KEY));
    verifyNoInteractions(primary, secondary, reportBuilder);
  }

  @Test
  void reportRouteResolutionExceptionReturnsBadRequest() throws Exception {
    RetryStore primary = mock(RetryStore.class);
    RetryStore secondary = mock(RetryStore.class);
    ReportBuilder reportBuilder = mock(ReportBuilder.class);
    RetryFromJettyDualStore retrier = newDualStore(primary, secondary, reportBuilder);
    prepareForListenerTests(retrier);
    RetryFromJettyBase.RetryJettyListenerImpl listener = retrier.reporter;
    AdaptrisMessage msg = spy(requestMessage("usa", null, null));

    doThrow(new RuntimeException("boom-resolve"))
        .when(msg)
        .resolve("%message{" + ROUTE_KEY + "}");
    reset(primary, secondary, reportBuilder);

    listener.onAdaptrisMessage(msg, m -> {}, m -> {});

    assertEquals(RetryFromJettyBase.HTTP_BAD, msg.getMetadataValue(RetryFromJettyBase.HTTP_STATUS_KEY));
    verifyNoInteractions(primary, secondary, reportBuilder);
  }

  @Test
  void reportIdentifierResolutionExceptionReturnsBadRequest() throws Exception {
    RetryStore primary = mock(RetryStore.class);
    RetryStore secondary = mock(RetryStore.class);
    ReportBuilder reportBuilder = mock(ReportBuilder.class);
    RetryFromJettyDualStore retrier = new RetryFromJettyDualStore()
        .withFirstRetryStore(primary)
        .withSecondRetryStore(secondary)
        .withFirstRetryStoreIdentifier("%message{primary-store-id}")
        .withSecondRetryStoreIdentifier("eu")
        .withRetryStoreRoutingExpression("%message{" + ROUTE_KEY + "}")
        .withReportBuilder(reportBuilder);
    prepareForListenerTests(retrier);
    RetryFromJettyBase.RetryJettyListenerImpl listener = retrier.reporter;
    AdaptrisMessage msg = spy(requestMessage("usa", null, null));

    doThrow(new RuntimeException("boom-id"))
        .when(msg)
        .resolve("%message{primary-store-id}");
    reset(primary, secondary, reportBuilder);

    listener.onAdaptrisMessage(msg, m -> {}, m -> {});

    assertEquals(RetryFromJettyBase.HTTP_BAD, msg.getMetadataValue(RetryFromJettyBase.HTTP_STATUS_KEY));
    verifyNoInteractions(primary, secondary, reportBuilder);
  }

  @Test
  void reportListenerHandlesBuilderException() throws Exception {
    RetryStore primary = mock(RetryStore.class);
    RetryStore secondary = mock(RetryStore.class);
    ReportBuilder reportBuilder = mock(ReportBuilder.class);
    RetryFromJettyDualStore retrier = newDualStore(primary, secondary, reportBuilder);
    prepareForListenerTests(retrier);
    RetryFromJettyBase.RetryJettyListenerImpl listener = retrier.reporter;
    AdaptrisMessage msg = requestMessage("usa", null, null);

    when(primary.report(true)).thenReturn(Collections.emptyList());
    doThrow(new RuntimeException("boom-report")).when(reportBuilder).build(any(), same(msg));

    listener.onAdaptrisMessage(msg, m -> {}, m -> {});

    assertEquals(RetryFromJettyBase.HTTP_ERROR, msg.getMetadataValue(RetryFromJettyBase.HTTP_STATUS_KEY));
    assertTrue(msg.getContent().contains("boom-report"));
  }

  @Test
  void deleteReturnsNotFoundWhenStoreReturnsFalse() throws Exception {
    RetryStore primary = mock(RetryStore.class);
    RetryStore secondary = mock(RetryStore.class);
    ReportBuilder reportBuilder = mock(ReportBuilder.class);
    RetryFromJettyDualStore retrier = newDualStore(primary, secondary, reportBuilder);
    prepareForListenerTests(retrier);
    RetryFromJettyBase.RetryJettyListenerImpl listener = retrier.deleter;
    AdaptrisMessage msg = requestMessage("usa", RetryFromJettyBase.HTTP_DELETE_METHOD,
        RetryFromJettyBase.DEFAULT_DELETE_PREFIX + "missing-msg");

    when(primary.delete("missing-msg")).thenReturn(false);

    listener.onAdaptrisMessage(msg, m -> {}, m -> {});

    assertEquals(RetryFromJettyBase.HTTP_NOT_FOUND,
        msg.getMetadataValue(RetryFromJettyBase.HTTP_STATUS_KEY));
  }

  @Test
  void deleteListenerHandlesException() throws Exception {
    RetryStore primary = mock(RetryStore.class);
    RetryStore secondary = mock(RetryStore.class);
    ReportBuilder reportBuilder = mock(ReportBuilder.class);
    RetryFromJettyDualStore retrier = newDualStore(primary, secondary, reportBuilder);
    prepareForListenerTests(retrier);
    RetryFromJettyBase.RetryJettyListenerImpl listener = retrier.deleter;
    AdaptrisMessage msg = requestMessage("usa", RetryFromJettyBase.HTTP_DELETE_METHOD,
        RetryFromJettyBase.DEFAULT_DELETE_PREFIX + "msg-ex");

    when(primary.delete("msg-ex")).thenThrow(new RuntimeException("boom-delete"));

    listener.onAdaptrisMessage(msg, m -> {}, m -> {});

    assertEquals(RetryFromJettyBase.HTTP_ERROR, msg.getMetadataValue(RetryFromJettyBase.HTTP_STATUS_KEY));
    assertTrue(msg.getContent().contains("boom-delete"));
  }

  @Test
  void deleteUnknownRouteReturnsBadRequest() throws Exception {
    RetryStore primary = mock(RetryStore.class);
    RetryStore secondary = mock(RetryStore.class);
    ReportBuilder reportBuilder = mock(ReportBuilder.class);
    RetryFromJettyDualStore retrier = newDualStore(primary, secondary, reportBuilder);
    prepareForListenerTests(retrier);
    RetryFromJettyBase.RetryJettyListenerImpl listener = retrier.deleter;
    AdaptrisMessage msg = requestMessage("unknown", RetryFromJettyBase.HTTP_DELETE_METHOD,
        RetryFromJettyBase.DEFAULT_DELETE_PREFIX + "msg-any");

    reset(primary, secondary, reportBuilder);
    listener.onAdaptrisMessage(msg, m -> {}, m -> {});

    assertEquals(RetryFromJettyBase.HTTP_BAD, msg.getMetadataValue(RetryFromJettyBase.HTTP_STATUS_KEY));
    verifyNoInteractions(primary, secondary);
  }

  @Test
  void retryListenerHandlesException() throws Exception {
    RetryStore primary = mock(RetryStore.class);
    RetryStore secondary = mock(RetryStore.class);
    ReportBuilder reportBuilder = mock(ReportBuilder.class);
    RetryFromJettyDualStore retrier = newDualStore(primary, secondary, reportBuilder);
    prepareForListenerTests(retrier);
    RetryFromJettyBase.RetryJettyListenerImpl listener = retrier.retrier;
    AdaptrisMessage msg = requestMessage("usa", RetryFromJettyBase.HTTP_RETRY_METHOD,
        RetryFromJettyBase.DEFAULT_ENDPOINT_PREFIX + "msg-ex");

    when(primary.getMetadata("msg-ex")).thenThrow(new InterlokException("boom-retry"));

    listener.onAdaptrisMessage(msg, m -> {}, m -> {});

    assertEquals(RetryFromJettyBase.HTTP_ERROR, msg.getMetadataValue(RetryFromJettyBase.HTTP_STATUS_KEY));
    assertTrue(msg.getContent().contains("boom-retry"));
  }

  @Test
  void retryMissingRouteReturnsBadRequest() throws Exception {
    RetryStore primary = mock(RetryStore.class);
    RetryStore secondary = mock(RetryStore.class);
    ReportBuilder reportBuilder = mock(ReportBuilder.class);
    RetryFromJettyDualStore retrier = newDualStore(primary, secondary, reportBuilder);
    prepareForListenerTests(retrier);
    RetryFromJettyBase.RetryJettyListenerImpl listener = retrier.retrier;
    AdaptrisMessage msg = requestMessage("usa", RetryFromJettyBase.HTTP_DELETE_METHOD,
        RetryFromJettyBase.DEFAULT_ENDPOINT_PREFIX + "msg-any");

    reset(primary, secondary, reportBuilder);
    listener.onAdaptrisMessage(msg, m -> {}, m -> {});

    assertEquals(RetryFromJettyBase.HTTP_BAD, msg.getMetadataValue(RetryFromJettyBase.HTTP_STATUS_KEY));
    verifyNoInteractions(primary, secondary);
  }

  @Test
  void retryUnknownRouteReturnsBadRequest() throws Exception {
    RetryStore primary = mock(RetryStore.class);
    RetryStore secondary = mock(RetryStore.class);
    ReportBuilder reportBuilder = mock(ReportBuilder.class);
    RetryFromJettyDualStore retrier = newDualStore(primary, secondary, reportBuilder);
    prepareForListenerTests(retrier);
    RetryFromJettyBase.RetryJettyListenerImpl listener = retrier.retrier;
    AdaptrisMessage msg = requestMessage("unknown", RetryFromJettyBase.HTTP_RETRY_METHOD,
        RetryFromJettyBase.DEFAULT_ENDPOINT_PREFIX + "msg-any");

    reset(primary, secondary, reportBuilder);
    listener.onAdaptrisMessage(msg, m -> {}, m -> {});

    assertEquals(RetryFromJettyBase.HTTP_BAD, msg.getMetadataValue(RetryFromJettyBase.HTTP_STATUS_KEY));
    verifyNoInteractions(primary, secondary);
  }

  @Test
  void stackTraceMissingRouteReturnsBadRequest() throws Exception {
    RetryStore primary = mock(RetryStore.class);
    RetryStore secondary = mock(RetryStore.class);
    ReportBuilder reportBuilder = mock(ReportBuilder.class);
    RetryFromJettyDualStore retrier = newDualStore(primary, secondary, reportBuilder);
    prepareForListenerTests(retrier);
    RetryFromJettyBase.RetryJettyListenerImpl listener = retrier.stacktraceGetter;
    AdaptrisMessage msg = requestMessage("usa", RetryFromJettyBase.HTTP_DELETE_METHOD,
        RetryFromJettyBase.DEFAULT_STACKTRACE_PREFIX + "msg-any");

    reset(primary, secondary, reportBuilder);
    listener.onAdaptrisMessage(msg, m -> {}, m -> {});

    assertEquals(RetryFromJettyBase.HTTP_BAD, msg.getMetadataValue(RetryFromJettyBase.HTTP_STATUS_KEY));
    verifyNoInteractions(primary, secondary);
  }

  @Test
  void stackTraceUnknownRouteReturnsBadRequest() throws Exception {
    RetryStore primary = mock(RetryStore.class);
    RetryStore secondary = mock(RetryStore.class);
    ReportBuilder reportBuilder = mock(ReportBuilder.class);
    RetryFromJettyDualStore retrier = newDualStore(primary, secondary, reportBuilder);
    prepareForListenerTests(retrier);
    RetryFromJettyBase.RetryJettyListenerImpl listener = retrier.stacktraceGetter;
    AdaptrisMessage msg = requestMessage("unknown", RetryFromJettyBase.HTTP_STACKTRACE_METHOD,
        RetryFromJettyBase.DEFAULT_STACKTRACE_PREFIX + "msg-any");

    reset(primary, secondary, reportBuilder);
    listener.onAdaptrisMessage(msg, m -> {}, m -> {});

    assertEquals(RetryFromJettyBase.HTTP_BAD, msg.getMetadataValue(RetryFromJettyBase.HTTP_STATUS_KEY));
    verifyNoInteractions(primary, secondary);
  }

  @Test
  void stackTraceListenerHandlesException() throws Exception {
    RetryStore primary = mock(RetryStore.class);
    RetryStore secondary = mock(RetryStore.class);
    ReportBuilder reportBuilder = mock(ReportBuilder.class);
    RetryFromJettyDualStore retrier = newDualStore(primary, secondary, reportBuilder);
    prepareForListenerTests(retrier);
    RetryFromJettyBase.RetryJettyListenerImpl listener = retrier.stacktraceGetter;
    AdaptrisMessage msg = requestMessage("usa", RetryFromJettyBase.HTTP_STACKTRACE_METHOD,
        RetryFromJettyBase.DEFAULT_STACKTRACE_PREFIX + "msg-ex");

    when(primary.getStackTrace("msg-ex")).thenThrow(new InterlokException("boom-stacktrace"));

    listener.onAdaptrisMessage(msg, m -> {}, m -> {});

    assertEquals(RetryFromJettyBase.HTTP_ERROR, msg.getMetadataValue(RetryFromJettyBase.HTTP_STATUS_KEY));
    assertTrue(msg.getContent().contains("boom-stacktrace"));
  }

  @Test
  void prepareFailsWithoutSecondaryStore() {
    RetryStore primary = mock(RetryStore.class);
    RetryFromJettyDualStore retrier = new RetryFromJettyDualStore().withFirstRetryStore(primary)
        .withFirstRetryStoreIdentifier("usa")
        .withRetryStoreRoutingExpression("%message{" + ROUTE_KEY + "}")
        .withReportBuilder(mock(ReportBuilder.class));

    CoreException ex = assertThrows(CoreException.class, retrier::prepare);
    assertTrue(ex.getMessage().contains("second RetryStore"));
  }

  @Test
  void prepareFailsWithoutPrimaryStore() {
    RetryFromJettyDualStore retrier = new RetryFromJettyDualStore().withReportBuilder(mock(ReportBuilder.class));

    CoreException ex = assertThrows(CoreException.class, retrier::prepare);
    assertTrue(ex.getMessage().contains("No first RetryStore configured"));
  }

  @Test
  void prepareFailsWhenSecondaryConfiguredWithoutRoutingExpression() {
    RetryFromJettyDualStore retrier = new RetryFromJettyDualStore()
        .withFirstRetryStore(mock(RetryStore.class))
        .withSecondRetryStore(mock(RetryStore.class))
        .withFirstRetryStoreIdentifier("usa")
        .withSecondRetryStoreIdentifier("eu")
        .withReportBuilder(mock(ReportBuilder.class));

    CoreException ex = assertThrows(CoreException.class, retrier::prepare);
    assertTrue(ex.getMessage().contains("retryStoreRoutingExpression is required."));
  }

  @Test
  void prepareFailsWhenSecondaryIdentifiersAreMissing() {
    RetryFromJettyDualStore retrier = new RetryFromJettyDualStore()
        .withFirstRetryStore(mock(RetryStore.class))
        .withSecondRetryStore(mock(RetryStore.class))
        .withFirstRetryStoreIdentifier(" ")
        .withSecondRetryStoreIdentifier(" ")
        .withRetryStoreRoutingExpression("%message{route}")
        .withReportBuilder(mock(ReportBuilder.class));

    CoreException ex = assertThrows(CoreException.class, retrier::prepare);
    assertTrue(ex.getMessage().contains("firstRetryStoreIdentifier and secondRetryStoreIdentifier are required"));
  }

  @Test
  void prepareFailsWhenIdentifiersAreEqual() {
    RetryFromJettyDualStore retrier = new RetryFromJettyDualStore()
        .withFirstRetryStore(mock(RetryStore.class))
        .withSecondRetryStore(mock(RetryStore.class))
        .withFirstRetryStoreIdentifier("usa")
        .withSecondRetryStoreIdentifier("usa")
        .withRetryStoreRoutingExpression("%message{route}")
        .withReportBuilder(mock(ReportBuilder.class));

    CoreException ex = assertThrows(CoreException.class, retrier::prepare);
    assertTrue(ex.getMessage().contains("must be distinct"));
  }

  @Test
  void friendlyNamesAreStable() throws Exception {
    RetryStore primary = mock(RetryStore.class);
    RetryStore secondary = mock(RetryStore.class);
    ReportBuilder reportBuilder = mock(ReportBuilder.class);
    RetryFromJettyDualStore retrier = newDualStore(primary, secondary, reportBuilder);
    prepareForListenerTests(retrier);

    assertEquals("RetryFromJettyDualStore::Report", retrier.reporter.friendlyName());
    assertEquals("RetryFromJettyDualStore::Delete", retrier.deleter.friendlyName());
    assertEquals("RetryFromJettyDualStore::Retry", retrier.retrier.friendlyName());
    assertEquals("RetryFromJettyDualStore::StackTrace", retrier.stacktraceGetter.friendlyName());
  }

  @Test
  void lifecycleMethodsOperateAcrossBothStores() throws Exception {
    RetryStore primary = mock(RetryStore.class);
    RetryStore secondary = mock(RetryStore.class);
    ReportBuilder reportBuilder = mock(ReportBuilder.class);
    RetryFromJettyDualStore retrier = new NoOpInfrastructureDualStore().withFirstRetryStore(primary)
        .withSecondRetryStore(secondary)
        .withFirstRetryStoreIdentifier("usa")
        .withSecondRetryStoreIdentifier("eu")
        .withRetryStoreRoutingExpression("%message{" + ROUTE_KEY + "}")
        .withReportBuilder(reportBuilder);

    retrier.init();
    retrier.start();
    retrier.stop();
    retrier.close();

    verify(primary).init();
    verify(secondary).init();
    verify(primary).start();
    verify(secondary).start();
    verify(primary).stop();
    verify(secondary).stop();
    verify(primary).close();
    verify(secondary).close();
  }

  @Test
  void prepareIsIdempotentAfterFirstCall() throws Exception {
    RetryStore primary = mock(RetryStore.class);
    RetryStore secondary = mock(RetryStore.class);
    ReportBuilder reportBuilder = mock(ReportBuilder.class);
    RetryFromJettyDualStore retrier = new NoOpInfrastructureDualStore().withFirstRetryStore(primary)
        .withSecondRetryStore(secondary)
        .withFirstRetryStoreIdentifier("usa")
        .withSecondRetryStoreIdentifier("eu")
        .withRetryStoreRoutingExpression("%message{" + ROUTE_KEY + "}")
        .withReportBuilder(reportBuilder);

    retrier.prepare();
    reset(primary, secondary);
    retrier.prepare();

    verifyNoInteractions(primary, secondary);
  }

  private void prepareForListenerTests(RetryFromJettyDualStore retrier) throws Exception {
    retrier.prepare();
    LifecycleHelper.init(retrier.reporter, retrier.retrier, retrier.deleter, retrier.stacktraceGetter);
    LifecycleHelper.init(retrier.retryRouting, retrier.deleteRouting, retrier.stackTraceRouting);
  }

  private RetryFromJettyDualStore newDualStore(RetryStore primary, RetryStore secondary,
      ReportBuilder reportBuilder) {
    return new RetryFromJettyDualStore().withFirstRetryStore(primary)
        .withSecondRetryStore(secondary)
        .withFirstRetryStoreIdentifier("usa")
        .withSecondRetryStoreIdentifier("eu")
        .withRetryStoreRoutingExpression("%message{" + ROUTE_KEY + "}")
        .withReportBuilder(reportBuilder);
  }

  private static class NoOpInfrastructureDualStore extends RetryFromJettyDualStore {
    @Override
    protected void prepareSharedComponents() {
      // no-op for pure lifecycle coverage tests
    }

    @Override
    protected void initListeners() {
      // no-op for pure lifecycle coverage tests
    }

    @Override
    protected void startListeners() {
      // no-op for pure lifecycle coverage tests
    }

    @Override
    protected void stopListeners() {
      // no-op for pure lifecycle coverage tests
    }

    @Override
    protected void closeListeners() {
      // no-op for pure lifecycle coverage tests
    }
  }

  private AdaptrisMessage requestMessage(String route, String method, String uri) {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    if (route != null) {
      msg.addMetadata(ROUTE_KEY, route);
    }
    if (method != null) {
      msg.addMetadata(CoreConstants.HTTP_METHOD, method);
    }
    if (uri != null) {
      msg.addMetadata(JettyConstants.JETTY_URI, uri);
    }
    return msg;
  }
}












