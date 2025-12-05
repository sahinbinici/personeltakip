package com.personeltakip.service;

import com.personeltakip.model.User;
import com.personeltakip.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Convert username (which is TC Kimlik No) to Long and find user
        try {
            Long tckiml = Long.parseLong(username);
            return userRepository.findByTckiml(tckiml)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with tckiml: " + username));
        } catch (NumberFormatException e) {
            throw new UsernameNotFoundException("Invalid TC Kimlik No format: " + username);
        }
    }

    public User save(User user) {
        return userRepository.save(user);
    }
}
