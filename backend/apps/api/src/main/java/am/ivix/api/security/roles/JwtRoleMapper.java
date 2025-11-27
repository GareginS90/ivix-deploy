package am.ivix.api.security.roles;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Production-grade mapper from business roles to Spring Security authorities.
 */
@Component
public class JwtRoleMapper {

    /**
     * Преобразует список ролей (USER, PROVIDER, ADMIN)
     * в authority формата ROLE_USER, ROLE_PROVIDER, ROLE_ADMIN
     */
    public List<GrantedAuthority> mapToAuthorities(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }

        return roles.stream()
                .map(String::toUpperCase)
                .map(r -> "ROLE_" + r)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}

