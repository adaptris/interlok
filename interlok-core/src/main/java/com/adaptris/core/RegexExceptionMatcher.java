package com.adaptris.core;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.InputFieldHint;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Arrays;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * A matcher that uses regular expressions to match exception details.
 *
 * <p>This class provides functionality to match exception-related strings
 * (e.g., exception type, message, cause, or stack trace) against a configured
 * regular expression. The matching field is specified using the {@link MatchAgainstField}
 * enum.</p>
 *
 * <p>Key features:</p>
 * <ul>
 *   <li>Supports matching against different exception fields such as exception type,
 *       message, cause, or stack trace.</li>
 *   <li>Returns {@code false} if the input string is {@code null} or does not match
 *       the regex.</li>
 * </ul>
 *
 * @see MatchAgainstField
 */
@NoArgsConstructor
@Getter
@Setter
@XStreamAlias("regex-exception-matcher")
@AdapterComponent
@ComponentProfile(summary = "A matcher that uses regex to match an exception", tag = "error-handling")
public class RegexExceptionMatcher implements ExceptionMatcher {

    public enum MatchAgainstField {
        EXCEPTION((ExceptionDetails::exception)),
        EXCEPTION_MESSAGE(ExceptionDetails::message),
        EXCEPTION_CAUSE(ExceptionDetails::cause),
        STACKTRACE(ExceptionDetails::stacktrace);

        private Function<ExceptionDetails, String> getterFn;

        MatchAgainstField(Function<ExceptionDetails, String> getterFn) {
            this.getterFn = getterFn;
        }

        public String getSource(ExceptionDetails details) {
            return this.getterFn.apply(details);
        }

        public static String[] getValues() {
            return Arrays.stream(MatchAgainstField.values())
                    .map(Enum::name)
                    .toArray(String[]::new);
        }
    }

    private String regex;
    private transient Pattern compiledRegex;

    public void setRegex(String regex) {
        this.compiledRegex = null;
        this.regex = regex;
    }

    @InputFieldHint(style = "MatchAgainstField#getValues")
    private MatchAgainstField matchAgainstField;

    public boolean matches(String exceptionString) {
        if (exceptionString == null) {
            return false;
        }

        compileRegexIfRequired();
        return compiledRegex.matcher(exceptionString).matches();
    }

    private void compileRegexIfRequired() {
        if (compiledRegex == null || !compiledRegex.pattern().equals(regex)) {
            this.compiledRegex = Pattern.compile(regex);
        }
    }

    @Override
    public boolean matches(Exception exception) {
        ExceptionDetails details = ExceptionDetails.from(exception);
        return matches(matchAgainstField.getSource(details));
    }
}
