package com.xiaoyua.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaoyua.entity.NotificationPO;
import com.xiaoyua.vo.notification.NotificationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 通知数据访问层
 *
 * @author xiaoyu
 */
@Mapper
public interface jNotificationMapper extends BaseMapper<NotificationPO> {

    /**
     * 分页查询通知列表（联表查询用户信息）
     *
     * @param page 分页参数
     * @param userId 用户ID
     * @param type 通知类型（可选）
     * @return 通知VO分页结果
     */
    IPage<NotificationVO> selectNotificationsWithUser(Page<NotificationVO> page,
                                                      @Param("userId") Long userId,
                                                      @Param("type") String type);
}
