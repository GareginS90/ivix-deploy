package am.ivix.api.config;

import am.ivix.api.security.JwtAuthFilter;
import am.ivix.api.security.JwtService;
import am.ivix.api.security.KeyProvider;
import am.ivix.api.security.DbUserDetailsService;
import am.ivix.users.repo.UserRepository;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public DbUserDetailsService uds(UserRepository users) {
        return new DbUserDetailsService(users);
    }

    @Bean
    public AuthenticationManager authenticationManager(DbUserDetailsService uds,
                                                       PasswordEncoder encoder) {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(uds);
        p.setPasswordEncoder(encoder);
        return new ProviderManager(p);
    }

    @Bean
    public KeyProvider keyProvider(
            @Value("${security.jwt.key.public-classpath}") String pub,
            @Value("${security.jwt.key.private-classpath}") String priv
    ) {
        return new KeyProvider(pub, priv);
    }

    @Bean
    public SecurityFilterChain chain(HttpSecurity http,
                                     JwtAuthFilter jwtAuthFilter) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/api/auth/register",
                        "/api/auth/login",
                        "/api/auth/refresh",
                        "/actuator/health"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

