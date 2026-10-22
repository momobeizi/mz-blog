package com.mz.blog.content.nav;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mz.blog.common.api.ApiResponse;
import com.mz.blog.common.exception.BusinessException;
import com.mz.blog.common.util.SecurityUtils;
import com.mz.blog.content.album.AlbumMapper;
import com.mz.blog.content.album.CmsAlbum;
import com.mz.blog.content.category.CategoryMapper;
import com.mz.blog.content.category.CmsCategory;
import com.mz.blog.content.page.CmsPage;
import com.mz.blog.content.page.PageMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/v1/content/nav-menus")
@RequiredArgsConstructor
public class NavAdminController {

    private final NavMenuMapper navMenuMapper;
    private final CategoryMapper categoryMapper;
    private final PageMapper pageMapper;
    private final AlbumMapper albumMapper;

    public record NavPayload(Long parentId, @NotBlank String name, @NotBlank String menuType,
                             Long targetId, String url, String icon, Integer sort, Integer visible,
                             String openType, Integer status) {
    }

    @GetMapping
    @PreAuthorize("hasAuthority('content:nav:list')")
    public ApiResponse<List<CmsNavMenu>> list() {
        List<CmsNavMenu> menus = navMenuMapper.selectList(Wrappers.<CmsNavMenu>lambdaQuery()
                .orderByAsc(CmsNavMenu::getSort).orderByAsc(CmsNavMenu::getId));
        menus.forEach(this::resolve);
        return ApiResponse.ok(menus);
    }

    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('content:nav:list')")
    public ApiResponse<List<CmsNavMenu>> tree() {
        List<CmsNavMenu> menus = navMenuMapper.selectList(Wrappers.<CmsNavMenu>lambdaQuery()
                .orderByAsc(CmsNavMenu::getSort).orderByAsc(CmsNavMenu::getId));
        menus.forEach(this::resolve);
        return ApiResponse.ok(buildTree(menus));
    }

    @GetMapping("/targets")
    @PreAuthorize("hasAuthority('content:nav:list')")
    public ApiResponse<Map<String, List<Map<String, String>>>> targets() {
        Map<String, List<Map<String, String>>> data = new LinkedHashMap<>();
        List<Map<String, String>> categories = categoryMapper.selectList(Wrappers.<CmsCategory>lambdaQuery()
                        .eq(CmsCategory::getStatus, 1).orderByAsc(CmsCategory::getSort))
                .stream().map(item -> Map.of("label", item.getName(), "value", String.valueOf(item.getId()))).toList();
        List<Map<String, String>> pages = pageMapper.selectList(Wrappers.<CmsPage>lambdaQuery()
                        .eq(CmsPage::getStatus, "PUBLISHED").orderByAsc(CmsPage::getSort))
                .stream().map(item -> Map.of("label", item.getTitle(), "value", String.valueOf(item.getId()))).toList();
        List<Map<String, String>> albums = albumMapper.selectList(Wrappers.<CmsAlbum>lambdaQuery()
                        .eq(CmsAlbum::getStatus, 1).orderByAsc(CmsAlbum::getSort))
                .stream().map(item -> Map.of("label", item.getTitle(), "value", String.valueOf(item.getId()))).toList();
        data.put("CATEGORY", categories);
        data.put("PAGE", pages);
        data.put("ALBUM", albums);
        return ApiResponse.ok(data);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('content:nav:create')")
    public ApiResponse<Void> create(@Valid @RequestBody NavPayload payload) {
        validate(payload);
        CmsNavMenu menu = new CmsNavMenu();
        apply(menu, payload);
        menu.setCreatedBy(SecurityUtils.currentUserId());
        navMenuMapper.insert(menu);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('content:nav:update')")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody NavPayload payload) {
        CmsNavMenu menu = requireMenu(id);
        if (id.equals(payload.parentId())) {
            throw BusinessException.badRequest("上级菜单不能是自身");
        }
        validate(payload);
        apply(menu, payload);
        menu.setUpdatedBy(SecurityUtils.currentUserId());
        navMenuMapper.updateById(menu);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('content:nav:delete')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        requireMenu(id);
        if (navMenuMapper.selectCount(Wrappers.<CmsNavMenu>lambdaQuery().eq(CmsNavMenu::getParentId, id)) > 0) {
            throw BusinessException.badRequest("请先删除子菜单");
        }
        navMenuMapper.deleteById(id);
        return ApiResponse.ok();
    }

    private void validate(NavPayload payload) {
        if (List.of("CATEGORY", "PAGE", "ALBUM").contains(payload.menuType()) && payload.targetId() == null) {
            throw BusinessException.badRequest("请选择导航目标");
        }
        if ("CUSTOM".equals(payload.menuType()) && (payload.url() == null || payload.url().isBlank())) {
            throw BusinessException.badRequest("请填写自定义链接");
        }
    }

    private void apply(CmsNavMenu menu, NavPayload payload) {
        menu.setParentId(payload.parentId() == null ? 0L : payload.parentId());
        menu.setName(payload.name().trim());
        menu.setMenuType(payload.menuType());
        menu.setTargetId(payload.targetId());
        menu.setUrl(payload.url());
        menu.setIcon(payload.icon());
        menu.setSort(payload.sort() == null ? 0 : payload.sort());
        menu.setVisible(payload.visible() == null ? 1 : payload.visible());
        menu.setOpenType(payload.openType() == null ? "_self" : payload.openType());
        menu.setStatus(payload.status() == null ? 1 : payload.status());
    }

    private void resolve(CmsNavMenu menu) {
        menu.setResolvedUrl(switch (menu.getMenuType()) {
            case "HOME" -> "/";
            case "CATEGORY" -> {
                CmsCategory category = menu.getTargetId() == null ? null : categoryMapper.selectById(menu.getTargetId());
                yield category == null ? "/articles" : "/categories/" + category.getSlug();
            }
            case "PAGE" -> {
                CmsPage page = menu.getTargetId() == null ? null : pageMapper.selectById(menu.getTargetId());
                yield page == null ? "/" : "/p/" + page.getSlug();
            }
            case "ALBUM" -> {
                CmsAlbum album = menu.getTargetId() == null ? null : albumMapper.selectById(menu.getTargetId());
                yield album == null ? "/albums" : "/albums/" + album.getSlug();
            }
            default -> menu.getUrl() == null ? "/" : menu.getUrl();
        });
    }

    private CmsNavMenu requireMenu(Long id) {
        CmsNavMenu menu = navMenuMapper.selectById(id);
        if (menu == null) {
            throw BusinessException.notFound("导航不存在");
        }
        return menu;
    }

    private List<CmsNavMenu> buildTree(List<CmsNavMenu> menus) {
        Map<Long, CmsNavMenu> byId = new LinkedHashMap<>();
        menus.forEach(menu -> {
            menu.setChildren(new ArrayList<>());
            byId.put(menu.getId(), menu);
        });
        List<CmsNavMenu> roots = new ArrayList<>();
        for (CmsNavMenu menu : menus) {
            CmsNavMenu parent = byId.get(menu.getParentId());
            if (parent == null || menu.getParentId() == 0) {
                roots.add(menu);
            } else {
                parent.getChildren().add(menu);
            }
        }
        roots.sort(Comparator.comparing(CmsNavMenu::getSort).thenComparing(CmsNavMenu::getId));
        return roots;
    }
}
