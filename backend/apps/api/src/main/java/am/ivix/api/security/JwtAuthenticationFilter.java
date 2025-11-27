package am.ivix.api.security;

import am.ivix.api.security.roles.JwtRoleMapper;
import am.ivix.securitycore.jwt.JwtTokenValidator;
import am.ivix.securitycore.jwt.JwtTokenValidator.TokenKind;
import am.ivix.securitycore.store.TokenJtiStore;
import com.nimbusds.jwt.JWTClaimsSet;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Production-level JWT authentication filter.
 *
 * Responsibilities:
 *  - safely extract JWT from Authorization header
 *  - validate signature, issuer, audience, exp, nbf, typ
 *  - check access token blacklist (JTI)
 *  - map roles → Spring authorities
 *  - set Authentication into SecurityContext
 *
 * This filter NEVER throws exceptions — silent fail is best practice
 * for stateless JWT authentication chains.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenValidator validator;
    private final JwtRoleMapper roleMapper;
    private final TokenJtiStore tokenJtiStore;

    public JwtAuthenticationFilter(JwtTokenValidator validator,
                                   JwtRoleMapper roleMapper,
                                   TokenJtiStore tokenJtiStore) {
        this.validator = validator;
        this.roleMapper = roleMapper;
        this.tokenJtiStore = tokenJtiStore;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7).trim();
        if (token.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Validate access token
        JwtTokenValidator.ValidationResult result =
                validator.validate(token, TokenKind.ACCESS);

        if (!result.ok) {
            filterChain.doFilter(request, response);
            return;
        }

        JWTClaimsSet claims = result.claims;

        String jti = claims.getJWTID();
        if (jti == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // BLACKLIST CHECK (REVOKED ACCESS CONTROL)
        if (tokenJtiStore.isAccessBlacklisted(jti)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract user data
        String userId = claims.getSubject();
        String email = (String) claims.getClaim("email");

        Object rolesObj = claims.getClaim("roles");
        if (!(rolesObj instanceof List<?> rawList)) {
            filterChain.doFilter(request, response);
            return;
        }

        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) rawList;

        var authorities = roleMapper.mapToAuthorities(roles);

        // Create Authentication
        var authentication = new UsernamePasswordAuthenticationToken(
                userId,    // principal (userId)
                null,      // no credentials
                authorities
        );

        authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );

        // Apply into Security Context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}

