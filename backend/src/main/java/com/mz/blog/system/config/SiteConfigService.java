package com.mz.blog.system.config;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SiteConfigService {

    private final SiteConfigMapper siteConfigMapper;
    private final String baseUrlOverride;

    public SiteConfigService(SiteConfigMapper siteConfigMapper,
                             @Value("${app.site.base-url:}") String baseUrlOverride) {
        this.siteConfigMapper = siteConfigMapper;
        this.baseUrlOverride = baseUrlOverride;
    }

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

    public String siteBaseUrl(Map<String, String> config) {
        String baseUrl = StringUtils.hasText(baseUrlOverride)
                ? baseUrlOverride
                : get(config, "site.domain", "http://localhost:8080");
        return baseUrl.replaceAll("/+$", "");
    }
}
