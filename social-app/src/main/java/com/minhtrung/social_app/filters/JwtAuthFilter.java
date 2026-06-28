package com.minhtrung.social_app.filters;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.minhtrung.social_app.common.context.RequestContext;
import com.minhtrung.social_app.common.context.RequestContextHolder;
import com.minhtrung.social_app.dtos.ClientInfo;
import com.minhtrung.social_app.services.ClientInfoService;
import com.minhtrung.social_app.services.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final ClientInfoService clientInfoService;

    @Value("${jwt.secret.access}")
    private String secretAccessKey;

    private static final List<String> PROTECTED_PATHS = List.of(
        "/api/v1/posts",
        "/api/v1/post-reaction",
        "/api/v1/comments",
        "/api/v1/comment-reaction",
        "/api/v1/shares",
        "/api/v1/friends"
    );

    JwtAuthFilter(JwtService jwtService, ClientInfoService clientInfoService) {
        this.jwtService = jwtService;
        this.clientInfoService = clientInfoService;
    }

    private boolean isProtectedPath(String path) {
        return PROTECTED_PATHS.stream().anyMatch(path::startsWith);
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);
        
        ClientInfo clientInfo = clientInfoService.getClientInfo(request);
        
        RequestContext.RequestContextBuilder builder = RequestContext.builder()
            .deviceType(clientInfo.getDeviceType())
            .deviceOS(clientInfo.getDeviceOS())
            .browser(clientInfo.getBrowser())
            .ipAddr(clientInfo.getIpAddr())
            .country(clientInfo.getCountry())
            .city(clientInfo.getCity());
        
        String path = request.getRequestURI();

        
        if (isProtectedPath(path)) {
            String header = request.getHeader("Authorization");

            if (header == null || !header.startsWith("Bearer ")) {
                response.setStatus(401);
                response.getWriter().write("Invalid Header or Missing Token");
                return;
            }

            String token = header.substring(7);

            try {
                if (jwtService.isTokenValid(token, secretAccessKey)) {
                    Map<String, Object> payload = jwtService.extractAll(token, secretAccessKey);

                    UUID userId = UUID.fromString((String) payload.get("sub"));
                    UUID sessionId = UUID.fromString((String) payload.get("sessionId"));
                    UUID deviceIdFromToken = UUID.fromString((String) payload.get("deviceId"));
                    builder.userId(userId).sessionId(sessionId).deviceId(deviceIdFromToken);
                } else {
                    response.setStatus(401);
                    response.getWriter().write("Invalid Token");
                    return;
                }
            } catch (Exception ex) {
                response.setStatus(500);
                response.getWriter().write("Server Error");
                return;
            } 
        }
        
        RequestContext ctx = builder.build();
        RequestContextHolder.set(ctx);

        log.debug("RequestContext: {}", RequestContextHolder.get());


        try {
            filterChain.doFilter(request, response);
        } finally {
            RequestContextHolder.clear();
            MDC.clear();
        }
    }
}
