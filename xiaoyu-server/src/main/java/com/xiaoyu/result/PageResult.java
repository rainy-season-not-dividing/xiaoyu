package com.xiaoyu.result;


import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {

    private List<T> data;
    private PageInfo pagination;

    public PageResult(List<T> data, Integer page, Integer size, Long total) {
        this.data = data;
        this.pagination = new PageInfo();
        this.pagination.page = page;
        this.pagination.size = size;
        this.pagination.total = total;
        this.pagination.pages = (long) total / size;
    }



    @Data
    public static class PageInfo {
        private Integer page;
        private Integer size;
        private Long total;
        private Long pages;
    }

}
