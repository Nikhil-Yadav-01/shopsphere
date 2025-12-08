package com.rudraksha.shopsphere.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rudraksha.shopsphere.auth.config.TestConfig;
import com.rudraksha.shopsphere.auth.dto.request.LoginRequest;
import com.rudraksha.shopsphere.auth.dto.request.RefreshTokenRequest;
import com.rudraksha.shopsphere.auth.dto.request.RegisterRequest;
import com.rudraksha.shopsphere.auth.entity.User;
import com.rudraksha.shopsphere.auth.repository.RefreshTokenRepository;
import com.rudraksha.shopsphere.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestConfig.class)
@ActiveProfiles("test")
@DisplayName("AuthController Integration Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        // Clear all data before each test
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        // Create a test user
        User testUser = User.builder()
                .email("test@shopsphere.com")
                .password(passwordEncoder.encode("TestPassword123!"))
                .firstName("Test")
                .lastName("User")
                .role(User.Role.CUSTOMER)
                .enabled(true)
                .build();
        userRepository.save(testUser);

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
    }

    @Test
    @DisplayName("POST /auth/login should return 200 with AuthResponse")
    void testLoginSuccess() throws Exception {
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.refreshToken", notNullValue()))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn", greaterThan(0)))
                .andExpect(jsonPath("$.email").value("test@shopsphere.com"))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"));
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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /auth/login with non-existent email should return 401")
    void testLoginNonExistentEmail() throws Exception {
        LoginRequest invalidRequest = LoginRequest.builder()
                .email("nonexistent@shopsphere.com")
                .password("TestPassword123!")
                .build();

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("POST /auth/login with wrong password should return 401")
    void testLoginWrongPassword() throws Exception {
        LoginRequest invalidRequest = LoginRequest.builder()
                .email("test@shopsphere.com")
                .password("WrongPassword123!")
                .build();

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("POST /auth/register should return 201 with AuthResponse")
    void testRegisterSuccess() throws Exception {
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.refreshToken", notNullValue()))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.email").value("newuser@shopsphere.com"))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("User"))
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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /auth/register with existing email should return 409")
    void testRegisterExistingEmail() throws Exception {
        RegisterRequest duplicateRequest = RegisterRequest.builder()
                .email("test@shopsphere.com")
                .password("TestPassword123!")
                .firstName("Test")
                .lastName("User")
                .build();

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    @DisplayName("POST /auth/refresh should return 200 with TokenResponse")
    void testRefreshTokenSuccess() throws Exception {
        // First login to get a refresh token
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());

        // Get the first refresh token from database
        String firstRefreshToken = refreshTokenRepository.findAll().get(0).getToken();

        RefreshTokenRequest refreshRequest = RefreshTokenRequest.builder()
                .refreshToken(firstRefreshToken)
                .build();

        mockMvc.perform(post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.refreshToken", notNullValue()))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn", greaterThan(0)));
    }

    @Test
    @DisplayName("POST /auth/refresh with missing refreshToken should return 400")
    void testRefreshTokenMissing() throws Exception {
        RefreshTokenRequest request = RefreshTokenRequest.builder().build();

        mockMvc.perform(post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /auth/refresh with invalid token should return 401")
    void testRefreshTokenInvalid() throws Exception {
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("invalid-token")
                .build();

        mockMvc.perform(post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("POST /auth/logout without authentication should return 403")
    void testLogoutRequiresAuthentication() throws Exception {
        // Try to logout without proper authentication
        mockMvc.perform(post("/auth/logout")
                .header("Authorization", "Bearer invalidtoken"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Login endpoint should be public (no auth required)")
    void testLoginIsPublic() throws Exception {
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Register endpoint should be public (no auth required)")
    void testRegisterIsPublic() throws Exception {
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Refresh endpoint should be public")
    void testRefreshIsPublic() throws Exception {
        // First get a valid refresh token
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());

        String refreshToken = refreshTokenRepository.findAll().get(0).getToken();

        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken(refreshToken)
                .build();

        mockMvc.perform(post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Response should contain validation errors for invalid inputs")
    void testValidationErrorResponse() throws Exception {
        RegisterRequest invalidRequest = RegisterRequest.builder()
                .email("invalid")
                .password("123")
                .build();

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors", notNullValue()));
    }

    @Test
    @DisplayName("Multiple logins should create separate refresh tokens")
    void testMultipleLogins() throws Exception {
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());

        int tokenCount = (int) refreshTokenRepository.count();
        int validateTokens = 0;
        for (var token : refreshTokenRepository.findAll()) {
            if (!token.isRevoked()) {
                validateTokens++;
            }
        }
        
        // Should have 2 separate tokens
        org.junit.jupiter.api.Assertions.assertEquals(2, tokenCount);
    }
}
