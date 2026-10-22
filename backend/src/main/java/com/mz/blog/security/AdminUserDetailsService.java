package com.mz.blog.security;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mz.blog.system.menu.MenuMapper;
import com.mz.blog.system.menu.SysMenu;
import com.mz.blog.system.user.SysUser;
import com.mz.blog.system.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;
    private final MenuMapper menuMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, username)
                .last("LIMIT 1"));
        if (user == null) {
            throw new UsernameNotFoundException("用户名或密码错误");
        }
        Set<String> roles = new LinkedHashSet<>(userMapper.selectRoleCodes(user.getId()));
        Set<String> permissions = new LinkedHashSet<>(userMapper.selectPermissionCodes(user.getId()));
        if (roles.contains("SUPER_ADMIN")) {
            List<SysMenu> menus = menuMapper.selectList(Wrappers.<SysMenu>lambdaQuery()
                    .eq(SysMenu::getStatus, 1)
                    .isNotNull(SysMenu::getPermissionCode));
            menus.stream()
                    .map(SysMenu::getPermissionCode)
                    .filter(code -> code != null && !code.isBlank())
                    .forEach(permissions::add);
        }
        return new AdminUserDetails(user.getId(), user.getUsername(), user.getPassword(),
                user.getNickname(), user.getAvatarUrl(), Integer.valueOf(1).equals(user.getStatus()),
                Set.copyOf(roles), Set.copyOf(permissions));
    }
}
