package com.mz.blog.content.article;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;

public interface ArticleLikeMapper extends BaseMapper<CmsArticleLike> {

    @Insert("INSERT IGNORE INTO cms_article_like (article_id, ip_hash, like_day, created_at) " +
            "VALUES (#{articleId}, #{ipHash}, #{likeDay}, NOW())")
    int insertIgnore(@Param("articleId") Long articleId,
                     @Param("ipHash") String ipHash,
                     @Param("likeDay") LocalDate likeDay);
}
