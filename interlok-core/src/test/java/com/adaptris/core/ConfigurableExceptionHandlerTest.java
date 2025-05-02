package com.adaptris.core;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConfigurableExceptionHandlerTest {

    private ConfigurableExceptionHandler exceptionHandler;
    private AdaptrisMessage mockMessage;
    private ConfigurableExceptionHandler.Rule mockRule;
    private RegexExceptionMatcher mockMatcher;
    private Service mockService;

    @BeforeEach
    void setUp() {
        exceptionHandler = new ConfigurableExceptionHandler();
        mockMessage = mock(AdaptrisMessage.class);
        mockRule = mock(ConfigurableExceptionHandler.Rule.class);
        mockMatcher = mock(RegexExceptionMatcher.class);
        mockService = mock(Service.class);
    }

    @Test
    void applyRuleIfMatchedHandlesExceptionField() throws ServiceException {
        when(mockRule.getMatcher()).thenReturn(mockMatcher);
        when(mockRule.getProcessingExceptionService()).thenReturn(mockService);
        when(mockMatcher.getMatchAgainstField()).thenReturn(RegexExceptionMatcher.MatchAgainstField.EXCEPTION);
        when(mockMatcher.matches(anyString())).thenReturn(true);

        ConfigurableExceptionHandler.ExceptionDetails details = new ConfigurableExceptionHandler.ExceptionDetails(
                "Exception", "Message", "Cause", "Stacktrace");

        boolean result = exceptionHandler.applyRuleIfMatched(mockMessage, mockRule, details);

        assertTrue(result);
        verify(mockService).doService(mockMessage);
    }

    @Test
    void applyRuleIfMatchedHandlesExceptionMessageField() throws ServiceException {
        when(mockRule.getMatcher()).thenReturn(mockMatcher);
        when(mockRule.getProcessingExceptionService()).thenReturn(mockService);
        when(mockMatcher.getMatchAgainstField()).thenReturn(RegexExceptionMatcher.MatchAgainstField.EXCEPTION_MESSAGE);
        when(mockMatcher.matches(anyString())).thenReturn(true);

        ConfigurableExceptionHandler.ExceptionDetails details = new ConfigurableExceptionHandler.ExceptionDetails(
                "Exception", "Message", "Cause", "Stacktrace");

        boolean result = exceptionHandler.applyRuleIfMatched(mockMessage, mockRule, details);

        assertTrue(result);
        verify(mockService).doService(mockMessage);
    }

    @Test
    void applyRuleIfMatchedHandlesExceptionCauseField() throws ServiceException {
        when(mockRule.getMatcher()).thenReturn(mockMatcher);
        when(mockRule.getProcessingExceptionService()).thenReturn(mockService);
        when(mockMatcher.getMatchAgainstField()).thenReturn(RegexExceptionMatcher.MatchAgainstField.EXCEPTION_CAUSE);
        when(mockMatcher.matches(anyString())).thenReturn(true);

        ConfigurableExceptionHandler.ExceptionDetails details = new ConfigurableExceptionHandler.ExceptionDetails(
                "Exception", "Message", "Cause", "Stacktrace");

        boolean result = exceptionHandler.applyRuleIfMatched(mockMessage, mockRule, details);

        assertTrue(result);
        verify(mockService).doService(mockMessage);
    }

    @Test
    void applyRuleIfMatchedHandlesStacktraceField() throws ServiceException {
        when(mockRule.getMatcher()).thenReturn(mockMatcher);
        when(mockRule.getProcessingExceptionService()).thenReturn(mockService);
        when(mockMatcher.getMatchAgainstField()).thenReturn(RegexExceptionMatcher.MatchAgainstField.STACKTRACE);
        when(mockMatcher.matches(anyString())).thenReturn(true);

        ConfigurableExceptionHandler.ExceptionDetails details = new ConfigurableExceptionHandler.ExceptionDetails(
                "Exception", "Message", "Cause", "Stacktrace");

        boolean result = exceptionHandler.applyRuleIfMatched(mockMessage, mockRule, details);

        assertTrue(result);
        verify(mockService).doService(mockMessage);
    }

    @Test
    void handleProcessingExceptionExecutesDefaultServiceWhenNoRuleMatches() throws ServiceException {
        exceptionHandler.setProcessingExceptionService(mockService);

        exceptionHandler.handleProcessingException(mockMessage);

        verify(mockService).doService(mockMessage);
    }

    @Test
    void handleProcessingExceptionSkipsDefaultServiceWhenRuleMatches() throws ServiceException {
        exceptionHandler.setProcessingExceptionService(mockService);
        exceptionHandler.getRules().add(mockRule);

        when(mockRule.getMatcher()).thenReturn(mockMatcher);
        when(mockRule.getProcessingExceptionService()).thenReturn(mockService);
        when(mockMatcher.getMatchAgainstField()).thenReturn(RegexExceptionMatcher.MatchAgainstField.EXCEPTION);
        when(mockMatcher.matches(anyString())).thenReturn(true);

        ConfigurableExceptionHandler.ExceptionDetails details = new ConfigurableExceptionHandler.ExceptionDetails(
                "Exception", "Message", "Cause", "Stacktrace");

        exceptionHandler.handleProcessingException(mockMessage);

        verify(mockService, times(1)).doService(mockMessage);
    }

    @Test
    void handleProcessingExceptionDoesNothingWhenNoRulesAndNoDefaultService() {
        exceptionHandler.handleProcessingException(mockMessage);

        verifyNoInteractions(mockService);
    }

    @Test
    void applyRuleIfMatchesHandlesWhenNoMatcher() throws ServiceException {
        when(mockRule.getMatcher()).thenReturn(null);

        ConfigurableExceptionHandler.ExceptionDetails details = new ConfigurableExceptionHandler.ExceptionDetails(
                "Exception", "Message", "Cause", "Stacktrace");

        boolean result = exceptionHandler.applyRuleIfMatched(mockMessage, mockRule, details);

        assertFalse(result);
        verifyNoInteractions(mockService);
    }
}