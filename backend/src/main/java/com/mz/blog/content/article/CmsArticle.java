package com.mz.blog.content.article;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@TableName("cms_article")
public class CmsArticle {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String slug;
    private String summary;
    private String coverUrl;
    private String content;
    private String contentHtml;
    private String contentText;
    private String contentFormat;
    private Long categoryId;
    private Long authorId;
    private String status;
    private Integer isTop;
    private Integer isRecommended;
    private Long viewCount;
    private Long likeCount;
    private LocalDateTime publishedAt;
    private String seoTitle;
    private String seoKeywords;
    private String seoDescription;
    @Version
    private Integer version;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
    @TableField(exist = false)
    private String categoryName;
    @TableField(exist = false)
    private String categorySlug;
    @TableField(exist = false)
    private String authorName;
    @TableField(exist = false)
    private Integer wordCount;
    @TableField(exist = false)
    private Integer readingMinutes;
    @TableField(exist = false)
    private List<Long> tagIds = new ArrayList<>();
    @TableField(exist = false)
    private List<String> tagNames = new ArrayList<>();
}
