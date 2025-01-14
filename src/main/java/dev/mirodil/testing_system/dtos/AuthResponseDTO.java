package dev.mirodil.testing_system.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;


public class AuthResponseDTO {
    @JsonProperty("user")
    private final UserResponseDTO userDTO;
    @JsonProperty("token")
    private final Map<String, Object> jwtTokenDetails;

    public AuthResponseDTO(UserResponseDTO userDTO, Map<String, Object> jwtTokenDetails) {
        this.userDTO = userDTO;
        this.jwtTokenDetails = jwtTokenDetails;
    }

    public UserResponseDTO getUserDTO() {
        return userDTO;
    }
}
