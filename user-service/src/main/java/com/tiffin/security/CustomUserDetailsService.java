package com.tiffin.security;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.tiffin.entity.User;
import com.tiffin.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {
	
	private final UserRepository userRepository;

	public CustomUserDetailsService(UserRepository userRepository) {
		super();
		this.userRepository = userRepository;
	}
	
	/**
     * Spring Security calls this method when it needs to verify a user.
     * We load the user by email (we use email as username).
     *
     * Returns Spring's UserDetails object which contains:
     *  - username (email in our case)
     *  - password (BCrypt hash)
     *  - authorities (roles like ROLE_CUSTOMER, ROLE_ADMIN)
     */
	
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		
		User user = userRepository.findByEmail(email)
				.orElseThrow(()->new UsernameNotFoundException("user not found with email : "+email));
		
		// Spring Security expects roles prefixed with "ROLE_"
        // So CUSTOMER becomes ROLE_CUSTOMER
        // This is used in SecurityConfig for hasRole() checks
		SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_"+user.getRole().name());
		
		// Build and return Spring's UserDetails
		return new org.springframework.security.core.userdetails.User(
				user.getEmail(),
				user.getPasswordHash(),
				user.isActive(),	//enabled
				true,				//accountNonExpired
				true, 				//credentialsNonExpired
				true,				//accountNonLocked
				Collections.singletonList(authority)
				);
		
	}

}
