package com.example.springbatch.common;

import lombok.Getter;

import java.util.List;

@Getter
public class PageResponse<T> {

    private List<T> content;
    private int page;
    private int size;
    private int totalPages;
    private long totalElements;
    private boolean first;
    private boolean last;

}
