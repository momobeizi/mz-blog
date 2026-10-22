package com.mz.blog.content.album;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@TableName("cms_album")
public class CmsAlbum {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String slug;
    private String coverUrl;
    private String description;
    private Integer sort;
    private Integer status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
    @TableField(exist = false)
    private List<CmsAlbumPhoto> photos = new ArrayList<>();
}
