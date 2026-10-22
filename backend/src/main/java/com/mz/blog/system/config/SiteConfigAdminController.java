package com.mz.blog.system.config;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mz.blog.common.api.ApiResponse;
import com.mz.blog.common.exception.BusinessException;
import com.mz.blog.common.util.SecurityUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/v1/settings/site")
@RequiredArgsConstructor
public class SiteConfigAdminController {

    private final SiteConfigMapper siteConfigMapper;

    public record ConfigValue(@NotBlank String configKey, String configValue) {
    }

    public record SavePayload(List<ConfigValue> values) {
    }

    @GetMapping
    @PreAuthorize("hasAuthority('system:config:view')")
    public ApiResponse<Map<String, List<CmsSiteConfig>>> list() {
        Map<String, List<CmsSiteConfig>> groups = new LinkedHashMap<>();
        siteConfigMapper.selectList(Wrappers.<CmsSiteConfig>lambdaQuery()
                        .orderByAsc(CmsSiteConfig::getGroupCode).orderByAsc(CmsSiteConfig::getSort))
                .forEach(config -> groups.computeIfAbsent(config.getGroupCode(), key -> new java.util.ArrayList<>()).add(config));
        return ApiResponse.ok(groups);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('system:config:update')")
    @Transactional
    @CacheEvict(cacheNames = "site-config-public", allEntries = true)
    public ApiResponse<Void> save(@Valid @RequestBody SavePayload payload) {
        if (payload.values() == null) {
            throw BusinessException.badRequest("配置内容不能为空");
        }
        for (ConfigValue value : payload.values()) {
            CmsSiteConfig config = siteConfigMapper.selectOne(Wrappers.<CmsSiteConfig>lambdaQuery()
                    .eq(CmsSiteConfig::getConfigKey, value.configKey()).last("LIMIT 1"));
            if (config == null) {
                throw BusinessException.notFound("配置项不存在：" + value.configKey());
            }
            config.setConfigValue(value.configValue());
            config.setUpdatedBy(SecurityUtils.currentUserId());
            siteConfigMapper.updateById(config);
        }
        return ApiResponse.ok();
    }
}
