package com.mz.blog.content.tag;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mz.blog.common.api.ApiResponse;
import com.mz.blog.common.exception.BusinessException;
import com.mz.blog.common.util.SecurityUtils;
import com.mz.blog.common.util.SlugUtils;
import com.mz.blog.content.article.ArticleTagMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/v1/content/tags")
@RequiredArgsConstructor
public class TagAdminController {

    private final TagMapper tagMapper;
    private final ArticleTagMapper articleTagMapper;

    public record TagPayload(@NotBlank String name, String slug, String description) {
    }

    public record TagRow(String id, String name, String slug, String description, long articleCount) {
    }

    @GetMapping
    @PreAuthorize("hasAuthority('content:tag:list')")
    public ApiResponse<List<TagRow>> list() {
        return ApiResponse.ok(tagMapper.selectList(Wrappers.<CmsTag>lambdaQuery().orderByAsc(CmsTag::getId))
                .stream().map(tag -> new TagRow(String.valueOf(tag.getId()), tag.getName(), tag.getSlug(),
                        tag.getDescription(), articleTagMapper.countByTagId(tag.getId()))).toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('content:tag:list')")
    public ApiResponse<CmsTag> detail(@PathVariable Long id) {
        return ApiResponse.ok(requireTag(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('content:tag:create')")
    public ApiResponse<Void> create(@Valid @RequestBody TagPayload payload) {
        CmsTag tag = new CmsTag();
        apply(tag, payload, null);
        tag.setCreatedBy(SecurityUtils.currentUserId());
        tagMapper.insert(tag);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('content:tag:update')")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody TagPayload payload) {
        CmsTag tag = requireTag(id);
        apply(tag, payload, id);
        tag.setUpdatedBy(SecurityUtils.currentUserId());
        tagMapper.updateById(tag);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('content:tag:delete')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        requireTag(id);
        if (articleTagMapper.countByTagId(id) > 0) {
            throw BusinessException.badRequest("标签已被文章使用，不能删除");
        }
        tagMapper.deleteById(id);
        return ApiResponse.ok();
    }

    private void apply(CmsTag tag, TagPayload payload, Long currentId) {
        String slug = payload.slug() != null && !payload.slug().isBlank()
                ? SlugUtils.slugify(payload.slug()) : SlugUtils.slugify(payload.name());
        if (tagMapper.selectCount(Wrappers.<CmsTag>lambdaQuery()
                .eq(CmsTag::getName, payload.name()).ne(currentId != null, CmsTag::getId, currentId)) > 0) {
            throw BusinessException.conflict("标签名称已存在");
        }
        if (tagMapper.selectCount(Wrappers.<CmsTag>lambdaQuery()
                .eq(CmsTag::getSlug, slug).ne(currentId != null, CmsTag::getId, currentId)) > 0) {
            throw BusinessException.conflict("标签别名已存在");
        }
        tag.setName(payload.name().trim());
        tag.setSlug(slug);
        tag.setDescription(payload.description());
    }

    private CmsTag requireTag(Long id) {
        CmsTag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw BusinessException.notFound("标签不存在");
        }
        return tag;
    }
}
