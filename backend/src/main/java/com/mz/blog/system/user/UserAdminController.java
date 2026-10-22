package com.mz.blog.system.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mz.blog.common.api.ApiResponse;
import com.mz.blog.common.api.PageResult;
import com.mz.blog.common.exception.BusinessException;
import com.mz.blog.common.util.SecurityUtils;
import com.mz.blog.system.role.RoleMapper;
import com.mz.blog.system.role.SysRole;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/v1/system/users")
@RequiredArgsConstructor
public class UserAdminController {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    public record UserPayload(@NotBlank String username, String nickname, String avatarUrl,
                              String email, String phone, Integer status, String remark,
                              List<Long> roleIds, String password) {
    }

    public record ResetPasswordPayload(@NotBlank @Size(min = 8, max = 64) String password) {
    }

    record UserRow(String id, String username, String nickname, String avatarUrl, String email,
                   String phone, Integer status, LocalDateTime lastLoginAt, String lastLoginIp,
                   String remark, LocalDateTime createdAt, List<Long> roleIds, List<String> roleNames) {
    }

    @GetMapping
    @PreAuthorize("hasAuthority('system:user:list')")
    public ApiResponse<PageResult<UserRow>> page(@RequestParam(defaultValue = "1") long page,
                                                 @RequestParam(defaultValue = "20") long size,
                                                 @RequestParam(required = false) String keyword,
                                                 @RequestParam(required = false) Integer status) {
        IPage<SysUser> result = userMapper.selectPage(new Page<>(page, Math.min(size, 100)),
                Wrappers.<SysUser>lambdaQuery()
                        .and(StringUtils.hasText(keyword), q -> q.like(SysUser::getUsername, keyword)
                                .or().like(SysUser::getNickname, keyword)
                                .or().like(SysUser::getEmail, keyword))
                        .eq(status != null, SysUser::getStatus, status)
                        .orderByDesc(SysUser::getCreatedAt));
        Map<Long, SysRole> roles = new LinkedHashMap<>();
        roleMapper.selectList(null).forEach(role -> roles.put(role.getId(), role));
        List<UserRow> rows = result.getRecords().stream().map(user -> toRow(user, roles)).toList();
        return ApiResponse.ok(PageResult.of(rows, result.getCurrent(), result.getSize(), result.getTotal()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:list')")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        SysUser user = requireUser(id);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("user", user);
        data.put("roleIds", userRoleMapper.selectRoleIds(id));
        return ApiResponse.ok(data);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('system:user:create')")
    @Transactional
    public ApiResponse<Void> create(@Valid @RequestBody UserPayload payload) {
        if (userMapper.selectCount(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, payload.username())) > 0) {
            throw BusinessException.conflict("用户名已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(payload.username().trim());
        user.setNickname(StringUtils.hasText(payload.nickname()) ? payload.nickname().trim() : payload.username().trim());
        user.setAvatarUrl(payload.avatarUrl());
        user.setEmail(payload.email());
        user.setPhone(payload.phone());
        user.setStatus(payload.status() == null ? 1 : payload.status());
        user.setRemark(payload.remark());
        user.setPassword(passwordEncoder.encode(StringUtils.hasText(payload.password()) ? payload.password() : "Admin@123"));
        user.setCreatedBy(SecurityUtils.currentUserId());
        userMapper.insert(user);
        saveRoles(user.getId(), payload.roleIds());
        return ApiResponse.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:update')")
    @Transactional
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody UserPayload payload) {
        SysUser user = requireUser(id);
        if (!user.getUsername().equals(payload.username()) &&
                userMapper.selectCount(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, payload.username())) > 0) {
            throw BusinessException.conflict("用户名已存在");
        }
        user.setUsername(payload.username().trim());
        user.setNickname(StringUtils.hasText(payload.nickname()) ? payload.nickname().trim() : payload.username().trim());
        user.setAvatarUrl(payload.avatarUrl());
        user.setEmail(payload.email());
        user.setPhone(payload.phone());
        user.setStatus(payload.status());
        user.setRemark(payload.remark());
        user.setUpdatedBy(SecurityUtils.currentUserId());
        userMapper.updateById(user);
        saveRoles(id, payload.roleIds());
        return ApiResponse.ok();
    }

    @PutMapping("/{id}/password")
    @PreAuthorize("hasAuthority('system:user:reset-password')")
    public ApiResponse<Void> resetPassword(@PathVariable Long id,
                                           @Valid @RequestBody ResetPasswordPayload payload) {
        requireUser(id);
        SysUser update = new SysUser();
        update.setId(id);
        update.setPassword(passwordEncoder.encode(payload.password()));
        update.setUpdatedBy(SecurityUtils.currentUserId());
        userMapper.updateById(update);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:delete')")
    @Transactional
    public ApiResponse<Void> delete(@PathVariable Long id) {
        requireUser(id);
        if (id.equals(SecurityUtils.currentUserId())) {
            throw BusinessException.badRequest("不能删除当前登录账号");
        }
        if (id == 1L) {
            throw BusinessException.badRequest("初始管理员账号不能删除");
        }
        userRoleMapper.deleteByUserId(id);
        userMapper.deleteById(id);
        return ApiResponse.ok();
    }

    @GetMapping("/role-options")
    @PreAuthorize("hasAuthority('system:user:list')")
    public ApiResponse<List<SysRole>> roleOptions() {
        return ApiResponse.ok(roleMapper.selectList(Wrappers.<SysRole>lambdaQuery()
                .eq(SysRole::getStatus, 1).orderByAsc(SysRole::getSort)));
    }

    private SysUser requireUser(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }
        return user;
    }

    private void saveRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.deleteByUserId(userId);
        if (roleIds == null) {
            return;
        }
        new ArrayList<>(new java.util.LinkedHashSet<>(roleIds)).forEach(roleId -> {
            if (roleMapper.selectById(roleId) != null) {
                userRoleMapper.insert(userId, roleId);
            }
        });
    }

    private UserRow toRow(SysUser user, Map<Long, SysRole> roles) {
        List<Long> roleIds = userRoleMapper.selectRoleIds(user.getId());
        List<String> names = roleIds.stream().map(roles::get).filter(java.util.Objects::nonNull)
                .map(SysRole::getName).toList();
        return new UserRow(String.valueOf(user.getId()), user.getUsername(), user.getNickname(), user.getAvatarUrl(),
                user.getEmail(), user.getPhone(), user.getStatus(), user.getLastLoginAt(), user.getLastLoginIp(),
                user.getRemark(), user.getCreatedAt(), roleIds, names);
    }
}
