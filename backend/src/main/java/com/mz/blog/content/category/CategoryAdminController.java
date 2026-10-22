package com.mz.blog.content.category;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mz.blog.common.api.ApiResponse;
import com.mz.blog.common.exception.BusinessException;
import com.mz.blog.common.util.SecurityUtils;
import com.mz.blog.common.util.SlugUtils;
import com.mz.blog.content.article.ArticleMapper;
import com.mz.blog.content.article.CmsArticle;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/v1/content/categories")
@RequiredArgsConstructor
public class CategoryAdminController {

    private final CategoryMapper categoryMapper;
    private final ArticleMapper articleMapper;

    public record CategoryPayload(Long parentId, @NotBlank String name, String slug,
                                  String description, String coverUrl, Integer sort, Integer status,
                                  String seoTitle, String seoKeywords, String seoDescription) {
    }

    public record CategoryRow(String id, Long parentId, String name, String slug, String description,
                              String coverUrl, Integer sort, Integer status, long articleCount) {
    }

    @GetMapping
    @PreAuthorize("hasAuthority('content:category:list')")
    public ApiResponse<List<CategoryRow>> list() {
        List<CategoryRow> rows = categoryMapper.selectList(Wrappers.<CmsCategory>lambdaQuery()
                        .orderByAsc(CmsCategory::getSort).orderByAsc(CmsCategory::getId))
                .stream()
                .map(category -> new CategoryRow(String.valueOf(category.getId()), category.getParentId(),
                        category.getName(), category.getSlug(), category.getDescription(), category.getCoverUrl(),
                        category.getSort(), category.getStatus(),
                        articleMapper.selectCount(Wrappers.<CmsArticle>lambdaQuery()
                                .eq(CmsArticle::getCategoryId, category.getId()))))
                .toList();
        return ApiResponse.ok(rows);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('content:category:list')")
    public ApiResponse<CmsCategory> detail(@PathVariable Long id) {
        return ApiResponse.ok(requireCategory(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('content:category:create')")
    public ApiResponse<Void> create(@Valid @RequestBody CategoryPayload payload) {
        CmsCategory category = new CmsCategory();
        apply(category, payload, null);
        category.setCreatedBy(SecurityUtils.currentUserId());
        categoryMapper.insert(category);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('content:category:update')")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody CategoryPayload payload) {
        CmsCategory category = requireCategory(id);
        if (id.equals(payload.parentId())) {
            throw BusinessException.badRequest("上级分类不能是自身");
        }
        apply(category, payload, id);
        category.setUpdatedBy(SecurityUtils.currentUserId());
        categoryMapper.updateById(category);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('content:category:delete')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        requireCategory(id);
        if (categoryMapper.selectCount(Wrappers.<CmsCategory>lambdaQuery().eq(CmsCategory::getParentId, id)) > 0) {
            throw BusinessException.badRequest("请先删除子分类");
        }
        if (articleMapper.selectCount(Wrappers.<CmsArticle>lambdaQuery().eq(CmsArticle::getCategoryId, id)) > 0) {
            throw BusinessException.badRequest("分类下仍有文章，不能删除");
        }
        categoryMapper.deleteById(id);
        return ApiResponse.ok();
    }

    private void apply(CmsCategory category, CategoryPayload payload, Long currentId) {
        String slug = payload.slug() != null && !payload.slug().isBlank()
                ? SlugUtils.slugify(payload.slug()) : SlugUtils.slugify(payload.name());
        if (categoryMapper.selectCount(Wrappers.<CmsCategory>lambdaQuery()
                .eq(CmsCategory::getSlug, slug).ne(currentId != null, CmsCategory::getId, currentId)) > 0) {
            throw BusinessException.conflict("分类别名已存在");
        }
        category.setParentId(payload.parentId() == null ? 0L : payload.parentId());
        category.setName(payload.name().trim());
        category.setSlug(slug);
        category.setDescription(payload.description());
        category.setCoverUrl(payload.coverUrl());
        category.setSort(payload.sort() == null ? 0 : payload.sort());
        category.setStatus(payload.status() == null ? 1 : payload.status());
        category.setSeoTitle(payload.seoTitle());
        category.setSeoKeywords(payload.seoKeywords());
        category.setSeoDescription(payload.seoDescription());
    }

    private CmsCategory requireCategory(Long id) {
        CmsCategory category = categoryMapper.selectById(id);
        if (category == null) {
            throw BusinessException.notFound("分类不存在");
        }
        return category;
    }
}
