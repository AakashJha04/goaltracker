package com.aakash.goalkeeper.goal.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/** Generic pagination envelope reused across list endpoints. */
public record PageResponse<T>(
        List<T> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static <S, T> PageResponse<T> of(Page<S> page, Function<S, T> mapper) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
