package am.ivix.users.app.exception;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}
