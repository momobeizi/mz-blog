package com.mz.blog.content.link;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("cms_link")
public class CmsLink {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String linkType;
    private String groupName;
    private String name;
    private String url;
    private String logoUrl;
    private String description;
    private Integer sort;
    private Integer nofollow;
    private String openType;
    private Integer status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
