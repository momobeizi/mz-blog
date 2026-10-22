package com.mz.blog.system.role;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mz.blog.common.api.ApiResponse;
import com.mz.blog.common.exception.BusinessException;
import com.mz.blog.common.util.SecurityUtils;
import com.mz.blog.system.user.UserRoleMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/v1/system/roles")
@RequiredArgsConstructor
public class RoleAdminController {

    private final RoleMapper roleMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final UserRoleMapper userRoleMapper;

    public record RolePayload(@NotBlank String code, @NotBlank String name, String description,
                              Integer sort, Integer status, List<Long> menuIds) {
    }

    record RoleRow(String id, String code, String name, String description, Integer sort,
                   Integer status, Integer builtIn, long userCount) {
    }

    @GetMapping
    @PreAuthorize("hasAuthority('system:role:list')")
    public ApiResponse<List<RoleRow>> list(@RequestParam(required = false) String keyword) {
        List<RoleRow> rows = roleMapper.selectList(Wrappers.<SysRole>lambdaQuery()
                        .and(StringUtils.hasText(keyword), q -> q.like(SysRole::getName, keyword)
                                .or().like(SysRole::getCode, keyword))
                        .orderByAsc(SysRole::getSort).orderByAsc(SysRole::getId))
                .stream()
                .map(role -> new RoleRow(String.valueOf(role.getId()), role.getCode(), role.getName(),
                        role.getDescription(), role.getSort(), role.getStatus(), role.getBuiltIn(),
                        userRoleMapper.countByRoleId(role.getId())))
                .toList();
        return ApiResponse.ok(rows);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:role:list')")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("role", requireRole(id));
        data.put("menuIds", roleMenuMapper.selectMenuIds(id));
        return ApiResponse.ok(data);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('system:role:create')")
    @Transactional
    public ApiResponse<Void> create(@Valid @RequestBody RolePayload payload) {
        if (roleMapper.selectCount(Wrappers.<SysRole>lambdaQuery().eq(SysRole::getCode, payload.code())) > 0) {
            throw BusinessException.conflict("角色编码已存在");
        }
        SysRole role = new SysRole();
        role.setCode(payload.code().trim().toUpperCase());
        role.setName(payload.name().trim());
        role.setDescription(payload.description());
        role.setSort(payload.sort() == null ? 0 : payload.sort());
        role.setStatus(payload.status() == null ? 1 : payload.status());
        role.setBuiltIn(0);
        role.setCreatedBy(SecurityUtils.currentUserId());
        roleMapper.insert(role);
        saveMenus(role.getId(), payload.menuIds());
        return ApiResponse.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:role:update')")
    @Transactional
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody RolePayload payload) {
        SysRole role = requireRole(id);
        if (!role.getCode().equalsIgnoreCase(payload.code()) &&
                roleMapper.selectCount(Wrappers.<SysRole>lambdaQuery().eq(SysRole::getCode, payload.code())) > 0) {
            throw BusinessException.conflict("角色编码已存在");
        }
        if (Integer.valueOf(1).equals(role.getBuiltIn()) && !role.getCode().equalsIgnoreCase(payload.code())) {
            throw BusinessException.badRequest("内置角色编码不能修改");
        }
        role.setCode(payload.code().trim().toUpperCase());
        role.setName(payload.name().trim());
        role.setDescription(payload.description());
        role.setSort(payload.sort() == null ? 0 : payload.sort());
        role.setStatus(payload.status());
        role.setUpdatedBy(SecurityUtils.currentUserId());
        roleMapper.updateById(role);
        if (payload.menuIds() != null) {
            saveMenus(id, payload.menuIds());
        }
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:role:delete')")
    @Transactional
    public ApiResponse<Void> delete(@PathVariable Long id) {
        SysRole role = requireRole(id);
        if (Integer.valueOf(1).equals(role.getBuiltIn())) {
            throw BusinessException.badRequest("内置角色不能删除");
        }
        if (userRoleMapper.countByRoleId(id) > 0) {
            throw BusinessException.badRequest("该角色仍有关联用户，不能删除");
        }
        roleMenuMapper.deleteByRoleId(id);
        roleMapper.deleteById(id);
        return ApiResponse.ok();
    }

    private SysRole requireRole(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw BusinessException.notFound("角色不存在");
        }
        return role;
    }

    private void saveMenus(Long roleId, List<Long> menuIds) {
        roleMenuMapper.deleteByRoleId(roleId);
        if (menuIds != null) {
            new java.util.LinkedHashSet<>(menuIds).forEach(menuId -> roleMenuMapper.insert(roleId, menuId));
        }
    }
}
