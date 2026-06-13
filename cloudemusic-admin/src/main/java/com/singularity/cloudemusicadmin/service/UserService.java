package com.singularity.cloudemusicadmin.service;

import com.singularity.cloudemusicadmin.dto.request.ChangePasswordRequest;
import com.singularity.cloudemusicadmin.dto.request.LoginRequest;
import com.singularity.cloudemusicadmin.dto.request.RegisterRequest;
import com.singularity.cloudemusicadmin.dto.request.SongActionRequest;
import com.singularity.cloudemusicadmin.dto.request.UserInfoUpdateRequest;
import com.singularity.cloudemusicadmin.dto.response.LoginResponse;
import com.singularity.cloudemusicadmin.dto.response.UserInfoResponse;
import com.singularity.cloudemusicadmin.entity.Favorite;
import com.singularity.cloudemusicadmin.entity.History;
import com.singularity.cloudemusicadmin.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 用户业务接口
 */
public interface UserService {

    /** 登录，返回 JWT token */
    LoginResponse login(LoginRequest request);

    /** 注册，返回用户信息 */
    User register(RegisterRequest request);

    /** 获取当前用户信息 */
    UserInfoResponse getUserInfo(Long userId);

    /** 更新用户信息，返回更新后的用户数据 */
    UserInfoResponse updateUserInfo(Long userId, UserInfoUpdateRequest request);

    /** 注销账号（逻辑删除） */
    void deleteAccount(Long userId);

    /** 上传头像到 MinIO 并更新用户 avatar 字段 */
    UserInfoResponse uploadAvatar(Long userId, MultipartFile file);

    /** 修改密码 */
    void changePassword(Long userId, ChangePasswordRequest request);

    // ========== 收藏 ==========

    /** 添加收藏 */
    void addFavorite(Long userId, SongActionRequest request);

    /** 取消收藏 */
    void removeFavorite(Long userId, Long songId);

    /** 获取收藏列表 */
    List<Favorite> getFavorites(Long userId);

    /** 检查歌曲是否已收藏 */
    boolean checkFavorite(Long userId, Long songId);

    // ========== 播放历史 ==========

    /** 添加播放记录 */
    void addHistory(Long userId, SongActionRequest request);

    /** 获取播放历史（按时间倒序，最多 200 条） */
    List<History> getHistory(Long userId);

    /** 清空播放历史 */
    void clearHistory(Long userId);
}
