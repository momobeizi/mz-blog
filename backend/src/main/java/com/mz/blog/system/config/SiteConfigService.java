package com.mz.blog.system.config;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SiteConfigService {

    private final SiteConfigMapper siteConfigMapper;

    @Cacheable(cacheNames = "site-config-public", key = "'all'")
    public Map<String, String> publicConfig() {
        Map<String, String> result = new LinkedHashMap<>();
        List<CmsSiteConfig> configs = siteConfigMapper.selectList(Wrappers.<CmsSiteConfig>lambdaQuery()
                .eq(CmsSiteConfig::getIsPublic, 1)
                .orderByAsc(CmsSiteConfig::getGroupCode).orderByAsc(CmsSiteConfig::getSort));
        configs.forEach(config -> result.put(config.getConfigKey(),
                config.getConfigValue() == null ? "" : config.getConfigValue()));
        return result;
    }

    public String get(Map<String, String> config, String key, String fallback) {
        String value = config.get(key);
        return value == null || value.isBlank() ? fallback : value;
    }

    public boolean bool(Map<String, String> config, String key, boolean fallback) {
        String value = config.get(key);
        return value == null ? fallback : Boolean.parseBoolean(value);
    }
}
