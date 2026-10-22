package com.mz.blog.content.album;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("cms_album_photo")
public class CmsAlbumPhoto {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long albumId;
    private Long mediaId;
    private String imageUrl;
    private String thumbUrl;
    private String caption;
    private Integer sort;
    private LocalDateTime createdAt;
    @TableLogic
    private Integer deleted;
}
