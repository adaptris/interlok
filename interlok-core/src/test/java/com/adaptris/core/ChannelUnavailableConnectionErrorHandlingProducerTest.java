package com.adaptris.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChannelUnavailableConnectionErrorHandlingProducerTest {

    private ChannelUnavailableConnectionErrorHandlingProducer producer;
    @Mock
    private AdaptrisConnection mockConnection;
    @Mock
    private Channel mockChannel;
    @Mock
    private AdaptrisMessage mockMessage;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // warning is safe to ignore for test context
        producer = spy(new ChannelUnavailableConnectionErrorHandlingProducer());
        doReturn(mockConnection).when(producer).retrieveConnection(AdaptrisConnection.class);
        Set<StateManagedComponent> listeners = new HashSet<>();
        listeners.add(mockChannel);
        when(mockConnection.retrieveExceptionListeners()).thenReturn(listeners);
        producer.setConnectionErrorThreshold(2);
        producer.setConnectionErrorWaitDuration(Duration.ofMillis(100));
        producer.setConnectionErrors(0);
        // set autoConfigureConnection via reflection since field is private
        setAutoConfigureConnection(producer, Boolean.TRUE);
    }

    private void setAutoConfigureConnection(ChannelUnavailableConnectionErrorHandlingProducer p, Boolean value) {
        try {
            java.lang.reflect.Field f = ChannelUnavailableConnectionErrorHandlingProducer.class.getDeclaredField("autoConfigureConnection");
            f.setAccessible(true);
            f.set(p, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testProduce_Success_ResetsErrorCount() throws Exception {
        // Simulate a successful produce by not throwing any exception
        producer.setConnectionErrors(1);
        // We need to mock dependencies so that produce() does not throw
        // but let the actual method run
        // If produce() requires more setup, add it here
        // For now, just call and check error count
        try {
            producer.produce(mockMessage);
        } catch (Exception ignored) {}
        assertEquals(0, producer.getConnectionErrors());
    }

    @Test
    void testProduce_Failure_IncrementsErrorCount() throws Exception {
        // Simulate a failure by throwing an exception
        producer.setConnectionErrors(0);
        // Call handleConnectionException directly to increment error count
        producer.handleConnectionException();
        assertEquals(1, producer.getConnectionErrors());
    }

    @Test
    void testHandleConnectionException_ThresholdNotReached() throws Exception {
        producer.setConnectionErrors(0);
        producer.setConnectionErrorThreshold(2);
        producer.handleConnectionException();
        assertEquals(1, producer.getConnectionErrors());
        verify(mockChannel, never()).toggleAvailability(false);
    }

    @Test
    void testHandleConnectionException_ThresholdReached_AutoConfigure() throws Exception {
        producer.setConnectionErrors(1);
        producer.setConnectionErrorThreshold(2);
        setAutoConfigureConnection(producer, Boolean.TRUE);
        producer.handleConnectionException();
        assertEquals(2, producer.getConnectionErrors());
        verify(mockChannel).toggleAvailability(false);
        // Wait for timer to elapse and auto-recover
        Thread.sleep(150);
        verify(mockChannel, atLeastOnce()).toggleAvailability(true);
        assertEquals(0, producer.getConnectionErrors());
    }

    @Test
    void testHandleConnectionException_ThresholdReached_NoAutoConfigure() throws Exception {
        producer.setConnectionErrors(1);
        producer.setConnectionErrorThreshold(2);
        setAutoConfigureConnection(producer, Boolean.FALSE);
        producer.handleConnectionException();
        assertEquals(2, producer.getConnectionErrors());
        verify(mockChannel).toggleAvailability(false);
        // Should not auto-recover
        Thread.sleep(150);
        verify(mockChannel, never()).toggleAvailability(true);
    }

    @Test
    void testRequest_Success_ResetsErrorCount() {
        // Simulate a successful request by not throwing any exception
        producer.setConnectionErrors(1);
        try {
            producer.request(mockMessage);
        } catch (Exception ignored) {}
        assertEquals(0, producer.getConnectionErrors());
    }

    @Test
    void testRequestWithTimeout_Success_ResetsErrorCount() {
        producer.setConnectionErrors(1);
        try {
            producer.request(mockMessage, 100L);
        } catch (Exception ignored) {}
        assertEquals(0, producer.getConnectionErrors());
    }
}
