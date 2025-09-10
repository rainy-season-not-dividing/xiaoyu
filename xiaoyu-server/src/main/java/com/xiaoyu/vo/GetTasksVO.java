package com.xiaoyu.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务列表返回 VO
 */
@Data
public class GetTasksVO {

    /** 任务ID */
    private Long id;

    /** 发布者信息 */
    private PublisherVO publisher;

    /** 任务标题 */
    private String title;

    /** 任务详情 */
    private String content;

    /** 悬赏金额 */
    private BigDecimal reward;

    /** 任务状态（DRAFT/AUDITING/RECRUIT/RUNNING/FINISH/CLOSED/ARBITRATED） */
    private String status;

    /** 可见范围（PUBLIC/FRIEND/CAMPUS） */
    private String visibility;

    /** 报名截止（ISO8601） */
    private LocalDateTime expireAt;

    /** 创建时间（ISO8601） */
    private LocalDateTime createdAt;

    /** 任务文件列表 */
    private List<FileItemVO> files;

    /** 任务标签列表 */
    private List<TagItemVO> tags;

    /** 统计信息（浏览量、接单量） */
    private StatsVO stats;



    /* ---------------- 子VO ---------------- */
    @Data
    public static class PublisherVO {
        /** 用户ID */
        private Long id;
        /** 昵称 */
        private String nickname;
        /** 头像URL */
        private String avatarUrl;
    }
    @Data
    public static class FileItemVO {
        /** 文件ID */
        private Long id;
        /** 缩略图URL */
        private String thumbUrl;
        /** 原图URL */
        private String fileUrl;
        /** 文件大小*/
        private Integer size;
    }
    @Data
    public static class TagItemVO {
        /** 标签ID */
        private Long id;
        /** 标签名称 */
        private String name;

    }
    @Data
    public static class StatsVO {
        /** 浏览次数 */
        private Integer viewCnt;
        /** 接单次数 */
        private Integer orderCnt;
    }

}