package com.pranav.society_vehicle_manager.security;

import com.pranav.society_vehicle_manager.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import com.pranav.society_vehicle_manager.enums.AccountStatus;
import com.pranav.society_vehicle_manager.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;


@Service
public class DwaarPalUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public DwaarPalUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByPhone(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with phone: " + username));

//        return org.springframework.security.core.userdetails.User
//                .withUsername(user.getPhone())
//                .password(user.getPassword())
//                .disabled(user.getStatus() != AccountStatus.ACTIVE)
//                .authorities("ROLE_" + user.getRole().name()) // You can set authorities/roles as needed
//                .build();

        return new DwaarPalPrincipal(
                user.getId(),
                user.getPhone(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())),
                user.getStatus() == AccountStatus.ACTIVE);
    }
}
