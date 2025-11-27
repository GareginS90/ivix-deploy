package am.ivix.users.app;

import am.ivix.users.domain.User;
import am.ivix.users.repo.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public boolean exists(String email) {
        return userRepository.existsByEmailIgnoreCase(email);
    }

    public User createUser(String email, String passwordHash, List<String> roles) {
        User user = new User(email, passwordHash, roles);
        return userRepository.save(user);
    }
}
