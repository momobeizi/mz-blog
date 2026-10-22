package com.mz.blog.content.page;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mz.blog.common.api.ApiResponse;
import com.mz.blog.common.api.PageResult;
import com.mz.blog.common.exception.BusinessException;
import com.mz.blog.common.util.SecurityUtils;
import com.mz.blog.common.util.SlugUtils;
import com.mz.blog.content.render.ContentRenderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/v1/content/pages")
@RequiredArgsConstructor
public class PageAdminController {

    private final PageMapper pageMapper;
    private final ContentRenderService renderService;

    public record PagePayload(@NotBlank String title, String slug, @NotBlank String content,
                              String contentFormat, String status, Integer sort, String seoTitle,
                              String seoKeywords, String seoDescription) {
    }

    @GetMapping
    @PreAuthorize("hasAuthority('content:page:list')")
    public ApiResponse<PageResult<CmsPage>> page(@RequestParam(defaultValue = "1") long page,
                                                  @RequestParam(defaultValue = "20") long size,
                                                  @RequestParam(required = false) String keyword,
                                                  @RequestParam(required = false) String status) {
        IPage<CmsPage> result = pageMapper.selectPage(new Page<>(page, Math.min(size, 100)),
                Wrappers.<CmsPage>lambdaQuery()
                        .and(StringUtils.hasText(keyword), q -> q.like(CmsPage::getTitle, keyword)
                                .or().like(CmsPage::getSlug, keyword))
                        .eq(StringUtils.hasText(status), CmsPage::getStatus, status)
                        .orderByAsc(CmsPage::getSort).orderByDesc(CmsPage::getCreatedAt));
        return ApiResponse.ok(PageResult.of(result.getRecords(), result.getCurrent(), result.getSize(), result.getTotal()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('content:page:list')")
    public ApiResponse<CmsPage> detail(@PathVariable Long id) {
        return ApiResponse.ok(requirePage(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('content:page:create')")
    public ApiResponse<CmsPage> create(@Valid @RequestBody PagePayload payload) {
        CmsPage page = new CmsPage();
        apply(page, payload, null);
        page.setCreatedBy(SecurityUtils.currentUserId());
        pageMapper.insert(page);
        return ApiResponse.ok(pageMapper.selectById(page.getId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('content:page:update')")
    public ApiResponse<CmsPage> update(@PathVariable Long id, @Valid @RequestBody PagePayload payload) {
        CmsPage page = requirePage(id);
        apply(page, payload, id);
        page.setUpdatedBy(SecurityUtils.currentUserId());
        pageMapper.updateById(page);
        return ApiResponse.ok(pageMapper.selectById(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('content:page:delete')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        requirePage(id);
        pageMapper.deleteById(id);
        return ApiResponse.ok();
    }

    private void apply(CmsPage page, PagePayload payload, Long currentId) {
        String format = renderService.normalizeFormat(payload.contentFormat());
        if (currentId != null && StringUtils.hasText(page.getContentFormat()) &&
                !page.getContentFormat().equals(format)) {
            throw BusinessException.badRequest("正文格式创建后不能切换，请新建单页");
        }
        String slug = StringUtils.hasText(payload.slug()) ? SlugUtils.slugify(payload.slug()) : SlugUtils.slugify(payload.title());
        if (pageMapper.selectCount(Wrappers.<CmsPage>lambdaQuery()
                .eq(CmsPage::getSlug, slug).ne(currentId != null, CmsPage::getId, currentId)) > 0) {
            throw BusinessException.conflict("页面别名已存在");
        }
        String status = StringUtils.hasText(payload.status()) ? payload.status().toUpperCase() : "DRAFT";
        if (!List.of("DRAFT", "PUBLISHED", "OFFLINE").contains(status)) {
            throw BusinessException.badRequest("页面状态不正确");
        }
        ContentRenderService.RenderedContent rendered = renderService.render(payload.content(), format);
        page.setTitle(payload.title().trim());
        page.setSlug(slug);
        page.setContent(payload.content());
        page.setContentHtml(rendered.html());
        page.setContentText(rendered.text());
        page.setContentFormat(format);
        page.setStatus(status);
        page.setSort(payload.sort() == null ? 0 : payload.sort());
        page.setSeoTitle(payload.seoTitle());
        page.setSeoKeywords(payload.seoKeywords());
        page.setSeoDescription(payload.seoDescription());
        page.setTemplate("page/detail");
    }

    private CmsPage requirePage(Long id) {
        CmsPage page = pageMapper.selectById(id);
        if (page == null) {
            throw BusinessException.notFound("单页不存在");
        }
        return page;
    }
}
