package com.adaptris.core.http.jetty.retry;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.MetadataElement;
import com.adaptris.interlok.cloud.BlobListRenderer;
import com.adaptris.interlok.cloud.RemoteBlob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class RetryStoreListServiceTest {
    private static final String METADATA_KEY= "includeErrorMessage";

    private RetryStoreListService service;
    private BlobListRenderer renderer;
    private AdaptrisMessage message;
    private InMemoryRetryStore retryStore;
    private Iterable<RemoteBlob> reportResult;

    @BeforeEach
    void setUp() {
        service = new RetryStoreListService();
        renderer = mock(BlobListRenderer.class);
        message = mock(AdaptrisMessage.class);
        retryStore = mock(InMemoryRetryStore.class);
        reportResult = Collections.emptyList();
        service.setReportRenderer(renderer);
        service.setRetryStore(retryStore);
    }

    @Test
    void test_includeErrorMessageTrue() throws Exception {
        when(message.getMetadataValue(METADATA_KEY)).thenReturn("true");
        when(retryStore.report(true)).thenReturn(reportResult);

        service.doService(message);

        verify(renderer).render(eq(reportResult), eq(message));
    }

    @Test
    void test_includeErrorMessageFalse() throws Exception {
        when(message.getMetadataValue(METADATA_KEY)).thenReturn("false");
        when(retryStore.report(false)).thenReturn(reportResult);

        service.doService(message);

        verify(renderer).render(eq(reportResult), eq(message));
    }

    @Test
    void test_includeErrorMessageValueNotNull() throws Exception {
        // Both metadata and metadataValue are non-null
        when(message.getMetadata(METADATA_KEY)).thenReturn(
            new MetadataElement(METADATA_KEY, "true"));
        when(message.getMetadataValue(METADATA_KEY)).thenReturn("true");
        when(retryStore.report(true)).thenReturn(reportResult);

        service.doService(message);

        verify(renderer).render(eq(reportResult), eq(message));
    }

    @Test
    void test_includeErrorMessageNotPresent_defaultsTrue() throws Exception {
        when(message.getMetadataValue(METADATA_KEY)).thenReturn(null);
        when(retryStore.report(true)).thenReturn(reportResult);

        service.doService(message);

        verify(renderer).render(eq(reportResult), eq(message));
    }

    @Test
    void test_errorScenario_throwsServiceException() throws Exception {
        when(message.getMetadataValue("includeErrorMessage")).thenReturn("true");
        when(retryStore.report(true)).thenThrow(new RuntimeException("Simulated error"));

        org.junit.jupiter.api.Assertions.assertThrows(
            com.adaptris.core.ServiceException.class,
            () -> service.doService(message)
        );
    }
}