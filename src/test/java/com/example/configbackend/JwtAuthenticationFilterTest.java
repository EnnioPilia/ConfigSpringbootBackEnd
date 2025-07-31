// package com.example.configbackend;

// import java.util.Optional;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import static org.mockito.ArgumentMatchers.anyString;
// import static org.mockito.ArgumentMatchers.contains;
// import static org.mockito.ArgumentMatchers.eq;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;
// import org.mockito.MockitoAnnotations;
// import org.springframework.security.core.context.SecurityContextHolder;

// import com.example.configbackend.config.JWTUtils;
// import com.example.configbackend.config.JwtAuthenticationFilter;
// import com.example.configbackend.model.RefreshToken;
// import com.example.configbackend.model.User;
// import com.example.configbackend.service.RefreshTokenService;
// import com.example.configbackend.service.UserService;

// import jakarta.servlet.FilterChain;
// import jakarta.servlet.http.Cookie;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;

// public class JwtAuthenticationFilterTest {

//     @InjectMocks
//     private JwtAuthenticationFilter filter;

//     @Mock
//     private JWTUtils jwtUtils;

//     @Mock
//     private UserService userService;

//     @Mock
//     private RefreshTokenService refreshTokenService;

//     @Mock
//     private FilterChain filterChain;

//     @Mock
//     private HttpServletRequest request;

//     @Mock
//     private HttpServletResponse response;

//     @BeforeEach
//     public void setup() {
//         MockitoAnnotations.openMocks(this);
//         SecurityContextHolder.clearContext();
//     }

//     @Test
// void testDoFilterInternal_refreshTokenUsed_authTokenRefreshed() throws Exception {
//     when(request.getServletPath()).thenReturn("/api/test"); // Ajouté pour éviter NPE

//     Cookie authTokenCookie = new Cookie("authToken", "invalidToken");
//     Cookie refreshTokenCookie = new Cookie("refreshToken", "validRefreshToken");

//     when(request.getCookies()).thenReturn(new Cookie[]{authTokenCookie, refreshTokenCookie});
//     when(jwtUtils.validateToken("invalidToken")).thenReturn(false);

//     User user = new User();
//     user.setEmail("user@example.com");
//     user.setRole("USER");

//     RefreshToken refreshToken = new RefreshToken();
//     refreshToken.setUser(user);

//     when(refreshTokenService.findByToken("validRefreshToken")).thenReturn(Optional.of(refreshToken));
//     when(refreshTokenService.isRefreshTokenExpired(refreshToken)).thenReturn(false);

//     when(jwtUtils.generateToken(eq(user.getEmail()), anyString())).thenReturn("newAuthToken");

// filter.doFilter(request, response, filterChain);

//     verify(response).setHeader(eq("Set-Cookie"), contains("newAuthToken"));
//     verify(filterChain).doFilter(request, response);

//     assert SecurityContextHolder.getContext().getAuthentication() != null;
//     assert SecurityContextHolder.getContext().getAuthentication().isAuthenticated();
// }

// }
