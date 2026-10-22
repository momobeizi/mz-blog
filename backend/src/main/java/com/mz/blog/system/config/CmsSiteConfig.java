package com.mz.blog.system.config;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("cms_site_config")
public class CmsSiteConfig {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String configKey;
    private String configValue;
    private String valueType;
    private String groupCode;
    private String label;
    private String description;
    private Integer isPublic;
    private Integer sort;
    private Long updatedBy;
    private LocalDateTime updatedAt;
}
