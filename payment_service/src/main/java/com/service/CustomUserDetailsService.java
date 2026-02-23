package com.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.model.entity.AppUser;
import com.repo.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

        @Autowired
        private UserRepository userRepository;

        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                AppUser user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
                return User
                                .builder()
                                .username(user.getUsername())
                                .password(user.getPassword())
                                .authorities(user.getRoles().stream()
                                                .map(role -> new SimpleGrantedAuthority(
                                                                "ROLE_" + role.getName().name()))
                                                .toList())
                                .build();
        }
}