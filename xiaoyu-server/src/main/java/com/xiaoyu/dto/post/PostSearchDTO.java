package com.xiaoyu.dto.post;


import lombok.Data;

@Data
public class PostSearchDTO {
    private String keyword;
    private Integer page;
    private Integer size;
}
