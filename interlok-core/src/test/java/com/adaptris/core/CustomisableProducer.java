package com.adaptris.core;

import com.adaptris.core.jms.DefinedJmsProducer;
import com.adaptris.core.jms.ProducerSession;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;

import static org.mockito.Mockito.mock;

class CustomisableProducer extends DefinedJmsProducer {
    ExceptionMatchingChannelRestartConnectionErrorHandlerTest.TriConsumerThrowsException<AdaptrisMessage, Destination, Destination> doProduceFn;
    ExceptionMatchingChannelRestartConnectionErrorHandlerTest.TriFunctionThrowsException<AdaptrisMessage, String, Long, AdaptrisMessage> doRequestFn;

    public CustomisableProducer(ExceptionMatchingChannelRestartConnectionErrorHandlerTest.TriConsumerThrowsException<AdaptrisMessage, Destination, Destination> doProduceFn,
                                ExceptionMatchingChannelRestartConnectionErrorHandlerTest.TriFunctionThrowsException<AdaptrisMessage, String, Long, AdaptrisMessage> doRequestFn) {
        this.doProduceFn = doProduceFn;
        this.doRequestFn = doRequestFn;
    }

    public ExceptionMatchingChannelRestartConnectionErrorHandlerTest.TriConsumerThrowsException<AdaptrisMessage, Destination, Destination> getDoProduceFn() {
        return doProduceFn;
    }

    public void setDoProduceFn(ExceptionMatchingChannelRestartConnectionErrorHandlerTest.TriConsumerThrowsException<AdaptrisMessage, Destination, Destination> doProduceFn) {
        this.doProduceFn = doProduceFn;
    }

    public ExceptionMatchingChannelRestartConnectionErrorHandlerTest.TriFunctionThrowsException<AdaptrisMessage, String, Long, AdaptrisMessage> getDoRequestFn() {
        return doRequestFn;
    }

    public void setDoRequestFn(ExceptionMatchingChannelRestartConnectionErrorHandlerTest.TriFunctionThrowsException<AdaptrisMessage, String, Long, AdaptrisMessage> doRequestFn) {
        this.doRequestFn = doRequestFn;
    }

    @Override
    public Destination createDestination(String name) throws JMSException {
        return mock(Destination.class);
    }

    @Override
    public void doProduce(AdaptrisMessage msg, Destination dest, Destination replyTo)
            throws JMSException, CoreException {
        doProduceFn.accept(msg, replyTo, dest);
    }

    @Override
    public Destination createTemporaryDestination() throws JMSException {
        return mock(Destination.class);
    }

    @Override
    public void rollback() {
        // do nothing
    }

    @Override
    public void commit() throws JMSException {
        // do nothing
    }

    @Override
    public String endpoint(AdaptrisMessage msg) throws ProduceException {
        return "endpoint";
    }

    @Override
    public AdaptrisMessage doRequest(AdaptrisMessage msg, String dest, long timeout) throws ProduceException {
        return doRequestFn.apply(msg, dest, timeout);
    }

    @Override
    public ProducerSession setupSession(AdaptrisMessage msg) throws JMSException {
        return null;
    }

    @Override
    public void logLinkedException(String prefix, Exception e) {
        super.logLinkedException(prefix, e);
    }

    @Override
    public int calculateDeliveryMode(AdaptrisMessage msg, String defaultDeliveryMode) {
        return super.calculateDeliveryMode(msg, defaultDeliveryMode);
    }


    @Override
    public long calculateTimeToLive(AdaptrisMessage msg, Long defaultTTL) throws JMSException {
        return super.calculateTimeToLive(msg, defaultTTL);
    }


    @Override
    public Message translate(AdaptrisMessage msg, Destination replyTo) throws JMSException {
        return super.translate(msg, replyTo);
    }

    @Override
    public int calculatePriority(AdaptrisMessage msg, Integer defaultPriority) {
        return super.calculatePriority(msg, defaultPriority);
    }

    @Override
    public boolean captureOutgoingMessageDetails() {
        return super.captureOutgoingMessageDetails();
    }
}
