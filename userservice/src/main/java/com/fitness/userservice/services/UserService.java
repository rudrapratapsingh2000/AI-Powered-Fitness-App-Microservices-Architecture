package com.fitness.userservice.services;

import com.fitness.userservice.dto.RegisterUser;
import com.fitness.userservice.dto.UserResponse;
import com.fitness.userservice.models.User;
import com.fitness.userservice.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public UserResponse register(RegisterUser registerUser) {

        if (userRepository.existsByEmail(registerUser.getEmail())) {
//            throw new RuntimeException("Email already exist");
            log.info("User already exist");
            User existingUser = userRepository.findByEmail(registerUser.getEmail());
            UserResponse userResponse = new UserResponse();
            userResponse.setId(existingUser.getId());
            userResponse.setEmail(existingUser.getEmail());
            userResponse.setFirstname(existingUser.getFirstname());
            userResponse.setLastname(existingUser.getLastname());
            userResponse.setPassword(existingUser.getPassword());
            userResponse.setCreatedAt(existingUser.getCreatedAt());
            userResponse.setUpdatedAt(existingUser.getUpdatedAt());
            userResponse.setKeycloakId(existingUser.getKeycloakId());
            return userResponse;
        }
        User user = new User();
        user.setEmail(registerUser.getEmail());
        user.setFirstname(registerUser.getFirstname());
        user.setPassword(registerUser.getPassword());
        user.setLastname(registerUser.getLastname());
        user.setKeycloakId(registerUser.getKeycloakId());

        User savedUser = userRepository.save(user);

        UserResponse userResponse = new UserResponse();
        userResponse.setId(savedUser.getId());
        userResponse.setEmail(savedUser.getEmail());
        userResponse.setFirstname(savedUser.getFirstname());
        userResponse.setLastname(savedUser.getLastname());
        userResponse.setPassword(savedUser.getPassword());
        userResponse.setCreatedAt(savedUser.getCreatedAt());
        userResponse.setUpdatedAt(savedUser.getUpdatedAt());
        userResponse.setKeycloakId(savedUser.getKeycloakId());
        return userResponse;
    }

    public UserResponse getUserProfile(String userId) {

        User savedUser = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        UserResponse userResponse = new UserResponse();
        userResponse.setId(savedUser.getId());
        userResponse.setKeycloakId(savedUser.getKeycloakId());
        userResponse.setEmail(savedUser.getEmail());
        userResponse.setFirstname(savedUser.getFirstname());
        userResponse.setLastname(savedUser.getLastname());
        userResponse.setPassword(savedUser.getPassword());
        userResponse.setCreatedAt(savedUser.getCreatedAt());
        userResponse.setUpdatedAt(savedUser.getUpdatedAt());

        return userResponse;
    }

    public Boolean existByUserId(String userId) {
        log.info("Calling user service for {}", userId);
//        return userRepository.existsById(userId);
        return userRepository.existsByKeycloakId(userId);
    }
}
