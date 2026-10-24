package com.mz.blog.content.article;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("cms_article_like")
public class CmsArticleLike {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long articleId;
    private String ipHash;
    private LocalDate likeDay;
    private LocalDateTime createdAt;
}
