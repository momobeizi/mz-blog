package com.mz.blog.content.link;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mz.blog.common.api.ApiResponse;
import com.mz.blog.common.exception.BusinessException;
import com.mz.blog.common.util.SecurityUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/v1/content/links")
@RequiredArgsConstructor
public class LinkAdminController {

    private final LinkMapper linkMapper;

    public record LinkPayload(@NotBlank String linkType, String groupName, @NotBlank String name,
                              @NotBlank String url, String logoUrl, String description, Integer sort,
                              Integer nofollow, String openType, Integer status) {
    }

    @GetMapping
    @PreAuthorize("hasAuthority('content:link:list')")
    public ApiResponse<List<CmsLink>> list(@RequestParam(required = false) String linkType,
                                            @RequestParam(required = false) String keyword) {
        return ApiResponse.ok(linkMapper.selectList(Wrappers.<CmsLink>lambdaQuery()
                .eq(StringUtils.hasText(linkType), CmsLink::getLinkType, linkType)
                .and(StringUtils.hasText(keyword), q -> q.like(CmsLink::getName, keyword)
                        .or().like(CmsLink::getDescription, keyword))
                .orderByAsc(CmsLink::getLinkType).orderByAsc(CmsLink::getSort).orderByAsc(CmsLink::getId)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('content:link:create')")
    public ApiResponse<Void> create(@Valid @RequestBody LinkPayload payload) {
        CmsLink link = new CmsLink();
        apply(link, payload);
        link.setCreatedBy(SecurityUtils.currentUserId());
        linkMapper.insert(link);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('content:link:update')")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody LinkPayload payload) {
        CmsLink link = requireLink(id);
        apply(link, payload);
        link.setUpdatedBy(SecurityUtils.currentUserId());
        linkMapper.updateById(link);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('content:link:delete')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        requireLink(id);
        linkMapper.deleteById(id);
        return ApiResponse.ok();
    }

    private void apply(CmsLink link, LinkPayload payload) {
        if (!List.of("FRIEND", "COMMON").contains(payload.linkType())) {
            throw BusinessException.badRequest("链接类型不正确");
        }
        if (!payload.url().startsWith("http://") && !payload.url().startsWith("https://") && !payload.url().startsWith("/")) {
            throw BusinessException.badRequest("链接必须以 http://、https:// 或 / 开头");
        }
        link.setLinkType(payload.linkType());
        link.setGroupName(StringUtils.hasText(payload.groupName()) ? payload.groupName() : "默认分组");
        link.setName(payload.name().trim());
        link.setUrl(payload.url().trim());
        link.setLogoUrl(payload.logoUrl());
        link.setDescription(payload.description());
        link.setSort(payload.sort() == null ? 0 : payload.sort());
        link.setNofollow(payload.nofollow() == null ? 0 : payload.nofollow());
        link.setOpenType(payload.openType() == null ? "_blank" : payload.openType());
        link.setStatus(payload.status() == null ? 1 : payload.status());
    }

    private CmsLink requireLink(Long id) {
        CmsLink link = linkMapper.selectById(id);
        if (link == null) {
            throw BusinessException.notFound("链接不存在");
        }
        return link;
    }
}
