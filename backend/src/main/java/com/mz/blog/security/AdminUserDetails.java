package com.mz.blog.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Getter
public class AdminUserDetails implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final String nickname;
    private final String avatarUrl;
    private final boolean enabled;
    private final Set<String> roles;
    private final Set<String> permissions;
    private final List<GrantedAuthority> authorities;

    public AdminUserDetails(Long id, String username, String password, String nickname, String avatarUrl,
                            boolean enabled, Set<String> roles, Set<String> permissions) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.avatarUrl = avatarUrl;
        this.enabled = enabled;
        this.roles = roles;
        this.permissions = permissions;
        List<GrantedAuthority> list = new ArrayList<>();
        roles.forEach(role -> list.add(new SimpleGrantedAuthority("ROLE_" + role)));
        permissions.forEach(permission -> list.add(new SimpleGrantedAuthority(permission)));
        this.authorities = List.copyOf(list);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    public boolean isSuperAdmin() {
        return roles.contains("SUPER_ADMIN");
    }
}
