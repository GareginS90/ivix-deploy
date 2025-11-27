package am.ivix.api.web.error;

import am.ivix.users.app.exception.EmailAlreadyTakenException;
import am.ivix.users.app.exception.InvalidCredentialsException;
import am.ivix.users.app.exception.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyTakenException.class)
    public ResponseEntity<ApiErrorResponse> handleEmailAlreadyTaken(EmailAlreadyTakenException ex) {
        ApiErrorResponse body = new ApiErrorResponse(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                "EMAIL_ALREADY_TAKEN",
                ex.getMessage(),
                null
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        ApiErrorResponse body = new ApiErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "INVALID_CREDENTIALS",
                ex.getMessage(),
                null
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        ApiErrorResponse body = new ApiErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                "USER_NOT_FOUND",
                ex.getMessage(),
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        BindingResult bindingResult = ex.getBindingResult();

        List<ApiErrorResponse.FieldError> fieldErrors = bindingResult.getFieldErrors()
                .stream()
                .map(fe -> new ApiErrorResponse.FieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();

        ApiErrorResponse body = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "VALIDATION_ERROR",
                "Request validation failed",
                fieldErrors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // fallback, чтобы не падать 500 без структуры
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex) {
        ApiErrorResponse body = new ApiErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "INTERNAL_ERROR",
                ex.getMessage(),
                null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
