package am.ivix.users.app;

import am.ivix.users.domain.User;
import am.ivix.users.repo.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(String email, String rawPassword) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new RuntimeException("Email already taken");
        }

        String hash = passwordEncoder.encode(rawPassword);

        User user = new User(
                email,
                hash,
                List.of("ROLE_USER")    // дефолтная роль
        );

        return userRepository.save(user);
    }

    public User login(String email, String rawPassword) {
        return userRepository.findByEmailIgnoreCase(email)
                .filter(u -> passwordEncoder.matches(rawPassword, u.getPasswordHash()))
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}

