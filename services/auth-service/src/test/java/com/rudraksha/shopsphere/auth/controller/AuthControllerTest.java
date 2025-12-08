package com.rudraksha.shopsphere.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rudraksha.shopsphere.auth.dto.request.LoginRequest;
import com.rudraksha.shopsphere.auth.dto.request.RefreshTokenRequest;
import com.rudraksha.shopsphere.auth.dto.request.RegisterRequest;
import com.rudraksha.shopsphere.auth.dto.response.AuthResponse;
import com.rudraksha.shopsphere.auth.dto.response.TokenResponse;
import com.rudraksha.shopsphere.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@DisplayName("AuthController Unit Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;
    private AuthResponse authResponse;
    private TokenResponse tokenResponse;

    @BeforeEach
    void setUp() {
        loginRequest = LoginRequest.builder()
                .email("test@shopsphere.com")
                .password("TestPassword123!")
                .build();

        registerRequest = RegisterRequest.builder()
                .email("newuser@shopsphere.com")
                .password("TestPassword123!")
                .firstName("Test")
                .lastName("User")
                .build();

        authResponse = AuthResponse.builder()
                .accessToken("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIn0.abc")
                .refreshToken("refresh-token-123")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .email("test@shopsphere.com")
                .firstName("Test")
                .lastName("User")
                .role("CUSTOMER")
                .build();

        tokenResponse = TokenResponse.builder()
                .accessToken("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIn0.abc")
                .refreshToken("new-refresh-token")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .build();
    }

    @Test
    @DisplayName("POST /auth/login should return 200 with AuthResponse")
    void testLoginSuccess() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.refreshToken", notNullValue()))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.email").value("test@shopsphere.com"));
    }

    @Test
    @DisplayName("POST /auth/login with invalid email should return 400")
    void testLoginInvalidEmail() throws Exception {
        LoginRequest invalidRequest = LoginRequest.builder()
                .email("invalid-email")
                .password("TestPassword123!")
                .build();

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/login with missing email should return 400")
    void testLoginMissingEmail() throws Exception {
        LoginRequest invalidRequest = LoginRequest.builder()
                .password("TestPassword123!")
                .build();

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/login with missing password should return 400")
    void testLoginMissingPassword() throws Exception {
        LoginRequest invalidRequest = LoginRequest.builder()
                .email("test@shopsphere.com")
                .build();

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/register should return 201 with AuthResponse")
    void testRegisterSuccess() throws Exception {
        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.refreshToken", notNullValue()))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    @DisplayName("POST /auth/register with invalid email should return 400")
    void testRegisterInvalidEmail() throws Exception {
        RegisterRequest invalidRequest = RegisterRequest.builder()
                .email("invalid-email")
                .password("TestPassword123!")
                .firstName("Test")
                .lastName("User")
                .build();

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/register with short password should return 400")
    void testRegisterShortPassword() throws Exception {
        RegisterRequest invalidRequest = RegisterRequest.builder()
                .email("test@shopsphere.com")
                .password("Short1")
                .firstName("Test")
                .lastName("User")
                .build();

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/register with missing firstName should return 400")
    void testRegisterMissingFirstName() throws Exception {
        RegisterRequest invalidRequest = RegisterRequest.builder()
                .email("test@shopsphere.com")
                .password("TestPassword123!")
                .lastName("User")
                .build();

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/register with missing lastName should return 400")
    void testRegisterMissingLastName() throws Exception {
        RegisterRequest invalidRequest = RegisterRequest.builder()
                .email("test@shopsphere.com")
                .password("TestPassword123!")
                .firstName("Test")
                .build();

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/refresh should return 200 with TokenResponse")
    void testRefreshTokenSuccess() throws Exception {
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("valid-refresh-token")
                .build();

        when(authService.refreshToken(any(RefreshTokenRequest.class))).thenReturn(tokenResponse);

        mockMvc.perform(post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.refreshToken", notNullValue()))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("POST /auth/refresh with missing refreshToken should return 400")
    void testRefreshTokenMissing() throws Exception {
        RefreshTokenRequest request = RefreshTokenRequest.builder().build();

        mockMvc.perform(post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/logout should return 204")
    void testLogoutSuccess() throws Exception {
        doNothing().when(authService).logout("Bearer eyJhbGciOiJIUzI1NiJ9");

        mockMvc.perform(post("/auth/logout")
                .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Login endpoint should be public (no auth required)")
    void testLoginIsPublic() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Register endpoint should be public (no auth required)")
    void testRegisterIsPublic() throws Exception {
        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Refresh endpoint should be public")
    void testRefreshIsPublic() throws Exception {
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("valid-refresh-token")
                .build();

        when(authService.refreshToken(any(RefreshTokenRequest.class))).thenReturn(tokenResponse);

        mockMvc.perform(post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
