package com.green.spring_board.security;

import com.green.spring_board.entity.AccessTokenBlacklist;
import com.green.spring_board.repository.AccessTokenBlacklistRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationEntryPoint authenticationEntryPrint;
    private final AccessTokenBlacklistRepository accessTokenBlacklistRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            //* 토큰 위변조 여부 검사
            if (tokenProvider.validAccessToken(token)) {
                Optional<AccessTokenBlacklist> OptionalATB = accessTokenBlacklistRepository.findByToken(token);
                if (OptionalATB.isPresent()) {
                    authenticationEntryPrint.commence(
                            request,
                            response,
                            new BadCredentialsException("무효화된 액세스 토큰입니다.")
                    );
                    return;
                }

                String username = tokenProvider.getUsernameFromToken(token);
                UserDetails userDetails = null;
                try {
                    userDetails = userDetailsService.loadUserByUsername(username);
                }  catch (UsernameNotFoundException e) {
                    authenticationEntryPrint.commence(request, response, e);
                    return;
                }

                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
