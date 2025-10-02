package com.xiaoyu.result;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult<T> {

    private List<T> list;
    private PageInfo pagination;

    public PageResult(List<T> data, Integer page, Integer size, Long total) {
        this.list = data;
        this.pagination = new PageInfo();
        this.pagination.page = page;
        this.pagination.size = size;
        this.pagination.total = total;
        this.pagination.pages = (long) Math.ceil((double) total / size);
    }



    @Data
    public static class PageInfo {
        private Integer page;
        private Integer size;
        private Long total;
        private Long pages;
    }

}
