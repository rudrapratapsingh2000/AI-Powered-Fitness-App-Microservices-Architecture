package com.fitness.gateway.userService.service;

import com.fitness.gateway.userService.dto.RegisterUser;
import com.fitness.gateway.userService.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final WebClient userServiceWebClient;

    public Mono<Boolean> validateUser(String userId) {
        log.info("Calling user service for {}", userId);
        return userServiceWebClient.get()
                .uri("/api/users/{userId}/validate", userId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .onErrorResume(WebClientResponseException.class, e -> {
                    if (e.getStatusCode() == HttpStatus.NOT_FOUND)
                        return Mono.error(new RuntimeException("User not found : " + userId));
                    else if (e.getStatusCode() == HttpStatus.BAD_REQUEST)
                        return Mono.error(new RuntimeException("Invalid : " + userId));

                    return Mono.error(new RuntimeException("Unexpected error : " + userId));
                });
    }

    public Mono<UserResponse> registerUser(RegisterUser registerUser) {
        log.info("Calling User Registration for {} " + registerUser.getEmail());
        return userServiceWebClient.post()
                .uri("/api/users/register")
                .bodyValue(registerUser)
                .retrieve()
                .bodyToMono(UserResponse.class)
                .onErrorResume(WebClientResponseException.class, e -> {
                    if (e.getStatusCode() == HttpStatus.BAD_REQUEST)
                        return Mono.error(new RuntimeException("Bad request : " + e.getMessage()));

                    return Mono.error(new RuntimeException("Unexpected error : " + e.getMessage()));
                });
    }

}
