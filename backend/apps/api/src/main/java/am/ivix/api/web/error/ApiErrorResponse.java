package am.ivix.api.web.error;

import java.time.Instant;
import java.util.List;

public class ApiErrorResponse {

    private final Instant timestamp;
    private final int status;
    private final String error;
    private final String code;
    private final String message;
    private final List<FieldError> fieldErrors;

    public ApiErrorResponse(int status,
                            String error,
                            String code,
                            String message,
                            List<FieldError> fieldErrors) {
        this.timestamp = Instant.now();
        this.status = status;
        this.error = error;
        this.code = code;
        this.message = message;
        this.fieldErrors = fieldErrors;
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

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public List<FieldError> getFieldErrors() {
        return fieldErrors;
    }

    public static class FieldError {
        private final String field;
        private final String message;

        public FieldError(String field, String message) {
            this.field = field;
            this.message = message;
        }

        public String getField() {
            return field;
        }

        public String getMessage() {
            return message;
        }
    }
}
