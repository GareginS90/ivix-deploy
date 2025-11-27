package am.ivix.api.security;

import am.ivix.api.security.roles.JwtRoleMapper;
import am.ivix.securitycore.jwt.JwtTokenValidator;
import am.ivix.securitycore.store.TokenJtiStore;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenValidator tokenValidator;
    private final JwtRoleMapper roleMapper;
    private final TokenJtiStore tokenJtiStore; // ✅ добавили store для фильтра

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        JwtAuthenticationFilter jwtFilter =
                new JwtAuthenticationFilter(tokenValidator, roleMapper, tokenJtiStore);

        http
                // REST API => без сессий, без CSRF-форм
                .csrf(csrf -> csrf.disable())
                .sessionManagement(cfg -> cfg.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // ✅ CORS включён и берёт конфигурацию из corsConfigurationSource()
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // ✅ Доступы
                .authorizeHttpRequests(auth -> auth
                        // ОТКРЫТЫЕ эндпоинты авторизации
                        .requestMatchers("/api/auth/**").permitAll()
                        // Остальное — только с валидным JWT
                        .anyRequest().authenticated()
                )

                // ✅ Наш JWT-фильтр перед стандартным UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * AuthenticationManager нужен, если где-то будем явно
     * аутентифицировать через Spring Security.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * ✅ CORS-конфигурация по best practices для SPA/мобилки.
     *
     * В проде:
     *  - поменяешь origin'ы на реальные домены (web, admin-panel и т.д.)
     *  - при необходимости разделишь на dev/prod профили.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // ⚠️ На проде обязательно заменить на реальные домены:
        config.setAllowedOrigins(List.of(
                "http://localhost:3000", // React / Next.js dev
                "http://localhost:4200"  // Angular dev (если понадобится)
        ));

        // Какие HTTP-методы разрешены
        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        // Какие заголовки клиент может отправлять
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Cache-Control",
                "Content-Type",
                "X-Requested-With"
        ));

        // Какие заголовки клиент сможет прочитать из ответа
        config.setExposedHeaders(List.of(
                "Authorization"
        ));

        // Разрешаем куки/credentials (на будущее, если понадобится)
        config.setAllowCredentials(true);

        // На сколько кэшировать preflight (OPTIONS)
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}

