package com.fitness.gateway.filter;

import com.fitness.gateway.userService.dto.RegisterUser;
import com.fitness.gateway.userService.service.UserService;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class KeycloakUserSyncFilter implements WebFilter {

    private final UserService userService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-ID");
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
        RegisterUser registerUser = getUserDetails(token);
        if (userId == null) {
            userId = registerUser.getKeycloakId();
        }
        if (userId != null && token != null) {
            String finalUserId = userId;
            return userService.validateUser(userId).flatMap(exist -> {
                if (!exist) {
                    if (registerUser != null) {
                        return userService.registerUser(registerUser)
                                .then(Mono.empty());
                    } else {
                        return Mono.empty();
                    }
                } else {
                    log.info("User already exist, skipping sync");
                    return Mono.empty();
                }
//                return null;
            }).then(Mono.defer(() -> {
                ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                        .header("X-User-Id", finalUserId).build();
                return chain.filter(exchange.mutate().request(mutatedRequest).build());
            }));
        }
        return chain.filter(exchange);
    }

    private RegisterUser getUserDetails(String token) {
        try {
            String tokenWithoutBearer = token.replace("Bearer", "").trim();
            SignedJWT signedJWT = SignedJWT.parse(tokenWithoutBearer);
            JWTClaimsSet jwtClaimsSet = signedJWT.getJWTClaimsSet();
            RegisterUser registerUser = new RegisterUser();
            registerUser.setEmail(jwtClaimsSet.getStringClaim("email"));
            registerUser.setKeycloakId(jwtClaimsSet.getStringClaim("sub"));
            registerUser.setPassword("dummy@123");
            registerUser.setFirstname(jwtClaimsSet.getStringClaim("given_name"));
            registerUser.setLastname(jwtClaimsSet.getStringClaim("family_name"));
            return registerUser;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
