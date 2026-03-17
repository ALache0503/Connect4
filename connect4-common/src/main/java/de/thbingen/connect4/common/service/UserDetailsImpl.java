package de.thbingen.connect4.common.service;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsImpl implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(@Nullable String username) throws UsernameNotFoundException {
        if (username == null) {
            throw new UsernameNotFoundException("No Username given");
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(username)
                .roles("USER") // Currently we only have this role
                .build();
    }
}
