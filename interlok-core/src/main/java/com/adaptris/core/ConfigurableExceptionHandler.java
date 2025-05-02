package com.adaptris.core;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.util.LifecycleHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import java.util.*;

import static com.adaptris.core.CoreConstants.OBJ_METADATA_EXCEPTION;
import static com.adaptris.core.CoreConstants.OBJ_METADATA_EXCEPTION_CAUSE;

@XStreamAlias("configurable-exception-handler")
@AdapterComponent
@ComponentProfile(summary = "An exception handling component configurable for specific errors", tag = "error-handling,base")
public class ConfigurableExceptionHandler extends RootProcessingExceptionHandler implements EventHandlerAware {

    @Getter
    @Setter
    @Valid
    private Service processingExceptionService;
    @Valid
    @AutoPopulated
    @XStreamImplicit
    @Getter
    @Setter
    private List<Rule> rules;
    @Getter
    private transient Map<String, Workflow> workflows;
    @Getter
    private transient EventHandler eventHandler;

    public ConfigurableExceptionHandler() {
        super();
        workflows = new HashMap<>();
        rules = new LinkedList<>();
    }

//    public ConfigurableExceptionHandler(ServiceList serviceList) {
//        this();
//        setProcessingExceptionService(serviceList);
//        rules = new LinkedList<>();
//    }

    @Override
    public void handleProcessingException(AdaptrisMessage msg) {
        msg.getObjectHeaders().put(CoreConstants.OBJ_METADATA_MESSAGE_FAILED, true);

        final ExceptionDetails exceptionDetails = extractExceptionDetailsFromObjectMetadata(msg);

        try {
            boolean matchedRule = false;

            for (Rule rule : rules) {
                matchedRule = applyRuleIfMatched(msg, rule, exceptionDetails);
            }

            if (!matchedRule && getProcessingExceptionService() != null) {
                getProcessingExceptionService().doService(msg);
            }
        } catch (Exception ex) {
            log.error("Exception handling error msg [{}]",
                    MessageLoggerImpl.LAST_RESORT_LOGGER.toString(msg), ex);
        }

        notifyParent(msg);
    }

    @Override
    public void init() throws CoreException {
        LifecycleHelper.registerEventHandler(getProcessingExceptionService(), eventHandler);
        LifecycleHelper.init(getProcessingExceptionService());
    }

    @Override
    public void start() throws CoreException {
        LifecycleHelper.start(getProcessingExceptionService());
    }

    @Override
    public void stop() {
        LifecycleHelper.stop(getProcessingExceptionService());
    }

    @Override
    public void close() {
        LifecycleHelper.close(getProcessingExceptionService());
    }

    @Override
    public void prepare() throws CoreException {
        LifecycleHelper.prepare(getProcessingExceptionService());
    }

    @Override
    public void registerWorkflow(Workflow w) {
        workflows.put(w.obtainWorkflowId(), w);
    }

    @Override
    public void registerEventHandler(EventHandler eh) {
        eventHandler = eh;
    }

    @Override
    public boolean hasConfiguredBehaviour() {
        return getProcessingExceptionService() != null;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @AdapterComponent
    @XStreamAlias("exception-rule")
    @ComponentProfile(summary = "A rule for handling a specific error", tag = "error-handling")
    @DisplayOrder(order = {"matcher", "processingExceptionService"})
    public static class Rule {
        private RegexExceptionMatcher matcher;
        private Service processingExceptionService;
    }

    record ExceptionDetails(
            String exception,
            String message,
            String cause,
            String stacktrace) {
    }

    private ExceptionDetails extractExceptionDetailsFromObjectMetadata(AdaptrisMessage msg) {
        final Exception exception = (Exception) msg.getObjectHeaders().getOrDefault(OBJ_METADATA_EXCEPTION, null);
        final Throwable cause = (Throwable) msg.getObjectHeaders().getOrDefault(OBJ_METADATA_EXCEPTION_CAUSE, null);

        return new ExceptionDetails(
                (exception != null ? exception.toString() : null),
                (exception != null ? exception.getMessage() : null),
                (cause != null ? cause.toString() : null),
                (exception != null ? Arrays.toString(exception.getStackTrace()) : null)
        );
    }

    boolean applyRuleIfMatched(AdaptrisMessage msg, Rule rule, ExceptionDetails exceptionDetails) throws ServiceException {
        if (rule.getMatcher() == null) {
            return false;
        }

        final Service ruleProcessingExceptionService = rule.getProcessingExceptionService();
        final RegexExceptionMatcher matcher = rule.getMatcher();

        switch (matcher.getMatchAgainstField()) {
            case EXCEPTION -> {
                log.debug("Matching rule {} against Exception {}", rule, exceptionDetails.exception);
                if (matcher.matches(exceptionDetails.exception)) {
                    ruleProcessingExceptionService.doService(msg);
                    return true;
                }
            }
            case EXCEPTION_MESSAGE -> {
                log.debug("Matching rule {} against Exception Message {}", rule, exceptionDetails.message);
                if (matcher.matches(exceptionDetails.message)) {
                    ruleProcessingExceptionService.doService(msg);
                    return true;
                }
            }
            case EXCEPTION_CAUSE -> {
                log.debug("Matching rule {} against Exception Cause {}", rule, exceptionDetails.cause);
                if (matcher.matches(exceptionDetails.cause)) {
                    ruleProcessingExceptionService.doService(msg);
                    return true;
                }
            }
            case STACKTRACE -> {
                log.debug("Matching rule {} against Stacktrace {}", rule, exceptionDetails.stacktrace);
                if (matcher.matches(exceptionDetails.stacktrace)) {
                    ruleProcessingExceptionService.doService(msg);
                    return true;
                }
            }
        }
        return false;
    }
}
