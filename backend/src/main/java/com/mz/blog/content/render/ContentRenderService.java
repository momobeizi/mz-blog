package com.mz.blog.content.render;

import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.footnotes.FootnoteExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class ContentRenderService {

    public enum Format {
        HTML,
        MARKDOWN
    }

    public record RenderedContent(String html, String text) {
    }

    private final Parser markdownParser;
    private final HtmlRenderer markdownRenderer;
    private final PolicyFactory policy;

    public ContentRenderService() {
        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, List.of(
                TablesExtension.create(),
                StrikethroughExtension.create(),
                TaskListExtension.create(),
                AutolinkExtension.create(),
                FootnoteExtension.create()));
        options.set(HtmlRenderer.SUPPRESS_HTML, true);
        options.set(HtmlRenderer.GENERATE_HEADER_ID, true);
        this.markdownParser = Parser.builder(options).build();
        this.markdownRenderer = HtmlRenderer.builder(options).build();
        this.policy = buildPolicy();
    }

    public RenderedContent render(String content, String format) {
        String source = content == null ? "" : content;
        String html;
        if (Format.MARKDOWN.name().equalsIgnoreCase(format)) {
            html = markdownRenderer.render(markdownParser.parse(source));
        } else {
            html = source;
        }
        Document document = Jsoup.parseBodyFragment(policy.sanitize(html == null ? "" : html));
        document.outputSettings().prettyPrint(false);
        document.select("img").forEach(image -> {
            image.attr("loading", "lazy");
            image.attr("decoding", "async");
        });
        document.select("a[href]").forEach(link -> {
            String href = link.attr("href");
            if (href.startsWith("http://") || href.startsWith("https://")) {
                link.attr("rel", "noopener noreferrer");
                link.attr("target", "_blank");
            }
        });
        String text = document.body().text().replace('\u00a0', ' ').replaceAll("\\s+", " ").trim();
        return new RenderedContent(document.body().html(), text);
    }

    public String normalizeFormat(String format) {
        return Format.MARKDOWN.name().equalsIgnoreCase(format) ? Format.MARKDOWN.name() : Format.HTML.name();
    }

    public String excerpt(String text, int maxLength) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        String normalized = text.replaceAll("\\s+", " ").trim();
        return normalized.length() <= maxLength ? normalized : normalized.substring(0, maxLength) + "...";
    }

    private PolicyFactory buildPolicy() {
        return new HtmlPolicyBuilder()
                .allowElements(
                        "p", "br", "hr", "div", "span", "strong", "b", "em", "i", "u", "s", "del",
                        "blockquote", "pre", "code", "kbd", "mark", "small", "sub", "sup",
                        "h1", "h2", "h3", "h4", "h5", "h6",
                        "ul", "ol", "li", "dl", "dt", "dd",
                        "a", "img", "figure", "figcaption",
                        "table", "thead", "tbody", "tfoot", "tr", "th", "td",
                        "input", "details", "summary", "video", "audio", "source")
                .allowAttributes("href", "title", "target", "rel", "class")
                .onElements("a")
                .allowAttributes("src", "alt", "title", "width", "height", "class")
                .onElements("img")
                .allowAttributes("class", "id")
                .onElements("h1", "h2", "h3", "h4", "h5", "h6", "p", "div", "span", "pre", "code", "table", "ul", "ol")
                .allowAttributes("class", "type", "checked", "disabled")
                .onElements("input")
                .allowAttributes("colspan", "rowspan", "scope")
                .onElements("th", "td")
                .allowAttributes("controls", "preload", "poster", "width", "height")
                .onElements("video")
                .allowAttributes("controls", "preload")
                .onElements("audio")
                .allowAttributes("src", "type")
                .onElements("source")
                .allowUrlProtocols("http", "https")
                .allowStandardUrlProtocols()
                .requireRelNofollowOnLinks()
                .toFactory();
    }
}
