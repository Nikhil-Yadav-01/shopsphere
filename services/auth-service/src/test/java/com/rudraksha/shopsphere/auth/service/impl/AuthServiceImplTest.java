package com.rudraksha.shopsphere.auth.service.impl;

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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Unit Tests")
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "jwtSecret", "mySecretKeyForJWTTokenGenerationMustBeAtLeast256BitsLong");
        ReflectionTestUtils.setField(authService, "jwtExpirationMs", 3600000L);
        ReflectionTestUtils.setField(authService, "refreshExpirationMs", 604800000L);

        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@shopsphere.com")
                .password("hashedPassword")
                .firstName("Test")
                .lastName("User")
                .role(User.Role.CUSTOMER)
                .enabled(true)
                .build();

        loginRequest = LoginRequest.builder()
                .email("test@shopsphere.com")
                .password("TestPassword123!")
                .build();

        registerRequest = RegisterRequest.builder()
                .email("newuser@shopsphere.com")
                .password("TestPassword123!")
                .firstName("New")
                .lastName("User")
                .build();
    }

    @Test
    @DisplayName("Login with valid credentials should return AuthResponse")
    void testLoginSuccess() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(true);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> {
            RefreshToken token = invocation.getArgument(0);
            token.setId(UUID.randomUUID());
            return token;
        });

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(3600L, response.getExpiresIn());
        assertEquals(testUser.getEmail(), response.getEmail());
        assertEquals(testUser.getFirstName(), response.getFirstName());
        assertEquals(testUser.getLastName(), response.getLastName());

        verify(userRepository, times(1)).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder, times(1)).matches(loginRequest.getPassword(), testUser.getPassword());
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Login with non-existent email should throw AuthException")
    void testLoginUserNotFound() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        AuthException exception = assertThrows(AuthException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Invalid email or password", exception.getMessage());
        verify(userRepository, times(1)).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    @DisplayName("Login with incorrect password should throw AuthException")
    void testLoginInvalidPassword() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(false);

        AuthException exception = assertThrows(AuthException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Invalid email or password", exception.getMessage());
        verify(userRepository, times(1)).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder, times(1)).matches(loginRequest.getPassword(), testUser.getPassword());
    }

    @Test
    @DisplayName("Login with disabled user should throw AuthException")
    void testLoginDisabledUser() {
        testUser.setEnabled(false);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(true);

        AuthException exception = assertThrows(AuthException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Account is disabled", exception.getMessage());
    }

    @Test
    @DisplayName("Register with valid request should return AuthResponse")
    void testRegisterSuccess() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> {
            RefreshToken token = invocation.getArgument(0);
            token.setId(UUID.randomUUID());
            return token;
        });

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(registerRequest.getEmail(), response.getEmail());
        assertEquals("CUSTOMER", response.getRole());

        verify(userRepository, times(1)).existsByEmail(registerRequest.getEmail());
        verify(passwordEncoder, times(1)).encode(registerRequest.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Register with existing email should throw UserAlreadyExistsException")
    void testRegisterUserAlreadyExists() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("Email already registered", exception.getMessage());
        verify(userRepository, times(1)).existsByEmail(registerRequest.getEmail());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("Register should create user with CUSTOMER role by default")
    void testRegisterCreatesCustomerRole() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> {
            RefreshToken token = invocation.getArgument(0);
            token.setId(UUID.randomUUID());
            return token;
        });

        authService.register(registerRequest);

        verify(userRepository).save(argThat(user -> user.getRole() == User.Role.CUSTOMER));
    }

    @Test
    @DisplayName("Refresh token with valid token should return new TokenResponse")
    void testRefreshTokenSuccess() {
        RefreshToken refreshToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .token("validToken")
                .user(testUser)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByToken("validToken")).thenReturn(Optional.of(refreshToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("validToken")
                .build();

        TokenResponse response = authService.refreshToken(request);

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());

        verify(refreshTokenRepository, times(1)).findByToken("validToken");
        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Refresh token with invalid token should throw AuthException")
    void testRefreshTokenInvalid() {
        when(refreshTokenRepository.findByToken("invalidToken")).thenReturn(Optional.empty());

        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("invalidToken")
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
                .id(UUID.randomUUID())
                .token("expiredToken")
                .user(testUser)
                .expiresAt(LocalDateTime.now().minusDays(1))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByToken("expiredToken")).thenReturn(Optional.of(expiredToken));

        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("expiredToken")
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
                .id(UUID.randomUUID())
                .token("revokedToken")
                .user(testUser)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .revoked(true)
                .build();

        when(refreshTokenRepository.findByToken("revokedToken")).thenReturn(Optional.of(revokedToken));

        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("revokedToken")
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
                .id(UUID.randomUUID())
                .token("logoutToken")
                .user(testUser)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByToken("logoutToken")).thenReturn(Optional.of(refreshToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.logout("logoutToken");

        verify(refreshTokenRepository, times(1)).findByToken("logoutToken");
        verify(refreshTokenRepository, times(1)).save(argThat(token -> token.isRevoked()));
    }

    @Test
    @DisplayName("Logout with non-existent token should not throw exception")
    void testLogoutNonExistentToken() {
        when(refreshTokenRepository.findByToken("nonExistentToken")).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> authService.logout("nonExistentToken"));
        verify(refreshTokenRepository, times(1)).findByToken("nonExistentToken");
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("JWT token should contain correct claims")
    void testJWTTokenClaims() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(true);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> {
            RefreshToken token = invocation.getArgument(0);
            token.setId(UUID.randomUUID());
            return token;
        });

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response.getAccessToken());
        assertTrue(response.getAccessToken().startsWith("eyJ"));
    }

    @Test
    @DisplayName("Login generates refresh token with correct expiration")
    void testRefreshTokenExpiration() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(true);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> {
            RefreshToken token = invocation.getArgument(0);
            token.setId(UUID.randomUUID());
            return token;
        });

        authService.login(loginRequest);

        verify(refreshTokenRepository).save(argThat(token ->
                token.getExpiresAt() != null && token.getExpiresAt().isAfter(LocalDateTime.now())
        ));
    }
}
