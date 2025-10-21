package com.adaptris.core;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

class ConfigurableExceptionHandlerTest {

    private ConfigurableExceptionHandler exceptionHandler;
    private AdaptrisMessage mockMessage;
    private ConfigurableExceptionHandler.Rule mockRule;
    private RegexExceptionMatcher mockMatcher;
    private Service mockService;
    private Workflow mockWorkflow;
    private EventHandler mockEventHandler;

    private ExceptionDetails buildDefaultExceptionDetails() {
        return new ExceptionDetails(
                "Exception", "Message", "Cause", "Stacktrace", Exception.class);
    }

    @BeforeEach
    void setUp() {
        exceptionHandler = new ConfigurableExceptionHandler();
        mockMessage = mock(AdaptrisMessage.class);
        mockRule = mock(ConfigurableExceptionHandler.Rule.class);
        mockMatcher = mock(RegexExceptionMatcher.class);
        mockService = mock(Service.class);
        exceptionHandler.setDefaultExceptionProcessingService(mockService);
        mockWorkflow = mock(Workflow.class);
        mockEventHandler = mock(EventHandler.class);
    }

    @Test
    void applyRuleIfMatchedHandlesExceptionField() throws CoreException {
        when(mockRule.getMatcher()).thenReturn(mockMatcher);
        when(mockRule.getExceptionProcessingService()).thenReturn(mockService);
        when(mockMatcher.getMatchAgainstField()).thenReturn(RegexExceptionMatcher.MatchAgainstField.EXCEPTION);
        when(mockMatcher.matches("Exception")).thenReturn(true);

        ExceptionDetails details = buildDefaultExceptionDetails();

        boolean result = exceptionHandler.applyRuleIfMatched(mockMessage, mockRule, details);

        assertTrue(result);
        verify(mockService).doService(mockMessage);
    }

    @Test
    void applyRuleIfMatchedHandlesExceptionMessageField() throws CoreException {
        when(mockRule.getMatcher()).thenReturn(mockMatcher);
        when(mockRule.getExceptionProcessingService()).thenReturn(mockService);
        when(mockMatcher.getMatchAgainstField()).thenReturn(RegexExceptionMatcher.MatchAgainstField.EXCEPTION_MESSAGE);
        when(mockMatcher.matches("Message")).thenReturn(true);

        ExceptionDetails details = buildDefaultExceptionDetails();

        boolean result = exceptionHandler.applyRuleIfMatched(mockMessage, mockRule, details);

        assertTrue(result);
        verify(mockService).doService(mockMessage);
    }

    @Test
    void applyRuleIfMatchedHandlesExceptionCauseField() throws CoreException {
        when(mockRule.getMatcher()).thenReturn(mockMatcher);
        when(mockRule.getExceptionProcessingService()).thenReturn(mockService);
        when(mockMatcher.getMatchAgainstField()).thenReturn(RegexExceptionMatcher.MatchAgainstField.EXCEPTION_CAUSE);
        when(mockMatcher.matches("Cause")).thenReturn(true);

        ExceptionDetails details = buildDefaultExceptionDetails();

        boolean result = exceptionHandler.applyRuleIfMatched(mockMessage, mockRule, details);

        assertTrue(result);
        verify(mockService).doService(mockMessage);
    }

    @Test
    void applyRuleIfMatchedHandlesStacktraceField() throws CoreException {
        when(mockRule.getMatcher()).thenReturn(mockMatcher);
        when(mockRule.getExceptionProcessingService()).thenReturn(mockService);
        when(mockMatcher.getMatchAgainstField()).thenReturn(RegexExceptionMatcher.MatchAgainstField.STACKTRACE);
        when(mockMatcher.matches("Stacktrace")).thenReturn(true);

        ExceptionDetails details = buildDefaultExceptionDetails();

        boolean result = exceptionHandler.applyRuleIfMatched(mockMessage, mockRule, details);

        assertTrue(result);
        verify(mockService).doService(mockMessage);
    }

    @Test
    void handleProcessingExceptionExecutesDefaultServiceWhenNoRuleMatches() throws ServiceException {
        exceptionHandler.setDefaultExceptionProcessingService(mockService);

        exceptionHandler.handleProcessingException(mockMessage);

        verify(mockService).doService(mockMessage);
    }

    @Test
    void handleProcessingExceptionSkipsDefaultServiceWhenRuleMatches() throws ServiceException {
        exceptionHandler.setDefaultExceptionProcessingService(mockService);
        exceptionHandler.getRules().add(mockRule);

        when(mockRule.getMatcher()).thenReturn(mockMatcher);
        when(mockRule.getExceptionProcessingService()).thenReturn(mockService);
        when(mockMatcher.getMatchAgainstField()).thenReturn(RegexExceptionMatcher.MatchAgainstField.EXCEPTION);
        when(mockMatcher.matches(anyString())).thenReturn(true);

        exceptionHandler.handleProcessingException(mockMessage);

        verify(mockService, times(1)).doService(mockMessage);
    }

    @Test
    void handleProcessingExceptionExtractsExceptionDetails() {
        Exception mockException = new RuntimeException("Test Exception");
        String mockCause = "Test Cause";
        Map<Object, Object> objectHeaders = new HashMap<>();
        objectHeaders.put(CoreConstants.OBJ_METADATA_EXCEPTION, mockException);
        objectHeaders.put(CoreConstants.OBJ_METADATA_EXCEPTION_CAUSE, mockCause);

        when(mockMessage.getObjectHeaders()).thenReturn(objectHeaders);

        exceptionHandler.handleProcessingException(mockMessage);

        verify(mockMessage, atLeastOnce()).getObjectHeaders();
    }

    @Test
    void handleProcessingExceptionDoesNothingWhenNoRulesAndNoDefaultService() {
        exceptionHandler.setDefaultExceptionProcessingService(null);
        exceptionHandler.handleProcessingException(mockMessage);

        verifyNoInteractions(mockService);
    }

    @Test
    void handleProcessingExceptionLogsErrorOnException() throws ServiceException {
        exceptionHandler.setDefaultExceptionProcessingService(mockService);
        doThrow(new RuntimeException("Simulated exception")).when(mockService).doService(mockMessage);

        exceptionHandler.handleProcessingException(mockMessage);

        verify(mockService).doService(mockMessage);
    }

    @Test
    void applyRuleIfMatchesHandlesWhenNoMatcher() throws CoreException {
        when(mockRule.getMatcher()).thenReturn(null);

        ExceptionDetails details = buildDefaultExceptionDetails();

        boolean result = exceptionHandler.applyRuleIfMatched(mockMessage, mockRule, details);

        assertFalse(result);
        verifyNoInteractions(mockService);
    }

    @Test
    void applyRuleIfMatchedReturnsFalseWhenServiceIsNull() throws CoreException {
        when(mockRule.getMatcher()).thenReturn(mockMatcher);
        when(mockRule.getExceptionProcessingService()).thenReturn(null);

        ExceptionDetails details = buildDefaultExceptionDetails();

        boolean result = exceptionHandler.applyRuleIfMatched(mockMessage, mockRule, details);

        assertFalse(result);
        verifyNoInteractions(mockService);
    }

    @Test
    void applyRuleIfMatchedReturnsFalseWhenMatcherIsNull() throws CoreException {
        when(mockRule.getMatcher()).thenReturn(null);
        when(mockRule.getExceptionProcessingService()).thenReturn(mockService);

        ExceptionDetails details = buildDefaultExceptionDetails();

        boolean result = exceptionHandler.applyRuleIfMatched(mockMessage, mockRule, details);

        assertFalse(result);
        verifyNoInteractions(mockService);
    }

    @Test
    void startService() throws CoreException {
        exceptionHandler.start();

        verify(mockService).requestStart();
    }

    @Test
    void stopService() {
        exceptionHandler.stop();

        verify(mockService).requestStop();
    }

    @Test
    void closeService() {
        exceptionHandler.close();

        verify(mockService).requestClose();
    }

    @Test
    void prepareService() throws CoreException {
        exceptionHandler.prepare();

        verify(mockService).prepare();
    }

    @Test
    void registerWorkflowAddsWorkflowToMap() {
        when(mockWorkflow.obtainWorkflowId()).thenReturn("workflow-id");

        exceptionHandler.registerWorkflow(mockWorkflow);

        assertTrue(exceptionHandler.getWorkflows().containsKey("workflow-id"));
        assertEquals(mockWorkflow, exceptionHandler.getWorkflows().get("workflow-id"));
    }

    @Test
    void registerEventHandlerSetsEventHandler() {
        exceptionHandler.registerEventHandler(mockEventHandler);

        assertEquals(mockEventHandler, exceptionHandler.getEventHandler());
    }

    @Test
    void hasConfiguredBehaviourReturnsTrueWhenServiceIsConfigured() {
        assertTrue(exceptionHandler.hasConfiguredBehaviour());
    }

    @Test
    void hasConfiguredBehaviourReturnsFalseWhenServiceIsNotConfigured() {
        exceptionHandler.setDefaultExceptionProcessingService(null);

        assertFalse(exceptionHandler.hasConfiguredBehaviour());
    }

    @Test
    void initRegistersAndInitializesService() throws CoreException {
        exceptionHandler.registerEventHandler(mockEventHandler);

        exceptionHandler.init();

        verify(mockService).requestInit();
    }
}