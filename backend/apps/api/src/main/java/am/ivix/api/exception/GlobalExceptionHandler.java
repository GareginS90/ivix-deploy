package am.ivix.api.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Ошибки валидации DTO ------------------------------------------------ */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {

        Map<String, Object> details = ex.getBindingResult().getFieldErrors().stream()
                .collect(
                        java.util.stream.Collectors.toMap(
                                err -> err.getField(),
                                err -> err.getDefaultMessage(),
                                (a, b) -> b
                        )
                );

        ErrorResponse resp = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Error",
                "Invalid request body",
                request.getRequestURI(),
                details
        );

        return ResponseEntity.badRequest().body(resp);
    }

    /** Наши бизнес-ошибки -------------------------------------------------- */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(
            RuntimeException ex,
            HttpServletRequest request
    ) {
        ErrorResponse resp = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(resp);
    }

    /** fallback — всё, что мы не перехватили ------------------------------- */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception ex,
            HttpServletRequest request
    ) {
        ErrorResponse resp = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "Unexpected error occurred",
                request.getRequestURI()
        );

        ex.printStackTrace(); // можно заменить на логгер

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
    }
}
