package com.mz.blog.content.article;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ArticleTagMapper {

    @Select("SELECT tag_id FROM cms_article_tag WHERE article_id = #{articleId}")
    List<Long> selectTagIds(Long articleId);

    @Delete("DELETE FROM cms_article_tag WHERE article_id = #{articleId}")
    int deleteByArticleId(Long articleId);

    @Insert("INSERT INTO cms_article_tag (article_id, tag_id) VALUES (#{articleId}, #{tagId})")
    int insert(Long articleId, Long tagId);

    @Select("SELECT COUNT(*) FROM cms_article_tag WHERE tag_id = #{tagId}")
    long countByTagId(Long tagId);
}
