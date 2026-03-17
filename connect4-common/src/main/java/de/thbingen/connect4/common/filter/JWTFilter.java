package de.thbingen.connect4.common.filter;

import de.thbingen.connect4.common.model.dto.UserDTO;
import de.thbingen.connect4.common.ports.in.JwtUtilService;
import de.thbingen.connect4.common.ports.out.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {
    private final JwtUtilService jwtUtilService;
    private final UserDetailsService userDetailsService;
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String userId = null;
        String jwt;

        if (authHeader != null && authHeader.startsWith("Bearer")) {
            jwt = authHeader.substring(7);

            if (jwtUtilService.validateToken(jwt)) {
                userId = jwtUtilService.extractUserId(jwt);
            }
        }

        if (userId != null) {
            Optional<UserDTO> optionalUser = userService.getUserById(Long.valueOf(userId));
            if (optionalUser.isPresent()) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(optionalUser.get().username());

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }
}
