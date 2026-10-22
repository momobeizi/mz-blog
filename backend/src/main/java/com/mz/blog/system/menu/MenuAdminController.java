package com.mz.blog.system.menu;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mz.blog.common.api.ApiResponse;
import com.mz.blog.common.exception.BusinessException;
import com.mz.blog.common.util.SecurityUtils;
import com.mz.blog.security.AdminUserDetails;
import com.mz.blog.system.role.RoleMenuMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/v1/system/menus")
@RequiredArgsConstructor
public class MenuAdminController {

    private final MenuMapper menuMapper;
    private final RoleMenuMapper roleMenuMapper;

    public record MenuPayload(Long parentId, @NotBlank String name, @NotBlank String type,
                              String path, String componentKey, String permissionCode, String icon,
                              Integer sort, Integer visible, Integer status) {
    }

    @GetMapping
    @PreAuthorize("hasAuthority('system:menu:list')")
    public ApiResponse<List<SysMenu>> all() {
        return ApiResponse.ok(menuMapper.selectList(Wrappers.<SysMenu>lambdaQuery()
                .orderByAsc(SysMenu::getSort).orderByAsc(SysMenu::getId)));
    }

    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('system:menu:list')")
    public ApiResponse<List<SysMenu>> tree() {
        return ApiResponse.ok(buildTree(menuMapper.selectList(Wrappers.<SysMenu>lambdaQuery()
                .orderByAsc(SysMenu::getSort).orderByAsc(SysMenu::getId))));
    }

    @GetMapping("/current")
    public ApiResponse<List<SysMenu>> current() {
        AdminUserDetails user = SecurityUtils.currentUser();
        if (user == null) {
            throw BusinessException.forbidden("请先登录");
        }
        List<SysMenu> menus;
        if (user.isSuperAdmin()) {
            menus = menuMapper.selectList(Wrappers.<SysMenu>lambdaQuery()
                    .eq(SysMenu::getStatus, 1)
                    .in(SysMenu::getType, "DIR", "MENU")
                    .orderByAsc(SysMenu::getSort));
        } else {
            menus = menuMapper.selectByUserId(user.getId()).stream()
                    .filter(menu -> Integer.valueOf(1).equals(menu.getStatus()))
                    .filter(menu -> "DIR".equals(menu.getType()) || "MENU".equals(menu.getType()))
                    .toList();
        }
        return ApiResponse.ok(buildVisibleTree(menus));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('system:menu:create')")
    public ApiResponse<Void> create(@Valid @RequestBody MenuPayload payload) {
        validate(payload, null);
        SysMenu menu = new SysMenu();
        apply(menu, payload);
        menu.setCreatedBy(SecurityUtils.currentUserId());
        menuMapper.insert(menu);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:menu:update')")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody MenuPayload payload) {
        SysMenu menu = requireMenu(id);
        validate(payload, id);
        apply(menu, payload);
        menu.setUpdatedBy(SecurityUtils.currentUserId());
        menuMapper.updateById(menu);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:menu:delete')")
    @Transactional
    public ApiResponse<Void> delete(@PathVariable Long id) {
        requireMenu(id);
        if (menuMapper.selectCount(Wrappers.<SysMenu>lambdaQuery().eq(SysMenu::getParentId, id)) > 0) {
            throw BusinessException.badRequest("请先删除子菜单");
        }
        menuMapper.deleteById(id);
        return ApiResponse.ok();
    }

    private void validate(MenuPayload payload, Long currentId) {
        if (currentId != null && currentId.equals(payload.parentId())) {
            throw BusinessException.badRequest("上级菜单不能是自身");
        }
        if (payload.permissionCode() != null && !payload.permissionCode().isBlank()) {
            Long count = menuMapper.selectCount(Wrappers.<SysMenu>lambdaQuery()
                    .eq(SysMenu::getPermissionCode, payload.permissionCode())
                    .ne(currentId != null, SysMenu::getId, currentId));
            if (count > 0) {
                throw BusinessException.conflict("权限标识已存在");
            }
        }
    }

    private void apply(SysMenu menu, MenuPayload payload) {
        menu.setParentId(payload.parentId() == null ? 0L : payload.parentId());
        menu.setName(payload.name().trim());
        menu.setType(payload.type());
        menu.setPath(payload.path());
        menu.setComponentKey(payload.componentKey());
        menu.setPermissionCode(payload.permissionCode() == null || payload.permissionCode().isBlank()
                ? null : payload.permissionCode().trim());
        menu.setIcon(payload.icon());
        menu.setSort(payload.sort() == null ? 0 : payload.sort());
        menu.setVisible(payload.visible() == null ? 1 : payload.visible());
        menu.setStatus(payload.status() == null ? 1 : payload.status());
    }

    private SysMenu requireMenu(Long id) {
        SysMenu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw BusinessException.notFound("菜单不存在");
        }
        return menu;
    }

    private List<SysMenu> buildTree(List<SysMenu> menus) {
        Map<Long, SysMenu> byId = new LinkedHashMap<>();
        menus.forEach(menu -> {
            menu.setChildren(new ArrayList<>());
            byId.put(menu.getId(), menu);
        });
        List<SysMenu> roots = new ArrayList<>();
        for (SysMenu menu : menus) {
            SysMenu parent = byId.get(menu.getParentId());
            if (parent == null || menu.getParentId() == 0) {
                roots.add(menu);
            } else {
                parent.getChildren().add(menu);
            }
        }
        sortTree(roots);
        return roots;
    }

    private List<SysMenu> buildVisibleTree(List<SysMenu> menus) {
        List<SysMenu> visible = menus.stream()
                .filter(menu -> Integer.valueOf(1).equals(menu.getVisible()))
                .toList();
        return buildTree(visible);
    }

    private void sortTree(List<SysMenu> menus) {
        menus.sort(Comparator.comparing(SysMenu::getSort).thenComparing(SysMenu::getId));
        menus.forEach(menu -> sortTree(menu.getChildren()));
    }
}
