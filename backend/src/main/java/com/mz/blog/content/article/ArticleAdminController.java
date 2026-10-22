package com.mz.blog.content.article;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mz.blog.common.api.ApiResponse;
import com.mz.blog.common.api.PageResult;
import com.mz.blog.common.exception.BusinessException;
import com.mz.blog.common.util.SecurityUtils;
import com.mz.blog.common.util.SlugUtils;
import com.mz.blog.content.category.CategoryMapper;
import com.mz.blog.content.category.CmsCategory;
import com.mz.blog.content.render.ContentRenderService;
import com.mz.blog.content.tag.CmsTag;
import com.mz.blog.content.tag.TagMapper;
import com.mz.blog.system.user.SysUser;
import com.mz.blog.system.user.UserMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/v1/content/articles")
@RequiredArgsConstructor
public class ArticleAdminController {

    private final ArticleMapper articleMapper;
    private final ArticleTagMapper articleTagMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final UserMapper userMapper;
    private final ContentRenderService renderService;

    public record ArticlePayload(@NotBlank String title, String slug, String summary, String coverUrl,
                                 @NotBlank String content, String contentFormat, Long categoryId,
                                 String status, Integer isTop, Integer isRecommended,
                                 String seoTitle, String seoKeywords, String seoDescription,
                                 List<Long> tagIds, Integer version) {
    }

    public record StatusPayload(@NotBlank String status) {
    }

    public record PreviewPayload(@NotBlank String content, String format) {
    }

    @GetMapping
    @PreAuthorize("hasAuthority('content:article:list')")
    public ApiResponse<PageResult<CmsArticle>> page(@RequestParam(defaultValue = "1") long page,
                                                     @RequestParam(defaultValue = "20") long size,
                                                     @RequestParam(required = false) String keyword,
                                                     @RequestParam(required = false) String status,
                                                     @RequestParam(required = false) Long categoryId,
                                                     @RequestParam(required = false) Long tagId) {
        IPage<CmsArticle> result = articleMapper.selectPage(new Page<>(page, Math.min(size, 100)),
                Wrappers.<CmsArticle>lambdaQuery()
                        .and(StringUtils.hasText(keyword), q -> q.like(CmsArticle::getTitle, keyword)
                                .or().like(CmsArticle::getSummary, keyword))
                        .eq(StringUtils.hasText(status), CmsArticle::getStatus, status)
                        .eq(categoryId != null, CmsArticle::getCategoryId, categoryId)
                        .inSql(tagId != null, CmsArticle::getId,
                                "SELECT article_id FROM cms_article_tag WHERE tag_id = " + (tagId == null ? 0 : tagId))
                        .orderByDesc(CmsArticle::getIsTop)
                        .orderByDesc(CmsArticle::getPublishedAt)
                        .orderByDesc(CmsArticle::getCreatedAt));
        enrich(result.getRecords(), false);
        return ApiResponse.ok(PageResult.of(result.getRecords(), result.getCurrent(), result.getSize(), result.getTotal()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('content:article:list')")
    public ApiResponse<CmsArticle> detail(@PathVariable Long id) {
        CmsArticle article = requireArticle(id);
        enrich(List.of(article), true);
        return ApiResponse.ok(article);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('content:article:create')")
    @Transactional
    public ApiResponse<CmsArticle> create(@Valid @RequestBody ArticlePayload payload) {
        validateReference(payload);
        CmsArticle article = new CmsArticle();
        apply(article, payload, null);
        article.setAuthorId(SecurityUtils.currentUserId());
        article.setCreatedBy(SecurityUtils.currentUserId());
        article.setViewCount(0L);
        article.setVersion(0);
        articleMapper.insert(article);
        saveTags(article.getId(), payload.tagIds());
        return ApiResponse.ok(articleMapper.selectById(article.getId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('content:article:update')")
    @Transactional
    public ApiResponse<CmsArticle> update(@PathVariable Long id, @Valid @RequestBody ArticlePayload payload) {
        CmsArticle article = requireArticle(id);
        validateReference(payload);
        apply(article, payload, id);
        article.setUpdatedBy(SecurityUtils.currentUserId());
        articleMapper.updateById(article);
        saveTags(id, payload.tagIds());
        return ApiResponse.ok(articleMapper.selectById(id));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('content:article:publish')")
    public ApiResponse<Void> changeStatus(@PathVariable Long id, @Valid @RequestBody StatusPayload payload) {
        CmsArticle article = requireArticle(id);
        String status = payload.status().toUpperCase();
        if (!List.of("DRAFT", "PUBLISHED", "OFFLINE").contains(status)) {
            throw BusinessException.badRequest("文章状态不正确");
        }
        CmsArticle update = new CmsArticle();
        update.setId(id);
        update.setStatus(status);
        if ("PUBLISHED".equals(status) && article.getPublishedAt() == null) {
            update.setPublishedAt(LocalDateTime.now());
        }
        update.setUpdatedBy(SecurityUtils.currentUserId());
        articleMapper.updateById(update);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('content:article:delete')")
    @Transactional
    public ApiResponse<Void> delete(@PathVariable Long id) {
        requireArticle(id);
        articleTagMapper.deleteByArticleId(id);
        articleMapper.deleteById(id);
        return ApiResponse.ok();
    }

    @PostMapping("/render-preview")
    @PreAuthorize("hasAuthority('content:article:list')")
    public ApiResponse<ContentRenderService.RenderedContent> renderPreview(@Valid @RequestBody PreviewPayload payload) {
        return ApiResponse.ok(renderService.render(payload.content(), payload.format()));
    }

    private void apply(CmsArticle article, ArticlePayload payload, Long currentId) {
        String format = renderService.normalizeFormat(payload.contentFormat());
        if (currentId != null && StringUtils.hasText(article.getContentFormat()) &&
                !article.getContentFormat().equals(format)) {
            throw BusinessException.badRequest("正文格式创建后不能切换，请新建文章");
        }
        String slug = StringUtils.hasText(payload.slug()) ? SlugUtils.slugify(payload.slug()) : SlugUtils.slugify(payload.title());
        if (articleMapper.selectCount(Wrappers.<CmsArticle>lambdaQuery()
                .eq(CmsArticle::getSlug, slug).ne(currentId != null, CmsArticle::getId, currentId)) > 0) {
            throw BusinessException.conflict("文章别名已存在");
        }
        ContentRenderService.RenderedContent rendered = renderService.render(payload.content(), format);
        String status = StringUtils.hasText(payload.status()) ? payload.status().toUpperCase() : "DRAFT";
        if (!List.of("DRAFT", "PUBLISHED", "OFFLINE").contains(status)) {
            throw BusinessException.badRequest("文章状态不正确");
        }
        article.setTitle(payload.title().trim());
        article.setSlug(slug);
        article.setSummary(StringUtils.hasText(payload.summary()) ? payload.summary()
                : renderService.excerpt(rendered.text(), 120));
        article.setCoverUrl(StringUtils.hasText(payload.coverUrl()) ? payload.coverUrl() : "/assets/covers/cover-1.svg");
        article.setContent(payload.content());
        article.setContentHtml(rendered.html());
        article.setContentText(rendered.text());
        article.setContentFormat(format);
        article.setCategoryId(payload.categoryId());
        article.setStatus(status);
        article.setIsTop(payload.isTop() == null ? 0 : payload.isTop());
        article.setIsRecommended(payload.isRecommended() == null ? 0 : payload.isRecommended());
        article.setSeoTitle(payload.seoTitle());
        article.setSeoKeywords(payload.seoKeywords());
        article.setSeoDescription(payload.seoDescription());
        if ("PUBLISHED".equals(status) && article.getPublishedAt() == null) {
            article.setPublishedAt(LocalDateTime.now());
        }
        if ("DRAFT".equals(status)) {
            article.setPublishedAt(null);
        }
    }

    private void validateReference(ArticlePayload payload) {
        if (payload.categoryId() == null || categoryMapper.selectById(payload.categoryId()) == null) {
            throw BusinessException.badRequest("请选择有效分类");
        }
    }

    private void saveTags(Long articleId, List<Long> tagIds) {
        articleTagMapper.deleteByArticleId(articleId);
        if (tagIds == null) {
            return;
        }
        new LinkedHashSet<>(tagIds).forEach(tagId -> {
            if (tagMapper.selectById(tagId) != null) {
                articleTagMapper.insert(articleId, tagId);
            }
        });
    }

    private CmsArticle requireArticle(Long id) {
        CmsArticle article = articleMapper.selectById(id);
        if (article == null) {
            throw BusinessException.notFound("文章不存在");
        }
        return article;
    }

    private void enrich(List<CmsArticle> articles, boolean includeContent) {
        if (articles.isEmpty()) {
            return;
        }
        Map<Long, CmsCategory> categories = categoryMapper.selectList(null).stream()
                .collect(Collectors.toMap(CmsCategory::getId, Function.identity()));
        Map<Long, SysUser> users = userMapper.selectList(null).stream()
                .collect(Collectors.toMap(SysUser::getId, Function.identity()));
        Map<Long, CmsTag> tags = tagMapper.selectList(null).stream()
                .collect(Collectors.toMap(CmsTag::getId, Function.identity()));
        for (CmsArticle article : articles) {
            CmsCategory category = categories.get(article.getCategoryId());
            article.setCategoryName(category == null ? "-" : category.getName());
            SysUser user = users.get(article.getAuthorId());
            article.setAuthorName(user == null ? "-" : user.getNickname());
            List<Long> tagIds = articleTagMapper.selectTagIds(article.getId());
            article.setTagIds(tagIds);
            article.setTagNames(tagIds.stream().map(tags::get).filter(Objects::nonNull)
                    .map(CmsTag::getName).toList());
            if (!includeContent) {
                article.setContentHtml(null);
            }
        }
    }
}
