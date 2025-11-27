package am.ivix.api.security;

import am.ivix.securitycore.jwt.JwtTokenValidator;
import com.nimbusds.jwt.JWTClaimsSet;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenValidator validator;

    public JwtAuthFilter(JwtTokenValidator validator) {
        this.validator = validator;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain)
            throws ServletException, IOException {

        String header = req.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            JwtTokenValidator.ValidationResult result =
                    validator.validate(token, JwtTokenValidator.TokenKind.ACCESS);

            if (result.ok && result.claims != null) {
                JWTClaimsSet claims = result.claims;
                String userId = claims.getSubject();

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));

                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        chain.doFilter(req, res);
    }
}

