package com.mz.blog.portal.feed;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
import com.mz.blog.portal.PortalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class FeedController {

    private final PortalService portalService;
    private final ArticleMapper articleMapper;
    private final PageMapper pageMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final AlbumMapper albumMapper;

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public String sitemap() {
        Map<String, String> config = portalService.config();
        String base = config.getOrDefault("site.domain", "http://localhost:8080").replaceAll("/$", "");
        StringBuilder xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");
        for (String path : List.of("/", "/articles", "/archive", "/albums", "/links")) appendUrl(xml, base + path, null);
        for (CmsArticle article : articleMapper.selectList(portalService.publishedQuery().orderByDesc(CmsArticle::getPublishedAt))) {
            appendUrl(xml, base + "/articles/" + article.getSlug(), article.getPublishedAt());
        }
        for (CmsCategory category : categoryMapper.selectList(Wrappers.<CmsCategory>lambdaQuery().eq(CmsCategory::getStatus, 1))) {
            appendUrl(xml, base + "/categories/" + category.getSlug(), null);
        }
        for (CmsTag tag : tagMapper.selectList(null)) appendUrl(xml, base + "/tags/" + tag.getSlug(), null);
        for (CmsPage page : pageMapper.selectList(Wrappers.<CmsPage>lambdaQuery().eq(CmsPage::getStatus, "PUBLISHED"))) {
            appendUrl(xml, base + "/p/" + page.getSlug(), page.getUpdatedAt());
        }
        for (CmsAlbum album : albumMapper.selectList(Wrappers.<CmsAlbum>lambdaQuery().eq(CmsAlbum::getStatus, 1))) {
            appendUrl(xml, base + "/albums/" + album.getSlug(), album.getUpdatedAt());
        }
        xml.append("</urlset>");
        return xml.toString();
    }

    @GetMapping(value = "/rss.xml", produces = "application/rss+xml;charset=UTF-8")
    public String rss() {
        Map<String, String> config = portalService.config();
        String base = config.getOrDefault("site.domain", "http://localhost:8080").replaceAll("/$", "");
        String title = config.getOrDefault("site.name", "mz.blog");
        String description = config.getOrDefault("site.description", "记录、思考与生活");
        List<CmsArticle> articles = articleMapper.selectList(portalService.publishedQuery()
                .orderByDesc(CmsArticle::getPublishedAt).last("LIMIT 20"));
        StringBuilder xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<rss version=\"2.0\"><channel>")
                .append("<title>").append(escape(title)).append("</title>")
                .append("<link>").append(escape(base)).append("</link>")
                .append("<description>").append(escape(description)).append("</description>")
                .append("<language>zh-CN</language>")
                .append("<lastBuildDate>").append(rfcDate(LocalDateTime.now())).append("</lastBuildDate>");
        for (CmsArticle article : articles) {
            String url = base + "/articles/" + article.getSlug();
            xml.append("<item><title>").append(escape(article.getTitle())).append("</title>")
                    .append("<link>").append(escape(url)).append("</link>")
                    .append("<guid>").append(escape(url)).append("</guid>")
                    .append("<description>").append(escape(article.getSummary() == null ? "" : article.getSummary())).append("</description>")
                    .append("<pubDate>").append(rfcDate(article.getPublishedAt())).append("</pubDate></item>");
        }
        xml.append("</channel></rss>");
        return xml.toString();
    }

    @GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    public String robots() {
        String base = portalService.config().getOrDefault("site.domain", "http://localhost:8080").replaceAll("/$", "");
        return "User-agent: *\nAllow: /\nDisallow: /admin/\nDisallow: /api/\nSitemap: " + base + "/sitemap.xml\n";
    }

    private void appendUrl(StringBuilder xml, String url, LocalDateTime lastmod) {
        xml.append("<url><loc>").append(escape(url)).append("</loc>");
        if (lastmod != null) xml.append("<lastmod>").append(lastmod.toLocalDate()).append("</lastmod>");
        xml.append("</url>\n");
    }

    private String rfcDate(LocalDateTime date) {
        LocalDateTime value = date == null ? LocalDateTime.now() : date;
        return DateTimeFormatter.RFC_1123_DATE_TIME.format(value.atZone(ZoneId.of("Asia/Shanghai")));
    }

    private String escape(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }
}
