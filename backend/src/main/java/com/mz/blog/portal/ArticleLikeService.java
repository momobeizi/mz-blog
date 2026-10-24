package com.mz.blog.portal;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.mz.blog.common.exception.BusinessException;
import com.mz.blog.content.article.ArticleLikeMapper;
import com.mz.blog.content.article.ArticleMapper;
import com.mz.blog.content.article.CmsArticle;
import com.mz.blog.content.article.CmsArticleLike;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HexFormat;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ArticleLikeService {

    private static final int BURST_LIMIT = 5;
    private static final long BURST_WINDOW_MILLIS = 10_000L;
    private static final int HOURLY_LIMIT = 20;
    private static final int DAILY_LIMIT = 30;
    private static final ZoneId SITE_ZONE = ZoneId.of("Asia/Shanghai");

    private final ArticleMapper articleMapper;
    private final ArticleLikeMapper articleLikeMapper;

    @Value("${app.security.like-ip-salt:mz-blog-like}")
    private String ipSalt;

    private final Cache<String, Deque<Long>> requestHistory = Caffeine.newBuilder()
            .expireAfterAccess(1, TimeUnit.HOURS)
            .maximumSize(100_000)
            .build();

    @Transactional
    public LikeResult like(String slug, String ip) {
        String ipHash = hash(ip);
        if (!allowRequest(ipHash)) {
            throw BusinessException.tooManyRequests("点赞太频繁，请稍后再试");
        }

        CmsArticle article = articleMapper.selectOne(Wrappers.<CmsArticle>lambdaQuery()
                .eq(CmsArticle::getSlug, slug)
                .eq(CmsArticle::getStatus, "PUBLISHED")
                .isNotNull(CmsArticle::getPublishedAt)
                .le(CmsArticle::getPublishedAt, LocalDateTime.now())
                .last("LIMIT 1"));
        if (article == null) {
            throw BusinessException.notFound("文章不存在");
        }

        LocalDate today = LocalDate.now(SITE_ZONE);
        long todayLikes = articleLikeMapper.selectCount(Wrappers.<CmsArticleLike>lambdaQuery()
                .eq(CmsArticleLike::getIpHash, ipHash)
                .eq(CmsArticleLike::getLikeDay, today));
        if (todayLikes >= DAILY_LIMIT) {
            throw BusinessException.tooManyRequests("今天点赞次数已达上限，请明天再来");
        }

        int inserted = articleLikeMapper.insertIgnore(article.getId(), ipHash, today);
        if (inserted == 0) {
            return new LikeResult(likeCount(article.getId()), true, "今天已经点过赞了");
        }

        articleMapper.update(null, Wrappers.<CmsArticle>lambdaUpdate()
                .eq(CmsArticle::getId, article.getId())
                .setSql("like_count = like_count + 1"));
        return new LikeResult(likeCount(article.getId()), true, "感谢你的喜欢");
    }

    private boolean allowRequest(String ipHash) {
        long now = System.currentTimeMillis();
        Deque<Long> history = requestHistory.get(ipHash, key -> new ArrayDeque<>());
        synchronized (history) {
            while (!history.isEmpty() && now - history.peekFirst() >= TimeUnit.HOURS.toMillis(1)) {
                history.pollFirst();
            }
            long recent = history.stream().filter(time -> now - time < BURST_WINDOW_MILLIS).count();
            if (recent >= BURST_LIMIT || history.size() >= HOURLY_LIMIT) {
                return false;
            }
            history.addLast(now);
            return true;
        }
    }

    private long likeCount(Long articleId) {
        CmsArticle current = articleMapper.selectById(articleId);
        return current == null || current.getLikeCount() == null ? 0L : current.getLikeCount();
    }

    private String hash(String ip) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest((ipSalt + ":" + ip).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
    }

    public record LikeResult(long likeCount, boolean liked, String message) {
    }
}
