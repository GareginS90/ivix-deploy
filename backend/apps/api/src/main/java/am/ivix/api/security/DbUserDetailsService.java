package am.ivix.api.security;

import am.ivix.users.repo.UserRepository;
import am.ivix.users.domain.User;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.UUID;

public class DbUserDetailsService implements UserDetailsService {

    private final UserRepository users;

    public DbUserDetailsService(UserRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // username может быть email — используется при логине
        User u = users.findByEmailIgnoreCase(username).orElseThrow(
                () -> new UsernameNotFoundException("User not found")
        );
        return toDetails(u);
    }

    public UserDetails loadUserById(UUID id) throws UsernameNotFoundException {
        User u = users.findById(id).orElseThrow(
                () -> new UsernameNotFoundException("User not found")
        );
        return toDetails(u);
    }

    private UserDetails toDetails(User u) {
        // Роли/привилегии можешь достроить позже; пока — USER
        return new org.springframework.security.core.userdetails.User(
                u.getEmail(),
                u.getPasswordHash(),
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
