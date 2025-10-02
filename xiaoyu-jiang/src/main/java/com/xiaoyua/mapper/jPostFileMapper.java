package com.xiaoyua.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaoyua.dto.post.PostFileDTO;
import com.xiaoyua.entity.FilePO;
import com.xiaoyua.entity.PostFilePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface jPostFileMapper extends BaseMapper<PostFilePO> {

    /**
     * 批量查询动态文件信息
     * @param postIds 动态ID列表
     * @return 动态文件DTO列表
     */
    @Select({
            "<script>",
            "SELECT pf.post_id, f.id, f.file_url, f.thumb_url, f.size, f.biz_type",
            "FROM post_files pf",
            "JOIN files f ON pf.file_id = f.id",
            "WHERE pf.post_id IN",
            "<foreach collection='postIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
            "ORDER BY pf.post_id, pf.sort",
            "</script>"
    })
    List<PostFileDTO> selectFileByPostIds(@Param("postIds") List<Long> postIds);

    /**
     * 查询单个动态的文件信息
     * @param postId 动态ID
     * @return 文件信息列表
     */
    @Select({
            "SELECT f.id, f.file_url, f.thumb_url, f.size, f.biz_type",
            "FROM post_files pf",
            "JOIN files f ON pf.file_id = f.id",
            "WHERE pf.post_id = #{postId}",
            "ORDER BY pf.sort"
    })
    List<FilePO> selectFilesByPostId(@Param("postId") Long postId);
}
