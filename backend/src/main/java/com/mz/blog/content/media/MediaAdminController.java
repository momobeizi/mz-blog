package com.mz.blog.content.media;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mz.blog.common.api.ApiResponse;
import com.mz.blog.common.api.PageResult;
import com.mz.blog.common.exception.BusinessException;
import com.mz.blog.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/v1/content/media")
@RequiredArgsConstructor
public class MediaAdminController {

    private final MediaMapper mediaMapper;
    private final StorageService storageService;

    @GetMapping
    @PreAuthorize("hasAuthority('content:media:list')")
    public ApiResponse<PageResult<CmsMedia>> page(@RequestParam(defaultValue = "1") long page,
                                                   @RequestParam(defaultValue = "24") long size) {
        IPage<CmsMedia> result = mediaMapper.selectPage(new Page<>(page, Math.min(size, 100)),
                Wrappers.<CmsMedia>lambdaQuery().orderByDesc(CmsMedia::getCreatedAt));
        return ApiResponse.ok(PageResult.of(result.getRecords(), result.getCurrent(), result.getSize(), result.getTotal()));
    }

    @PostMapping("/upload")
    @PreAuthorize("hasAuthority('content:media:upload')")
    public ApiResponse<CmsMedia> upload(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(storageService.storeImage(file));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('content:media:delete')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        if (mediaMapper.selectById(id) == null) {
            throw BusinessException.notFound("媒体不存在");
        }
        mediaMapper.deleteById(id);
        return ApiResponse.ok();
    }
}
