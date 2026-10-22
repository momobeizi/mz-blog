package com.mz.blog.dashboard;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mz.blog.common.api.ApiResponse;
import com.mz.blog.content.album.AlbumMapper;
import com.mz.blog.content.album.CmsAlbum;
import com.mz.blog.content.article.ArticleMapper;
import com.mz.blog.content.article.CmsArticle;
import com.mz.blog.content.category.CategoryMapper;
import com.mz.blog.content.category.CmsCategory;
import com.mz.blog.content.page.CmsPage;
import com.mz.blog.content.page.PageMapper;
import com.mz.blog.content.tag.CmsTag;
import com.mz.blog.content.tag.TagMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ArticleMapper articleMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final PageMapper pageMapper;
    private final AlbumMapper albumMapper;

    @GetMapping
    @PreAuthorize("hasAuthority('dashboard:view')")
    public ApiResponse<Map<String, Object>> dashboard() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("articleCount", articleMapper.selectCount(null));
        data.put("publishedCount", articleMapper.selectCount(Wrappers.<CmsArticle>lambdaQuery()
                .eq(CmsArticle::getStatus, "PUBLISHED")));
        data.put("draftCount", articleMapper.selectCount(Wrappers.<CmsArticle>lambdaQuery()
                .eq(CmsArticle::getStatus, "DRAFT")));
        data.put("pageCount", pageMapper.selectCount(Wrappers.<CmsPage>lambdaQuery()
                .eq(CmsPage::getStatus, "PUBLISHED")));
        data.put("categoryCount", categoryMapper.selectCount(Wrappers.<CmsCategory>lambdaQuery()
                .eq(CmsCategory::getStatus, 1)));
        data.put("tagCount", tagMapper.selectCount(null));
        data.put("albumCount", albumMapper.selectCount(Wrappers.<CmsAlbum>lambdaQuery()
                .eq(CmsAlbum::getStatus, 1)));
        List<CmsArticle> recent = articleMapper.selectList(Wrappers.<CmsArticle>lambdaQuery()
                .orderByDesc(CmsArticle::getCreatedAt).last("LIMIT 6"));
        Map<Long, String> categoryNames = new LinkedHashMap<>();
        for (CmsCategory category : categoryMapper.selectList(null)) {
            categoryNames.put(category.getId(), category.getName());
        }
        recent.forEach(article -> article.setCategoryName(
                categoryNames.getOrDefault(article.getCategoryId(), "未分类")));
        data.put("recentArticles", recent);
        return ApiResponse.ok(data);
    }
}
