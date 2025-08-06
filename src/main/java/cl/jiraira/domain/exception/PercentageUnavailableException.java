package cl.jiraira.domain.exception;

public class PercentageUnavailableException extends RuntimeException {
    public PercentageUnavailableException(String message, Exception lastException) {
        super(message);
    }
}
