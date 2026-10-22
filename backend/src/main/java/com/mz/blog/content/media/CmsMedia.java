package com.mz.blog.content.media;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("cms_media")
public class CmsMedia {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String originalName;
    private String storageKey;
    private String url;
    private String thumbUrl;
    private String mimeType;
    private String extension;
    private Long sizeBytes;
    private Integer width;
    private Integer height;
    private String sha256;
    private Long createdBy;
    private LocalDateTime createdAt;
    @TableLogic
    private Integer deleted;
}
