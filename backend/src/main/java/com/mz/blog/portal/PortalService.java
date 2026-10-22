package com.mz.blog.portal;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mz.blog.content.album.AlbumMapper;
import com.mz.blog.content.album.CmsAlbum;
import com.mz.blog.content.article.ArticleMapper;
import com.mz.blog.content.article.CmsArticle;
import com.mz.blog.content.category.CategoryMapper;
import com.mz.blog.content.category.CmsCategory;
import com.mz.blog.content.link.CmsLink;
import com.mz.blog.content.link.LinkMapper;
import com.mz.blog.content.nav.CmsNavMenu;
import com.mz.blog.content.nav.NavMenuMapper;
import com.mz.blog.content.page.CmsPage;
import com.mz.blog.content.page.PageMapper;
import com.mz.blog.content.tag.CmsTag;
import com.mz.blog.content.tag.TagMapper;
import com.mz.blog.system.config.SiteConfigService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PortalService {

    private final SiteConfigService siteConfigService;
    private final NavMenuMapper navMenuMapper;
    private final ArticleMapper articleMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final AlbumMapper albumMapper;
    private final PageMapper pageMapper;
    private final LinkMapper linkMapper;

    public Map<String, String> config() {
        return siteConfigService.publicConfig();
    }

    public void base(Model model, HttpServletRequest request, String title, String description, String image) {
        Map<String, String> config = config();
        String siteName = siteConfigService.get(config, "site.name", "mz.blog");
        String defaultDescription = siteConfigService.get(config, "site.description", "记录、思考与生活");
        String seoDescription = StringUtils.hasText(description) ? description
                : siteConfigService.get(config, "seo.description", defaultDescription);
        String domain = siteConfigService.get(config, "site.domain", "http://localhost:8080");
        model.addAttribute("site", config);
        model.addAttribute("navMenus", navTree());
        model.addAttribute("currentPath", request.getRequestURI());
        model.addAttribute("seoTitle", StringUtils.hasText(title) ? title + " · " + siteName : siteName);
        model.addAttribute("seoDescription", seoDescription);
        model.addAttribute("seoKeywords", siteConfigService.get(config, "seo.keywords", ""));
        model.addAttribute("ogImage", StringUtils.hasText(image) ? image : siteConfigService.get(config, "seo.og_image", "/assets/covers/cover-1.svg"));
        model.addAttribute("canonical", domain + request.getRequestURI());
        model.addAttribute("currentYear", LocalDateTime.now().getYear());
        model.addAttribute("articleCount", articleMapper.selectCount(publishedQuery()));
        model.addAttribute("categoryCount", categoryMapper.selectCount(Wrappers.<CmsCategory>lambdaQuery().eq(CmsCategory::getStatus, 1)));
        model.addAttribute("tagCount", tagMapper.selectCount(null));
        model.addAttribute("aboutPage", publishedPage("about"));
        model.addAttribute("footerLinks", links("COMMON"));
        model.addAttribute("friendLinks", links("FRIEND"));
        model.addAttribute("featureSearch", siteConfigService.bool(config, "feature.search", true));
        model.addAttribute("featureRss", siteConfigService.bool(config, "feature.rss", true));
        model.addAttribute("featureViewCount", siteConfigService.bool(config, "feature.view_count", true));
    }

    public List<CmsNavMenu> navTree() {
        List<CmsNavMenu> menus = navMenuMapper.selectList(Wrappers.<CmsNavMenu>lambdaQuery()
                .eq(CmsNavMenu::getStatus, 1)
                .eq(CmsNavMenu::getVisible, 1)
                .orderByAsc(CmsNavMenu::getSort).orderByAsc(CmsNavMenu::getId));
        menus.forEach(this::resolveNav);
        Map<Long, CmsNavMenu> byId = new LinkedHashMap<>();
        menus.forEach(menu -> {
            menu.setChildren(new ArrayList<>());
            byId.put(menu.getId(), menu);
        });
        List<CmsNavMenu> roots = new ArrayList<>();
        for (CmsNavMenu menu : menus) {
            CmsNavMenu parent = byId.get(menu.getParentId());
            if (parent == null || menu.getParentId() == 0) roots.add(menu);
            else parent.getChildren().add(menu);
        }
        roots.sort(Comparator.comparing(CmsNavMenu::getSort).thenComparing(CmsNavMenu::getId));
        return roots;
    }

    public CmsCategory categoryBySlug(String slug) {
        return categoryMapper.selectOne(Wrappers.<CmsCategory>lambdaQuery()
                .eq(CmsCategory::getSlug, slug).eq(CmsCategory::getStatus, 1).last("LIMIT 1"));
    }

    public CmsTag tagBySlug(String slug) {
        return tagMapper.selectOne(Wrappers.<CmsTag>lambdaQuery().eq(CmsTag::getSlug, slug).last("LIMIT 1"));
    }

    public List<CmsCategory> categories() {
        return categoryMapper.selectList(Wrappers.<CmsCategory>lambdaQuery()
                .eq(CmsCategory::getStatus, 1).orderByAsc(CmsCategory::getSort));
    }

    public List<CmsTag> tags() {
        return tagMapper.selectList(Wrappers.<CmsTag>lambdaQuery().orderByAsc(CmsTag::getId));
    }

    public List<CmsLink> links(String type) {
        return linkMapper.selectList(Wrappers.<CmsLink>lambdaQuery()
                .eq(CmsLink::getLinkType, type).eq(CmsLink::getStatus, 1)
                .orderByAsc(CmsLink::getSort).orderByAsc(CmsLink::getId));
    }

    public Map<String, List<CmsLink>> linksByGroup(String type) {
        Map<String, List<CmsLink>> result = new LinkedHashMap<>();
        links(type).forEach(link -> result.computeIfAbsent(link.getGroupName(), key -> new ArrayList<>()).add(link));
        return result;
    }

    public CmsPage publishedPage(String slug) {
        return pageMapper.selectOne(Wrappers.<CmsPage>lambdaQuery()
                .eq(CmsPage::getSlug, slug).eq(CmsPage::getStatus, "PUBLISHED").last("LIMIT 1"));
    }

    public List<CmsAlbum> albumsWithPhotos() {
        List<CmsAlbum> albums = albumMapper.selectList(Wrappers.<CmsAlbum>lambdaQuery()
                .eq(CmsAlbum::getStatus, 1).orderByAsc(CmsAlbum::getSort));
        return albums;
    }

    public com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CmsArticle> publishedQuery() {
        return Wrappers.<CmsArticle>lambdaQuery()
                .eq(CmsArticle::getStatus, "PUBLISHED")
                .isNotNull(CmsArticle::getPublishedAt)
                .le(CmsArticle::getPublishedAt, LocalDateTime.now());
    }

    private void resolveNav(CmsNavMenu menu) {
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
            default -> StringUtils.hasText(menu.getUrl()) ? menu.getUrl() : "/";
        });
    }
}
