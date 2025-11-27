package am.ivix.api.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.OffsetDateTime;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleIllegalState(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err("bad_request", e.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntime(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err("bad_request", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAny(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err("internal_error", "Unexpected error"));
    }

    private Map<String, Object> err(String code, String message) {
        return Map.of(
                "timestamp", OffsetDateTime.now().toString(),
                "code", code,
                "message", message
        );
    }
}
