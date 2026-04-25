package com.relatorio.transporte.service.sec;

import com.relatorio.transporte.entity.mysql.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class UserPrincipal implements UserDetails, Principal {

    private final UUID   id;
    private final String name;
    private final String email;
    private final String password;
    private final String role;
    private final boolean active;
    private final User.UserStatus status;
    private final Collection<? extends GrantedAuthority> authorities;

    public static UserPrincipal from(User user) {

        List<GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority(user.getRole().toSpringRole())
        );
        return new UserPrincipal(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getPasswordHash(),
            user.getRole().name(),
            user.isActive(),
            user.getStatus(),
            authorities
        );
    }

    @Override public String getUsername()               { return email; }
    @Override public boolean isAccountNonExpired()      { return true; }
    @Override public boolean isAccountNonLocked()       { return active; }
    @Override public boolean isCredentialsNonExpired()  { return true; }
    @Override public boolean isEnabled()                { return active; }
}
