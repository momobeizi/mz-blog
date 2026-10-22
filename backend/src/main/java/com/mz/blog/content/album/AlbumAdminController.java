package com.mz.blog.content.album;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mz.blog.common.api.ApiResponse;
import com.mz.blog.common.exception.BusinessException;
import com.mz.blog.common.util.SecurityUtils;
import com.mz.blog.common.util.SlugUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/admin/v1/content/albums")
@RequiredArgsConstructor
public class AlbumAdminController {

    private final AlbumMapper albumMapper;
    private final AlbumPhotoMapper albumPhotoMapper;

    public record PhotoPayload(Long mediaId, @NotBlank String imageUrl, String thumbUrl,
                               String caption, Integer sort) {
    }

    public record AlbumPayload(@NotBlank String title, String slug, String coverUrl, String description,
                               Integer sort, Integer status, List<PhotoPayload> photos) {
    }

    @GetMapping
    @PreAuthorize("hasAuthority('content:album:list')")
    public ApiResponse<List<CmsAlbum>> list() {
        List<CmsAlbum> albums = albumMapper.selectList(Wrappers.<CmsAlbum>lambdaQuery()
                .orderByAsc(CmsAlbum::getSort).orderByDesc(CmsAlbum::getCreatedAt));
        albums.forEach(album -> album.setPhotos(albumPhotoMapper.selectList(Wrappers.<CmsAlbumPhoto>lambdaQuery()
                .eq(CmsAlbumPhoto::getAlbumId, album.getId()).orderByAsc(CmsAlbumPhoto::getSort))));
        return ApiResponse.ok(albums);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('content:album:list')")
    public ApiResponse<CmsAlbum> detail(@PathVariable Long id) {
        CmsAlbum album = requireAlbum(id);
        album.setPhotos(albumPhotoMapper.selectList(Wrappers.<CmsAlbumPhoto>lambdaQuery()
                .eq(CmsAlbumPhoto::getAlbumId, id).orderByAsc(CmsAlbumPhoto::getSort)));
        return ApiResponse.ok(album);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('content:album:create')")
    @Transactional
    public ApiResponse<CmsAlbum> create(@Valid @RequestBody AlbumPayload payload) {
        CmsAlbum album = new CmsAlbum();
        apply(album, payload, null);
        album.setCreatedBy(SecurityUtils.currentUserId());
        albumMapper.insert(album);
        savePhotos(album.getId(), payload.photos());
        return ApiResponse.ok(albumMapper.selectById(album.getId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('content:album:update')")
    @Transactional
    public ApiResponse<CmsAlbum> update(@PathVariable Long id, @Valid @RequestBody AlbumPayload payload) {
        CmsAlbum album = requireAlbum(id);
        apply(album, payload, id);
        album.setUpdatedBy(SecurityUtils.currentUserId());
        albumMapper.updateById(album);
        savePhotos(id, payload.photos());
        return ApiResponse.ok(albumMapper.selectById(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('content:album:delete')")
    @Transactional
    public ApiResponse<Void> delete(@PathVariable Long id) {
        requireAlbum(id);
        albumPhotoMapper.delete(Wrappers.<CmsAlbumPhoto>lambdaQuery().eq(CmsAlbumPhoto::getAlbumId, id));
        albumMapper.deleteById(id);
        return ApiResponse.ok();
    }

    private void apply(CmsAlbum album, AlbumPayload payload, Long currentId) {
        String slug = StringUtils.hasText(payload.slug()) ? SlugUtils.slugify(payload.slug()) : SlugUtils.slugify(payload.title());
        if (albumMapper.selectCount(Wrappers.<CmsAlbum>lambdaQuery()
                .eq(CmsAlbum::getSlug, slug).ne(currentId != null, CmsAlbum::getId, currentId)) > 0) {
            throw BusinessException.conflict("相册别名已存在");
        }
        album.setTitle(payload.title().trim());
        album.setSlug(slug);
        album.setDescription(payload.description());
        album.setSort(payload.sort() == null ? 0 : payload.sort());
        album.setStatus(payload.status() == null ? 1 : payload.status());
        String cover = payload.coverUrl();
        if (!StringUtils.hasText(cover) && payload.photos() != null && !payload.photos().isEmpty()) {
            cover = payload.photos().get(0).imageUrl();
        }
        album.setCoverUrl(StringUtils.hasText(cover) ? cover : "/assets/covers/cover-3.svg");
    }

    private void savePhotos(Long albumId, List<PhotoPayload> photos) {
        albumPhotoMapper.delete(Wrappers.<CmsAlbumPhoto>lambdaQuery().eq(CmsAlbumPhoto::getAlbumId, albumId));
        if (photos == null) {
            return;
        }
        int index = 0;
        for (PhotoPayload photo : new ArrayList<>(photos)) {
            CmsAlbumPhoto entity = new CmsAlbumPhoto();
            entity.setAlbumId(albumId);
            entity.setMediaId(photo.mediaId() == null ? 0L : photo.mediaId());
            entity.setImageUrl(photo.imageUrl());
            entity.setThumbUrl(StringUtils.hasText(photo.thumbUrl()) ? photo.thumbUrl() : photo.imageUrl());
            entity.setCaption(photo.caption());
            entity.setSort(photo.sort() == null ? index * 10 : photo.sort());
            albumPhotoMapper.insert(entity);
            index++;
        }
    }

    private CmsAlbum requireAlbum(Long id) {
        CmsAlbum album = albumMapper.selectById(id);
        if (album == null) {
            throw BusinessException.notFound("相册不存在");
        }
        return album;
    }
}
