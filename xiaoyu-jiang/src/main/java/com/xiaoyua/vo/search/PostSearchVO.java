package com.xiaoyua.vo.search;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "post_search", createIndex = true)
@Setting(refreshInterval = "5s")
public class PostSearchVO {
    @Id
    private Long id;
    /* 完全匹配用 keyword，不需要分词 */
    @Field(type = FieldType.Keyword)
    private String title;
    @Field(type = FieldType.Keyword)
    private String content;
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    private UserVO user;



    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserVO{
        private Long id;
        private String nickname;
        private String avatarUrl;
    }
}
