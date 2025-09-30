package com.fitness.gateway.userService.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterUser {

    private String keycloakId;
    @NotBlank(message = "Email is Required")
    @Email(message = "Invalid email format")
    private String email;
    @NotBlank(message = "Password is Required")
    @Size(min = 6, message = "Password must have atleast 6 characters")
    private String password;
    private String firstname;
    private String lastname;

}
