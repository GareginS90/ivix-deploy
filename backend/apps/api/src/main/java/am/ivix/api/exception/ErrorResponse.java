package am.ivix.api.exception;

import java.time.Instant;
import java.util.Map;

public class ErrorResponse {

    private Instant timestamp = Instant.now();
    private int status;
    private String error;
    private String message;
    private String path;
    private Map<String, Object> details;

    public ErrorResponse(int status, String error, String message, String path) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public ErrorResponse(int status, String error, String message, String path, Map<String, Object> details) {
        this(status, error, message, path);
        this.details = details;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public Map<String, Object> getDetails() {
        return details;
    }
}
