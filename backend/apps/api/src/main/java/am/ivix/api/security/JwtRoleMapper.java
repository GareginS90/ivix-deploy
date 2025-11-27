package am.ivix.api.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class JwtRoleMapper {

    public static List<SimpleGrantedAuthority> toAuthorities(List<String> roles) {
        return roles.stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                .collect(Collectors.toList());
    }
}
