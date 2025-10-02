package com.xiaoyua.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaoyua.entity.FilePO;
import com.xiaoyua.entity.TaskFilePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface jTaskFileMapper extends BaseMapper<TaskFilePO> {

    /**
     * 查询单个任务的文件信息
     * @param taskId 任务ID
     * @return 文件信息列表
     */
    @Select({
            "SELECT f.id, f.file_url, f.thumb_url, f.size, f.biz_type",
            "FROM task_files tf",
            "JOIN files f ON tf.file_id = f.id",
            "WHERE tf.task_id = #{taskId}",
            "ORDER BY tf.id"
    })
    List<FilePO> selectFilesByTaskId(@Param("taskId") Long taskId);
}
