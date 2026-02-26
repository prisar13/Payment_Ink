package com.service;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ServiceAuthFilter extends OncePerRequestFilter {

    private static final String SERVICE_SECRET = "my-service-secret-key-my-service-secret-key";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        String token = authHeader.substring(7);
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(SERVICE_SECRET.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();
        String issuer = claims.getIssuer();
        String audience = claims.getAudience();
        String role = claims.get("role", String.class);
        if (!"service".equals(role) ||
                !"payment-service".equals(issuer) ||
                !"fraud-service".equals(audience)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "service-user",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_SERVICE")));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
        filterChain.doFilter(request, response);
    }

}
