package com.mz.blog.content.page;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("cms_page")
public class CmsPage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String slug;
    private String content;
    private String contentHtml;
    private String contentText;
    private String contentFormat;
    private String template;
    private String status;
    private Integer sort;
    private String seoTitle;
    private String seoKeywords;
    private String seoDescription;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
