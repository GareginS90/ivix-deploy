package am.ivix.users.app.exception;

public class UserNotFoundException extends RuntimeException {

    private final String email;

    public UserNotFoundException(String email) {
        super("User not found: " + email);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
