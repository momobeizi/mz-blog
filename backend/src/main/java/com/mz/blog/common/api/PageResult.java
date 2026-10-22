package com.mz.blog.common.api;

import java.util.List;

public record PageResult<T>(List<T> records, long page, long size, long total, long pages) {

    public static <T> PageResult<T> of(List<T> records, long page, long size, long total) {
        long pages = size <= 0 ? 0 : (total + size - 1) / size;
        return new PageResult<>(records, page, size, total, pages);
    }
}
