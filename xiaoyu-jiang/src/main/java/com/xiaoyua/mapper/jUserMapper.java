package com.xiaoyua.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaoyua.entity.UserPO;
import com.xiaoyua.vo.user.UserSearchVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface jUserMapper extends BaseMapper<UserPO> {

    /**
     * 根据账号搜索用户
     *
     * @param page 分页参数
     * @param keyword 搜索关键词
     * @param campusId 校区ID过滤（可选）
     * @param onlyRealName 是否只搜索已实名用户
     * @return 搜索结果
     */
    IPage<UserSearchVO> searchUsersByAccount(Page<UserSearchVO> page,
                                             @Param("keyword") String keyword,
                                             @Param("campusId") Long campusId,
                                             @Param("onlyRealName") Boolean onlyRealName);

    /**
     * 根据昵称搜索用户
     *
     * @param page 分页参数
     * @param keyword 搜索关键词
     * @param campusId 校区ID过滤（可选）
     * @param onlyRealName 是否只搜索已实名用户
     * @return 搜索结果
     */
    IPage<UserSearchVO> searchUsersByNickname(Page<UserSearchVO> page,
                                              @Param("keyword") String keyword,
                                              @Param("campusId") Long campusId,
                                              @Param("onlyRealName") Boolean onlyRealName);

    /**
     * 根据用户ID获取用户详细信息（包含校区名称）
     *
     * @param userId 用户ID
     * @return 用户详细信息
     */
    UserPO selectUserWithCampus(@Param("userId") Long userId);
}
