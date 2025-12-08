package com.rudraksha.shopsphere.auth.service.impl;

import com.rudraksha.shopsphere.auth.config.TestConfig;
import com.rudraksha.shopsphere.auth.dto.request.LoginRequest;
import com.rudraksha.shopsphere.auth.dto.request.RefreshTokenRequest;
import com.rudraksha.shopsphere.auth.dto.request.RegisterRequest;
import com.rudraksha.shopsphere.auth.dto.response.AuthResponse;
import com.rudraksha.shopsphere.auth.dto.response.TokenResponse;
import com.rudraksha.shopsphere.auth.entity.RefreshToken;
import com.rudraksha.shopsphere.auth.entity.User;
import com.rudraksha.shopsphere.auth.exception.AuthException;
import com.rudraksha.shopsphere.auth.exception.UserAlreadyExistsException;
import com.rudraksha.shopsphere.auth.repository.RefreshTokenRepository;
import com.rudraksha.shopsphere.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({TestConfig.class, AuthServiceImpl.class})
@ActiveProfiles("test")
@DisplayName("AuthServiceImpl Integration Tests")
class AuthServiceImplTest {

    @Autowired
    private AuthServiceImpl authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        // Clear all data before each test
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        // Create a test user
        String rawPassword = "TestPassword123!";
        testUser = User.builder()
                .email("test@shopsphere.com")
                .password(passwordEncoder.encode(rawPassword))
                .firstName("Test")
                .lastName("User")
                .role(User.Role.CUSTOMER)
                .enabled(true)
                .build();
        testUser = userRepository.save(testUser);

        loginRequest = LoginRequest.builder()
                .email("test@shopsphere.com")
                .password(rawPassword)
                .build();

        registerRequest = RegisterRequest.builder()
                .email("newuser@shopsphere.com")
                .password("NewPassword123!")
                .firstName("New")
                .lastName("User")
                .build();
    }

    @Test
    @DisplayName("Login with valid credentials should return AuthResponse")
    void testLoginSuccess() {
        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(3600L, response.getExpiresIn());
        assertEquals(testUser.getEmail(), response.getEmail());
        assertEquals(testUser.getFirstName(), response.getFirstName());
        assertEquals(testUser.getLastName(), response.getLastName());
        assertEquals("CUSTOMER", response.getRole());
    }

    @Test
    @DisplayName("Login with non-existent email should throw AuthException")
    void testLoginUserNotFound() {
        LoginRequest invalidRequest = LoginRequest.builder()
                .email("nonexistent@shopsphere.com")
                .password("TestPassword123!")
                .build();

        AuthException exception = assertThrows(AuthException.class, () -> {
            authService.login(invalidRequest);
        });

        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    @DisplayName("Login with incorrect password should throw AuthException")
    void testLoginInvalidPassword() {
        LoginRequest invalidRequest = LoginRequest.builder()
                .email("test@shopsphere.com")
                .password("WrongPassword123!")
                .build();

        AuthException exception = assertThrows(AuthException.class, () -> {
            authService.login(invalidRequest);
        });

        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    @DisplayName("Login with disabled user should throw AuthException")
    void testLoginDisabledUser() {
        testUser.setEnabled(false);
        userRepository.save(testUser);

        AuthException exception = assertThrows(AuthException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Account is disabled", exception.getMessage());
    }

    @Test
    @DisplayName("Register with valid request should return AuthResponse and persist user")
    void testRegisterSuccess() {
        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(registerRequest.getEmail(), response.getEmail());
        assertEquals("CUSTOMER", response.getRole());

        // Verify user is persisted in database
        assertTrue(userRepository.existsByEmail(registerRequest.getEmail()));
        User savedUser = userRepository.findByEmail(registerRequest.getEmail()).orElse(null);
        assertNotNull(savedUser);
        assertEquals(registerRequest.getFirstName(), savedUser.getFirstName());
        assertEquals(registerRequest.getLastName(), savedUser.getLastName());
        assertTrue(savedUser.isEnabled());
    }

    @Test
    @DisplayName("Register with existing email should throw UserAlreadyExistsException")
    void testRegisterUserAlreadyExists() {
        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, () -> {
            authService.register(RegisterRequest.builder()
                    .email("test@shopsphere.com")  // Existing user email
                    .password("TestPassword123!")
                    .firstName("New")
                    .lastName("User")
                    .build());
        });

        assertEquals("Email already registered", exception.getMessage());
    }

    @Test
    @DisplayName("Register should create user with CUSTOMER role by default")
    void testRegisterCreatesCustomerRole() {
        authService.register(registerRequest);

        User savedUser = userRepository.findByEmail(registerRequest.getEmail()).orElse(null);
        assertNotNull(savedUser);
        assertEquals(User.Role.CUSTOMER, savedUser.getRole());
    }

    @Test
    @DisplayName("Register should encode password correctly")
    void testRegisterEncodesPassword() {
        authService.register(registerRequest);

        User savedUser = userRepository.findByEmail(registerRequest.getEmail()).orElse(null);
        assertNotNull(savedUser);
        assertNotEquals(registerRequest.getPassword(), savedUser.getPassword());
        assertTrue(passwordEncoder.matches(registerRequest.getPassword(), savedUser.getPassword()));
    }

    @Test
    @DisplayName("Refresh token with valid token should return new TokenResponse")
    void testRefreshTokenSuccess() {
        // Create a valid refresh token
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(testUser)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();
        refreshToken = refreshTokenRepository.save(refreshToken);

        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken(refreshToken.getToken())
                .build();

        TokenResponse response = authService.refreshToken(request);

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(3600L, response.getExpiresIn());

        // Verify old token is revoked
        RefreshToken oldToken = refreshTokenRepository.findByToken(refreshToken.getToken()).orElse(null);
        assertNotNull(oldToken);
        assertTrue(oldToken.isRevoked());
    }

    @Test
    @DisplayName("Refresh token with invalid token should throw AuthException")
    void testRefreshTokenInvalid() {
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("nonexistent-token")
                .build();

        AuthException exception = assertThrows(AuthException.class, () -> {
            authService.refreshToken(request);
        });

        assertEquals("Invalid refresh token", exception.getMessage());
    }

    @Test
    @DisplayName("Refresh token with expired token should throw AuthException")
    void testRefreshTokenExpired() {
        RefreshToken expiredToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(testUser)
                .expiresAt(LocalDateTime.now().minusDays(1))
                .revoked(false)
                .build();
        expiredToken = refreshTokenRepository.save(expiredToken);

        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken(expiredToken.getToken())
                .build();

        AuthException exception = assertThrows(AuthException.class, () -> {
            authService.refreshToken(request);
        });

        assertEquals("Refresh token is expired or revoked", exception.getMessage());
    }

    @Test
    @DisplayName("Refresh token with revoked token should throw AuthException")
    void testRefreshTokenRevoked() {
        RefreshToken revokedToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(testUser)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .revoked(true)
                .build();
        revokedToken = refreshTokenRepository.save(revokedToken);

        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken(revokedToken.getToken())
                .build();

        AuthException exception = assertThrows(AuthException.class, () -> {
            authService.refreshToken(request);
        });

        assertEquals("Refresh token is expired or revoked", exception.getMessage());
    }

    @Test
    @DisplayName("Logout should revoke the refresh token")
    void testLogout() {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(testUser)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();
        refreshToken = refreshTokenRepository.save(refreshToken);

        authService.logout(refreshToken.getToken());

        RefreshToken revokedToken = refreshTokenRepository.findByToken(refreshToken.getToken()).orElse(null);
        assertNotNull(revokedToken);
        assertTrue(revokedToken.isRevoked());
    }

    @Test
    @DisplayName("Logout with non-existent token should not throw exception")
    void testLogoutNonExistentToken() {
        assertDoesNotThrow(() -> authService.logout("nonexistent-token"));
    }

    @Test
    @DisplayName("JWT token should contain correct claims")
    void testJWTTokenClaims() {
        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response.getAccessToken());
        assertTrue(response.getAccessToken().startsWith("eyJ"));
        // JWT tokens are in format: header.payload.signature
        String[] parts = response.getAccessToken().split("\\.");
        assertEquals(3, parts.length);
    }

    @Test
    @DisplayName("Refresh token should have correct expiration")
    void testRefreshTokenExpiration() {
        AuthResponse response = authService.login(loginRequest);

        RefreshToken refreshToken = refreshTokenRepository.findByToken(response.getRefreshToken()).orElse(null);
        assertNotNull(refreshToken);
        assertTrue(refreshToken.getExpiresAt().isAfter(LocalDateTime.now()));
    }

    @Test
    @DisplayName("Refresh token creates new valid token after refresh")
    void testRefreshTokenCreatesValidNewToken() {
        RefreshToken initialToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(testUser)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();
        initialToken = refreshTokenRepository.save(initialToken);

        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken(initialToken.getToken())
                .build();

        TokenResponse response = authService.refreshToken(request);

        RefreshToken newToken = refreshTokenRepository.findByToken(response.getRefreshToken()).orElse(null);
        assertNotNull(newToken);
        assertFalse(newToken.isRevoked());
        assertTrue(newToken.getExpiresAt().isAfter(LocalDateTime.now()));
    }

    @Test
    @DisplayName("Multiple logins should create separate refresh tokens")
    void testMultipleLoginsCreateSeparateTokens() {
        AuthResponse response1 = authService.login(loginRequest);
        AuthResponse response2 = authService.login(loginRequest);

        assertNotEquals(response1.getRefreshToken(), response2.getRefreshToken());
        assertTrue(refreshTokenRepository.findByToken(response1.getRefreshToken()).isPresent());
        assertTrue(refreshTokenRepository.findByToken(response2.getRefreshToken()).isPresent());
    }

    @Test
    @DisplayName("Successfully logged out token should not be usable for refresh")
    void testLoggedOutTokenCannotBeRefreshed() {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(testUser)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();
        refreshToken = refreshTokenRepository.save(refreshToken);

        // Logout
        authService.logout(refreshToken.getToken());

        // Try to refresh
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken(refreshToken.getToken())
                .build();

        AuthException exception = assertThrows(AuthException.class, () -> {
            authService.refreshToken(request);
        });

        assertEquals("Refresh token is expired or revoked", exception.getMessage());
    }
}
