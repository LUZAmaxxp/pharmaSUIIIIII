package com.pharmacy.shared.security;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;

/**
 * Custom principal object stored in the SecurityContext.
 * Carries the user's database ID alongside email and granted authorities,
 * enabling @AuthenticationPrincipal injection in controllers.
 */
public class AdminPrincipal {

    private final Long id;
    private final String email;
    private final Collection<? extends GrantedAuthority> authorities;

    public AdminPrincipal(Long id, String email,
                          Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.authorities = Collections.unmodifiableCollection(authorities);
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }

    @Override
    public String toString() {
        return "AdminPrincipal{id=" + id + ", email='" + email + "'}";
    }
}
