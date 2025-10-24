package com.regifted.app.security;

import com.regifted.app.entity.User;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private CustomUserDetails userDetails;

    private JwtAuthenticationFilter filter;

    private final String VALID_TOKEN = "valid.jwt.token";
    private final String USER_UUID = "user-uuid-123";

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        filter = new JwtAuthenticationFilter(jwtUtils, userDetailsService);
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("Token Parsing and Authentication")
    class TokenParsingTests {

        @Test
        @DisplayName("Authenticates and sets SecurityContext when token is valid")
        void testValidToken() throws ServletException, IOException {
            when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
            when(jwtUtils.validateToken(VALID_TOKEN)).thenReturn(true);
            when(jwtUtils.getUserUuidFromToken(VALID_TOKEN)).thenReturn(USER_UUID);

            User mockUserEntity = new User();
            mockUserEntity.setUuid(USER_UUID);
            mockUserEntity.setEmail("john@example.com");
            mockUserEntity.setName("John Doe");
            when(userDetailsService.loadUserEntityByUuid(USER_UUID)).thenReturn(mockUserEntity);

            filter.doFilterInternal(request, response, filterChain);

            var auth = SecurityContextHolder.getContext().getAuthentication();
            assertNotNull(auth, "Authentication should be set in context");
            assertTrue(auth instanceof UsernamePasswordAuthenticationToken);
            assertEquals("john@example.com", ((CustomUserDetails) auth.getPrincipal()).getUsername());
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Does not authenticate if Authorization header is missing")
        void testNoAuthorizationHeader() throws ServletException, IOException {
            when(request.getHeader("Authorization")).thenReturn(null);

            filter.doFilterInternal(request, response, filterChain);

            assertNull(SecurityContextHolder.getContext().getAuthentication());
            verify(filterChain).doFilter(request, response);
            verifyNoInteractions(jwtUtils, userDetailsService);
        }

        @Test
        @DisplayName("Does not authenticate if token is invalid")
        void testInvalidToken() throws ServletException, IOException {
            when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
            when(jwtUtils.validateToken(VALID_TOKEN)).thenReturn(false);

            filter.doFilterInternal(request, response, filterChain);

            assertNull(SecurityContextHolder.getContext().getAuthentication());
            verify(jwtUtils).validateToken(VALID_TOKEN);
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Skips authentication when user not found")
        void testUserNotFound() throws ServletException, IOException {
            when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
            when(jwtUtils.validateToken(VALID_TOKEN)).thenReturn(true);
            when(jwtUtils.getUserUuidFromToken(VALID_TOKEN)).thenReturn(USER_UUID);
            when(userDetailsService.loadUserEntityByUuid(USER_UUID)).thenReturn(null);

            filter.doFilterInternal(request, response, filterChain);

            assertNull(SecurityContextHolder.getContext().getAuthentication());
            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("Header Parsing")
    class HeaderParsingTests {

        @Test
        @DisplayName("Extracts token from Bearer header")
        void testParseToken() throws Exception {
            when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);

            // Use reflection to test private method
            var method = JwtAuthenticationFilter.class.getDeclaredMethod("parseToken", HttpServletRequest.class);
            method.setAccessible(true);
            String token = (String) method.invoke(filter, request);

            assertEquals(VALID_TOKEN, token);
        }

        @Test
        @DisplayName("Returns null when header is malformed")
        void testMalformedHeader() throws Exception {
            when(request.getHeader("Authorization")).thenReturn("InvalidHeader");

            var method = JwtAuthenticationFilter.class.getDeclaredMethod("parseToken", HttpServletRequest.class);
            method.setAccessible(true);
            String token = (String) method.invoke(filter, request);

            assertNull(token);
        }
    }

    @Test
    void testCreateCustomUserDetails() {
        User user = new User();
        user.setUuid(USER_UUID);
        user.setEmail("john@example.com");
        user.setName("John Doe");

        // Spy on the filter to access protected method
        JwtAuthenticationFilter spyFilter = Mockito.spy(filter);
        CustomUserDetails details = spyFilter.createCustomUserDetails(user);

        assertNotNull(details);
        assertEquals("john@example.com", details.getUsername());
    }
}
