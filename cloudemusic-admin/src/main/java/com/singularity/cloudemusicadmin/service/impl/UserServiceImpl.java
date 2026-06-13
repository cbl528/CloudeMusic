package com.singularity.cloudemusicadmin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.singularity.cloudemusicadmin.common.BusinessException;
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
import com.singularity.cloudemusicadmin.mapper.FavoriteMapper;
import com.singularity.cloudemusicadmin.mapper.HistoryMapper;
import com.singularity.cloudemusicadmin.mapper.UserMapper;
import com.singularity.cloudemusicadmin.service.MinioService;
import com.singularity.cloudemusicadmin.service.UserService;
import com.singularity.cloudemusicadmin.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户业务实现
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final MinioService minioService;
    private final FavoriteMapper favoriteMapper;
    private final HistoryMapper historyMapper;

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectOne(
                Wrappers.<User>lambdaQuery().eq(User::getUsername, request.getUsername()));

        if (user == null) {
            throw new BusinessException(400, "用户名或密码错误");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(400, "用户名或密码错误");
        }

        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(403, "账号已被禁用");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        return LoginResponse.builder()
                .token(token)
                .build();
    }

    @Override
    public User register(RegisterRequest request) {
        Long count = userMapper.selectCount(
                Wrappers.<User>lambdaQuery().eq(User::getUsername, request.getUsername()));

        if (count != null && count > 0) {
            throw new BusinessException(409, "用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setStatus(1);

        int insert = userMapper.insert(user);
        if (insert < 1) {
            throw new BusinessException(500, "注册失败");
        }
        return user;
    }

    @Override
    public UserInfoResponse getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return toUserInfoResponse(user);
    }

    @Override
    public UserInfoResponse updateUserInfo(Long userId, UserInfoUpdateRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getSignature() != null) {
            user.setSignature(request.getSignature());
        }
        int i = userMapper.updateById(user);
        if (i < 1) {
            throw new BusinessException(500, "更新用户信息失败");
        }
        return toUserInfoResponse(user);
    }

    @Override
    public void deleteAccount(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        userMapper.deleteById(userId);
    }

    @Override
    public UserInfoResponse uploadAvatar(Long userId, MultipartFile file) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        String avatarUrl = minioService.uploadAvatar(file, userId);
        user.setAvatar(avatarUrl);
        userMapper.updateById(user);

        return toUserInfoResponse(user);
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(400, "原密码错误");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);
    }

    // ========== 收藏 ==========

    @Override
    @Transactional
    public void addFavorite(Long userId, SongActionRequest request) {
        // 检查是否已收藏
        Long count = favoriteMapper.selectCount(
                Wrappers.<Favorite>lambdaQuery()
                        .eq(Favorite::getUserId, userId)
                        .eq(Favorite::getSongId, request.getSongId()));
        if (count != null && count > 0) {
            throw new BusinessException(409, "该歌曲已收藏");
        }

        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setSongId(request.getSongId());
        favorite.setSongName(request.getSongName() != null ? request.getSongName() : "");
        favorite.setArtist(request.getArtist() != null ? request.getArtist() : "");
        favorite.setCover(request.getCover() != null ? request.getCover() : "");
        favorite.setDuration(request.getDuration() != null ? request.getDuration() : 0);
        favorite.setType("song");

        favoriteMapper.insert(favorite);
    }

    @Override
    public void removeFavorite(Long userId, Long songId) {
        favoriteMapper.delete(
                Wrappers.<Favorite>lambdaQuery()
                        .eq(Favorite::getUserId, userId)
                        .eq(Favorite::getSongId, songId));
    }

    @Override
    public List<Favorite> getFavorites(Long userId) {
        return favoriteMapper.selectList(
                Wrappers.<Favorite>lambdaQuery()
                        .eq(Favorite::getUserId, userId)
                        .orderByDesc(Favorite::getCreateTime));
    }

    @Override
    public boolean checkFavorite(Long userId, Long songId) {
        Long count = favoriteMapper.selectCount(
                Wrappers.<Favorite>lambdaQuery()
                        .eq(Favorite::getUserId, userId)
                        .eq(Favorite::getSongId, songId));
        return count != null && count > 0;
    }

    // ========== 播放历史 ==========

    @Override
    @Transactional
    public void addHistory(Long userId, SongActionRequest request) {
        // 插入新记录
        History history = new History();
        history.setUserId(userId);
        history.setSongId(request.getSongId());
        history.setSongName(request.getSongName() != null ? request.getSongName() : "");
        history.setArtist(request.getArtist() != null ? request.getArtist() : "");
        history.setCover(request.getCover() != null ? request.getCover() : "");
        history.setDuration(request.getDuration() != null ? request.getDuration() : 0);
        history.setPlayedAt(LocalDateTime.now());

        historyMapper.insert(history);

        // 保留最近 200 条，删除更早的
        List<History> kept = historyMapper.selectList(
                Wrappers.<History>lambdaQuery()
                        .eq(History::getUserId, userId)
                        .orderByDesc(History::getPlayedAt)
                        .last("LIMIT 200"));
        if (!kept.isEmpty()) {
            Long lastId = kept.get(kept.size() - 1).getId();
            historyMapper.delete(
                    Wrappers.<History>lambdaQuery()
                            .eq(History::getUserId, userId)
                            .lt(History::getId, lastId));
        }
    }

    @Override
    public List<History> getHistory(Long userId) {
        return historyMapper.selectList(
                Wrappers.<History>lambdaQuery()
                        .eq(History::getUserId, userId)
                        .orderByDesc(History::getPlayedAt)
                        .last("LIMIT 200"));
    }

    @Override
    public void clearHistory(Long userId) {
        historyMapper.delete(
                Wrappers.<History>lambdaQuery()
                        .eq(History::getUserId, userId));
    }

    private UserInfoResponse toUserInfoResponse(User user) {
        UserInfoResponse resp = new UserInfoResponse();
        resp.setId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setNickname(user.getNickname());
        resp.setAvatar(user.getAvatar());
        resp.setEmail(user.getEmail());
        resp.setPhone(user.getPhone());
        resp.setGender(user.getGender());
        resp.setSignature(user.getSignature());
        resp.setCreateTime(user.getCreateTime());
        return resp;
    }
}
