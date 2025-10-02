package com.xiaoyua.vo.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.xiaoyua.entity.CampusPO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户简要信息VO
 * <p>
 * 用于返回用户的基本公开信息，不包含敏感数据如手机号、身份证号等。
 * 主要用于动态列表、评论列表、好友列表等场景中显示用户基本信息。
 * </p>
 * 
 * <h3>使用场景：</h3>
 * <ul>
 *   <li>朋友圈动态作者信息</li>
 *   <li>评论作者信息</li>
 *   <li>任务发布者信息</li>
 *   <li>好友列表展示</li>
 *   <li>搜索结果用户信息</li>
 * </ul>
 * 
 * <h3>隐私保护：</h3>
 * <ul>
 *   <li>不包含手机号、邮箱等联系方式</li>
 *   <li>不包含身份证号、真实姓名等实名信息</li>
 *   <li>不包含登录密码、令牌等认证信息</li>
 *   <li>根据用户隐私设置可能隐藏部分信息</li>
 * </ul>
 * 
 * <h3>字段说明：</h3>
 * <ul>
 *   <li>性别：0-未知，1-男，2-女</li>
 *   <li>实名认证：0-未认证，1-已认证</li>
 * </ul>
 * 
 * @author xiaoyu
 * @since 1.0.0
 * @see com.xiaoyua.vo.user.UserVO
 * @see com.xiaoyua.vo.user.CurrentUserVO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSimpleVO {
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 用户昵称
     */
    private String nickname;
    
    /**
     * 头像URL
     */
    private String avatarUrl;
    
    /**
     * 性别：0-未知，1-男，2-女
     */
    private Integer gender;
    
    /**
     * 校园ID
     */
    private Long campusId;

    /**
     * 校区名称
     */
    private CampusPO.CampuseName campusName;
    
    /**
     * 是否实名认证：0-未认证，1-已认证
     */
    private Integer isRealName;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    

    public UserSimpleVO(Long id, String nickname, String avatarUrl, Integer gender,
                        Long campusId, Integer isRealName, LocalDateTime createdAt) {
        this.id = id;
        this.nickname = nickname;
        this.avatarUrl = avatarUrl;
        this.gender = gender;
        this.campusId = campusId;
        this.isRealName = isRealName;
        this.createdAt = createdAt;
    }

    
    @Override
    public String toString() {
        return "UserSimpleVO{" +
                "id=" + id +
                ", nickname='" + nickname + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                ", gender=" + gender +
                ", campusName=" + campusName +
                ", isRealName=" + isRealName +
                ", createdAt=" + createdAt +
                '}';
    }
}