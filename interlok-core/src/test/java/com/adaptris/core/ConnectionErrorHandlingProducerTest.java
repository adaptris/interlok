package com.adaptris.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConnectionErrorHandlingProducerTest {

    private ConnectionErrorHandlingProducer producer;
    @Mock
    private AdaptrisMessageProducer mockDelegate;
    @Mock
    private AdaptrisMessage mockMessage;
    @Mock
    private AdaptrisConnection mockConnection;
    @Mock
    private AdaptrisMessageEncoder mockEncoder;
    @Mock
    private AdaptrisMessageFactory mockFactory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        producer = new ConnectionErrorHandlingProducer();
        producer.setDelegate(mockDelegate);
    }

    @Test
    void testProduce_Delegates() throws Exception {
        producer.produce(mockMessage);
        verify(mockDelegate).produce(mockMessage);
    }

    @Test
    void testProduce_PropagatesException() throws Exception {
        doThrow(new ProduceException("fail")).when(mockDelegate).produce(mockMessage);
        assertThrows(ProduceException.class, () -> producer.produce(mockMessage));
    }

    @Test
    void testRequest_Delegates() throws Exception {
        when(mockDelegate.request(mockMessage)).thenReturn(mockMessage);
        assertEquals(mockMessage, producer.request(mockMessage));
        verify(mockDelegate).request(mockMessage);
    }

    @Test
    void testRequestWithTimeout_Delegates() throws Exception {
        when(mockDelegate.request(mockMessage, 100L)).thenReturn(mockMessage);
        assertEquals(mockMessage, producer.request(mockMessage, 100L));
        verify(mockDelegate).request(mockMessage, 100L);
    }

    @Test
    void testRegisterConnection_Delegates() {
        producer.registerConnection(mockConnection);
        verify(mockDelegate).registerConnection(mockConnection);
    }

    @Test
    void testRetrieveConnection_Delegates() {
        when(mockDelegate.retrieveConnection(AdaptrisConnection.class)).thenReturn(mockConnection);
        assertEquals(mockConnection, producer.retrieveConnection(AdaptrisConnection.class));
    }

    @Test
    void testGetEncoder_Delegates() {
        when(mockDelegate.getEncoder()).thenReturn(mockEncoder);
        assertEquals(mockEncoder, producer.getEncoder());
    }

    @Test
    void testSetEncoder_Delegates() {
        producer.setEncoder(mockEncoder);
        verify(mockDelegate).setEncoder(mockEncoder);
    }

    @Test
    void testHandleConnectionException_Delegates() throws Exception {
        producer.handleConnectionException();
        verify(mockDelegate).handleConnectionException();
    }

    @Test
    void testEncode_Delegates() throws Exception {
        byte[] bytes = new byte[]{1,2,3};
        when(mockDelegate.encode(mockMessage)).thenReturn(bytes);
        assertArrayEquals(bytes, producer.encode(mockMessage));
    }

    @Test
    void testDecode_Delegates() throws Exception {
        byte[] bytes = new byte[]{1,2,3};
        when(mockDelegate.decode(bytes)).thenReturn(mockMessage);
        assertEquals(mockMessage, producer.decode(bytes));
    }

    @Test
    void testGetMessageFactory_Delegates() {
        when(mockDelegate.getMessageFactory()).thenReturn(mockFactory);
        assertEquals(mockFactory, producer.getMessageFactory());
    }

    @Test
    void testSetMessageFactory_Delegates() {
        producer.setMessageFactory(mockFactory);
        verify(mockDelegate).setMessageFactory(mockFactory);
    }

    @Test
    void testGetUniqueId_Delegates() {
        when(mockDelegate.getUniqueId()).thenReturn("id");
        assertEquals("id", producer.getUniqueId());
    }

    @Test
    void testPrepare_Delegates() throws Exception {
        producer.prepare();
        verify(mockDelegate).prepare();
    }

    @Test
    void testCreateName_Delegates() {
        when(mockDelegate.createName()).thenReturn("name");
        assertEquals("name", producer.createName());
    }

    @Test
    void testCreateQualifier_Delegates() {
        when(mockDelegate.createQualifier()).thenReturn("qual");
        assertEquals("qual", producer.createQualifier());
    }

    @Test
    void testIsTrackingEndpoint_Delegates() {
        when(mockDelegate.isTrackingEndpoint()).thenReturn(true);
        assertTrue(producer.isTrackingEndpoint());
    }
}

