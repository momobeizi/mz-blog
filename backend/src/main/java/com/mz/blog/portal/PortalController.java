package com.mz.blog.portal;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mz.blog.common.api.PageResult;
import com.mz.blog.content.album.AlbumPhotoMapper;
import com.mz.blog.content.album.CmsAlbum;
import com.mz.blog.content.album.CmsAlbumPhoto;
import com.mz.blog.content.article.ArticleMapper;
import com.mz.blog.content.article.CmsArticle;
import com.mz.blog.content.category.CmsCategory;
import com.mz.blog.content.page.CmsPage;
import com.mz.blog.content.tag.CmsTag;
import com.mz.blog.system.user.SysUser;
import com.mz.blog.system.user.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class PortalController {

    private final PortalService portalService;
    private final ArticleMapper articleMapper;
    private final com.mz.blog.content.article.ArticleTagMapper articleTagMapper;
    private final UserMapper userMapper;
    private final AlbumPhotoMapper albumPhotoMapper;

    @GetMapping("/")
    public String home(Model model, HttpServletRequest request) {
        portalService.base(model, request, null, null, null);
        List<CmsArticle> latest = articleMapper.selectList(portalService.publishedQuery()
                .orderByDesc(CmsArticle::getIsTop).orderByDesc(CmsArticle::getPublishedAt).last("LIMIT 9"));
        enrich(latest);
        Map<Long, List<CmsArticle>> byCategory = new LinkedHashMap<>();
        List<CmsCategory> categories = portalService.categories();
        for (CmsCategory category : categories.stream().limit(3).toList()) {
            List<CmsArticle> items = articleMapper.selectList(portalService.publishedQuery()
                    .eq(CmsArticle::getCategoryId, category.getId())
                    .orderByDesc(CmsArticle::getPublishedAt).last("LIMIT 3"));
            if (!items.isEmpty()) {
                enrich(items);
                byCategory.put(category.getId(), items);
            }
        }
        model.addAttribute("latestArticles", latest);
        model.addAttribute("categories", categories);
        model.addAttribute("categoryArticles", byCategory);
        model.addAttribute("tags", portalService.tags());
        model.addAttribute("heroTitle", portalService.config().getOrDefault("site.hero_title", "相信记录的力量"));
        model.addAttribute("heroSubtitle", portalService.config().getOrDefault("site.hero_subtitle", "mz.blog · 个人数字花园"));
        return "portal/index";
    }

    @GetMapping("/articles")
    public String articles(@RequestParam(defaultValue = "1") int page,
                           @RequestParam(required = false) String keyword,
                           @RequestParam(required = false) Long categoryId,
                           @RequestParam(required = false) Long tagId,
                           Model model, HttpServletRequest request) {
        portalService.base(model, request, StringUtils.hasText(keyword) ? "搜索：" + keyword : "文章与片段", null, null);
        return articleList(page, keyword, categoryId, tagId, null, model, request);
    }

    @GetMapping("/categories/{slug}")
    public String category(@PathVariable String slug, @RequestParam(defaultValue = "1") int page,
                           Model model, HttpServletRequest request) {
        CmsCategory category = portalService.categoryBySlug(slug);
        if (category == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        portalService.base(model, request, category.getName(), category.getDescription(), category.getCoverUrl());
        return articleList(page, null, category.getId(), null, category, model, request);
    }

    @GetMapping("/tags/{slug}")
    public String tag(@PathVariable String slug, @RequestParam(defaultValue = "1") int page,
                      Model model, HttpServletRequest request) {
        CmsTag tag = portalService.tagBySlug(slug);
        if (tag == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        portalService.base(model, request, "#" + tag.getName(), tag.getDescription(), null);
        return articleList(page, null, null, tag.getId(), null, model, request);
    }

    @GetMapping("/search")
    public String search(@RequestParam(defaultValue = "") String q, @RequestParam(defaultValue = "1") int page,
                         Model model, HttpServletRequest request) {
        portalService.base(model, request, StringUtils.hasText(q) ? "搜索：" + q : "搜索", null, null);
        return articleList(page, q, null, null, null, model, request);
    }

    private String articleList(int page, String keyword, Long categoryId, Long tagId, CmsCategory category,
                               Model model, HttpServletRequest request) {
        Page<CmsArticle> pageRequest = new Page<>(Math.max(page, 1), 9);
        IPage<CmsArticle> result = articleMapper.selectPage(pageRequest, portalService.publishedQuery()
                .and(StringUtils.hasText(keyword), q -> q.like(CmsArticle::getTitle, keyword)
                        .or().like(CmsArticle::getSummary, keyword).or().like(CmsArticle::getContentText, keyword))
                .eq(categoryId != null, CmsArticle::getCategoryId, categoryId)
                .inSql(tagId != null, CmsArticle::getId,
                        "SELECT article_id FROM cms_article_tag WHERE tag_id = " + (tagId == null ? 0 : tagId))
                .orderByDesc(CmsArticle::getIsTop).orderByDesc(CmsArticle::getPublishedAt));
        enrich(result.getRecords());
        model.addAttribute("articles", result.getRecords());
        model.addAttribute("pageData", PageResult.of(result.getRecords(), result.getCurrent(), result.getSize(), result.getTotal()));
        model.addAttribute("categories", portalService.categories());
        model.addAttribute("tags", portalService.tags());
        model.addAttribute("currentCategory", category);
        model.addAttribute("keyword", keyword);
        model.addAttribute("paginationBase", request.getRequestURI());
        model.addAttribute("paginationQuery", StringUtils.hasText(keyword) ? "&q=" + java.net.URLEncoder.encode(keyword, java.nio.charset.StandardCharsets.UTF_8) : "");
        return "portal/article/list";
    }

    @GetMapping("/articles/{slug}")
    public String article(@PathVariable String slug, Model model, HttpServletRequest request) {
        CmsArticle article = articleMapper.selectOne(portalService.publishedQuery()
                .eq(CmsArticle::getSlug, slug).last("LIMIT 1"));
        if (article == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        enrich(List.of(article));
        CmsCategory category = article.getCategoryId() == null ? null : portalService.categories().stream()
                .filter(item -> item.getId().equals(article.getCategoryId())).findFirst().orElse(null);
        SysUser author = userMapper.selectById(article.getAuthorId());
        List<Long> tagIds = article.getTagIds();
        List<CmsTag> articleTags = portalService.tags().stream().filter(tag -> tagIds.contains(tag.getId())).toList();
        List<CmsArticle> related = articleMapper.selectList(portalService.publishedQuery()
                .eq(CmsArticle::getCategoryId, article.getCategoryId()).ne(CmsArticle::getId, article.getId())
                .orderByDesc(CmsArticle::getPublishedAt).last("LIMIT 3"));
        enrich(related);
        CmsArticle previous = articleMapper.selectOne(portalService.publishedQuery()
                .lt(CmsArticle::getPublishedAt, article.getPublishedAt())
                .orderByDesc(CmsArticle::getPublishedAt).last("LIMIT 1"));
        CmsArticle next = articleMapper.selectOne(portalService.publishedQuery()
                .gt(CmsArticle::getPublishedAt, article.getPublishedAt())
                .orderByAsc(CmsArticle::getPublishedAt).last("LIMIT 1"));
        articleMapper.update(null, Wrappers.<CmsArticle>lambdaUpdate()
                .eq(CmsArticle::getId, article.getId()).setSql("view_count = view_count + 1"));
        article.setViewCount(article.getViewCount() + 1);
        portalService.base(model, request, article.getSeoTitle() == null ? article.getTitle() : article.getSeoTitle(),
                article.getSeoDescription() == null ? article.getSummary() : article.getSeoDescription(), article.getCoverUrl());
        model.addAttribute("article", article);
        model.addAttribute("category", category);
        model.addAttribute("author", author);
        model.addAttribute("articleTags", articleTags);
        model.addAttribute("relatedArticles", related);
        model.addAttribute("previousArticle", previous);
        model.addAttribute("nextArticle", next);
        return "portal/article/detail";
    }

    @GetMapping("/archive")
    public String archive(Model model, HttpServletRequest request) {
        portalService.base(model, request, "归档", "按时间浏览全部文章", null);
        List<CmsArticle> articles = articleMapper.selectList(portalService.publishedQuery()
                .orderByDesc(CmsArticle::getPublishedAt));
        enrich(articles);
        Map<String, List<CmsArticle>> groups = new TreeMap<>(Comparator.reverseOrder());
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy 年 MM 月");
        for (CmsArticle article : articles) {
            String key = article.getPublishedAt() == null ? "未发布" : article.getPublishedAt().format(format);
            groups.computeIfAbsent(key, ignored -> new ArrayList<>()).add(article);
        }
        model.addAttribute("archiveGroups", groups);
        model.addAttribute("total", articles.size());
        return "portal/archive";
    }

    @GetMapping("/p/{slug}")
    public String page(@PathVariable String slug, Model model, HttpServletRequest request) {
        CmsPage page = portalService.publishedPage(slug);
        if (page == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        portalService.base(model, request, page.getTitle(), page.getSeoDescription(), null);
        model.addAttribute("page", page);
        return "portal/page/detail";
    }

    @GetMapping("/albums")
    public String albums(Model model, HttpServletRequest request) {
        portalService.base(model, request, "相册", "光线、街道与日常的片刻", "/assets/covers/cover-3.svg");
        List<CmsAlbum> albums = portalService.albumsWithPhotos();
        Map<Long, List<CmsAlbumPhoto>> photos = new LinkedHashMap<>();
        albums.forEach(album -> photos.put(album.getId(), albumPhotoMapper.selectList(Wrappers.<CmsAlbumPhoto>lambdaQuery()
                .eq(CmsAlbumPhoto::getAlbumId, album.getId()).orderByAsc(CmsAlbumPhoto::getSort).last("LIMIT 1"))));
        model.addAttribute("albums", albums);
        model.addAttribute("albumPhotos", photos);
        return "portal/album/list";
    }

    @GetMapping("/albums/{slug}")
    public String album(@PathVariable String slug, Model model, HttpServletRequest request) {
        CmsAlbum album = portalService.albumsWithPhotos().stream().filter(item -> slug.equals(item.getSlug())).findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        List<CmsAlbumPhoto> photos = albumPhotoMapper.selectList(Wrappers.<CmsAlbumPhoto>lambdaQuery()
                .eq(CmsAlbumPhoto::getAlbumId, album.getId()).orderByAsc(CmsAlbumPhoto::getSort));
        album.setPhotos(photos);
        portalService.base(model, request, album.getTitle(), album.getDescription(), album.getCoverUrl());
        model.addAttribute("album", album);
        return "portal/album/detail";
    }

    @GetMapping("/links")
    public String links(Model model, HttpServletRequest request) {
        portalService.base(model, request, "友链与常用入口", "互联网上值得再去的地方", null);
        model.addAttribute("friends", portalService.linksByGroup("FRIEND"));
        model.addAttribute("commons", portalService.linksByGroup("COMMON"));
        return "portal/link/list";
    }

    private void enrich(List<CmsArticle> articles) {
        if (articles == null || articles.isEmpty()) return;
        Map<Long, CmsCategory> categories = portalService.categories().stream()
                .collect(Collectors.toMap(CmsCategory::getId, Function.identity()));
        Map<Long, SysUser> users = userMapper.selectList(null).stream()
                .collect(Collectors.toMap(SysUser::getId, Function.identity()));
        Map<Long, CmsTag> tags = portalService.tags().stream().collect(Collectors.toMap(CmsTag::getId, Function.identity()));
        for (CmsArticle article : articles) {
            CmsCategory category = categories.get(article.getCategoryId());
            article.setCategoryName(category == null ? "-" : category.getName());
            article.setCategorySlug(category == null ? null : category.getSlug());
            SysUser author = users.get(article.getAuthorId());
            article.setAuthorName(author == null ? "-" : author.getNickname());
            List<Long> tagIds = articleTagMapper.selectTagIds(article.getId());
            article.setTagIds(tagIds);
            article.setTagNames(tagIds.stream().map(tags::get).filter(Objects::nonNull).map(CmsTag::getName).toList());
        }
    }
}
