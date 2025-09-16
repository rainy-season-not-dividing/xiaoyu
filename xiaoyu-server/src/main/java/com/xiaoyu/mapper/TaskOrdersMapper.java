package com.xiaoyu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaoyu.entity.TaskOrdersPO;
import com.xiaoyu.entity.TaskReviewsPO;
import com.xiaoyu.vo.task.ListTaskOrdersVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


@Mapper
public interface TaskOrdersMapper extends BaseMapper<TaskOrdersPO> {
    Page<ListTaskOrdersVO> getListOrders(@Param("pageSet") Page<ListTaskOrdersVO> pageSet, @Param("status") TaskOrdersPO.Status status, @Param("roleType") TaskReviewsPO.RoleType roleType, @Param("currentId") Long currentId);
}
