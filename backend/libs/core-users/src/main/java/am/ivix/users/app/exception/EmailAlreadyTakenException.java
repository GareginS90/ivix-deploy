package am.ivix.users.app.exception;

public class EmailAlreadyTakenException extends RuntimeException {

    private final String email;

    public EmailAlreadyTakenException(String email) {
        super("Email already taken: " + email);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
