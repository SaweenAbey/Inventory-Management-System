package com.Inventory.Backend.dto;

import com.Inventory.Backend.entity.Role;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    private String username;

    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    private String imageUrl;

    private Role role; // Optional - will default to USER if not provided

}
